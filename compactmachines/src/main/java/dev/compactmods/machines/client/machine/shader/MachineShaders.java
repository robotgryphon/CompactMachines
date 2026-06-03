package dev.compactmods.machines.client.machine.shader;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.compactmods.machines.api.CompactMachines;
import dev.compactmods.machines.core.CompactMachinesCore;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

/**
 * Known shader ids the Compact Machine block model can dispatch to, plus the
 * standalone {@link RenderPipeline}s / {@link RenderType}s backing them.
 *
 * <p>This is intentionally not a full registry yet — it's a small constants
 * holder for shaders whose behaviour is fixed at compile time (tye_dye). The
 * stripe-style "flag" shaders are configured at runtime instead, see
 * {@link MachineFlagRenderTypes} and {@link MachineFlags}.</p>
 */
public interface MachineShaders {

    /**
     * Domain-warped noise palette gradient — reads as a slow lava-lamp
     * turbulence with a ROY G BIV default palette. Formerly named "pride",
     * but it had drifted away from anything that read as a pride flag, so
     * it lives on as a celebratory pattern in its own right.
     */
    Identifier TYE_DYE = CompactMachinesCore.identifier("tye_dye");

    /** Tye-dye lava-lamp pipeline. */
    RenderPipeline TYE_DYE_PIPELINE = RenderPipeline.builder(RenderPipelines.GLOBALS_SNIPPET)
            .withLocation(CompactMachines.identifier("pipeline/tye_dye"))
            .withVertexShader(CompactMachines.identifier("tye_dye"))
            .withFragmentShader(CompactMachines.identifier("tye_dye"))
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR_NORMAL, VertexFormat.Mode.QUADS)
            .withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
            .withUniform("Projection", UniformType.UNIFORM_BUFFER)
            .withCull(true)
            // Depth-test against opaque geometry, but DO NOT write depth — otherwise
            // the overlay occludes anything translucent rendered after it (water
            // behind the cube, other compact machines in the same frame, etc.).
            .withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, false))
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .build();

    /**
     * Render type that draws geometry through {@link #TYE_DYE_PIPELINE}.
     *
     * <p>Mirrors vanilla {@code TRANSLUCENT_MOVING_BLOCK}: vertices get sorted
     * back-to-front on upload, and the geometry is rendered to
     * {@link OutputTarget#ITEM_ENTITY_TARGET} — the translucent-compositing
     * framebuffer — so the overlay composes correctly with the chunk
     * translucent pass (water, glass, other compact machines) instead of being
     * depth-occluded by it.</p>
     */
    RenderType TYE_DYE_RENDER_TYPE = RenderType.create(
            "compactmachines:tye_dye",
            RenderSetup.builder(TYE_DYE_PIPELINE)
//                    .sortOnUpload()
//                    .setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET)
                    .createRenderSetup());

    /**
     * Look up the {@link RenderType} for a given shader / flag id, or
     * {@code null} if neither catalog knows it.
     *
     * <p>Tye-dye is checked first because it's the single special-effect
     * shader; everything else is dispatched to the
     * {@link MachineFlagRenderTypes flag-driven} pipelines.</p>
     */
    static @Nullable RenderType renderTypeFor(Identifier id) {
        if (TYE_DYE.equals(id)) return TYE_DYE_RENDER_TYPE;
        return MachineFlagRenderTypes.byId(id).orElse(null);
    }
}
