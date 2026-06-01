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
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Holds one compiled {@link RenderType} per registered {@link MachineFlag}.
 *
 * <p>At {@link RegisterRenderPipelinesEvent} time
 * ({@link #registerAll(RegisterRenderPipelinesEvent)}), this class walks
 * {@link MachineFlags#all()}, builds a dedicated {@link RenderPipeline} for
 * each flag with the palette baked in via {@code withShaderDefine}, registers
 * the pipeline through the event, and creates the matching {@link RenderType}
 * via {@link RenderType#create}. The result is cached in a static map so the
 * BER can look up the correct render type with one hashmap probe per frame.</p>
 *
 * <p>All flag pipelines share the same vertex/fragment shader source
 * ({@code shaders/pride_stripes.vsh/fsh}) — the only thing that varies between
 * compiled programs is the palette macros.</p>
 */
public final class MachineFlagRenderTypes {

    private MachineFlagRenderTypes() {}

    private static final Map<Identifier, RenderType> BY_FLAG_ID = new HashMap<>();

    /**
     * Listener target for {@link RegisterRenderPipelinesEvent}. Idempotent —
     * safe to call again after a resource reload once the pipeline map is
     * itself rebuilt.
     */
    public static void registerAll(RegisterRenderPipelinesEvent event) {
        BY_FLAG_ID.clear();
        for (MachineFlag flag : MachineFlags.all()) {
            var pipeline = buildPipeline(flag);
            event.registerPipeline(pipeline);
            // Same translucent-compositing setup vanilla TRANSLUCENT_MOVING_BLOCK
            // uses: sortOnUpload() + ITEM_ENTITY_TARGET. Without these the overlay
            // gets depth-occluded by anything translucent (water, glass, other
            // machines) that the chunk pass has already written depth for.
            var renderType = RenderType.create(
                    "compactmachines:" + flag.id().getPath().replace('/', '_'),
                    RenderSetup.builder(pipeline)
                            .sortOnUpload()
                            .setOutputTarget(OutputTarget.MAIN_TARGET)
                            .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                            .createRenderSetup());
            BY_FLAG_ID.put(flag.id(), renderType);
        }
    }

    /** Render type for {@code flagId}, or {@code null} if no flag with that id is registered. */
    public static Optional<RenderType> byId(Identifier flagId) {
        return Optional.ofNullable(BY_FLAG_ID.get(flagId));
    }

    /** Read-only snapshot of all currently-registered flag render types. */
    public static Map<Identifier, RenderType> all() {
        return Collections.unmodifiableMap(BY_FLAG_ID);
    }

    // ---------- pipeline construction ----------

    /**
     * Build the {@link RenderPipeline} for the given flag. Configuration
     * matches {@code MachineShaders.TYE_DYE_PIPELINE} except that:
     * <ul>
     *   <li>The pipeline location, vertex shader, and fragment shader all
     *       point at {@code pride_stripes}.</li>
     *   <li>Shader defines bake the flag's palette into the compiled GLSL.</li>
     * </ul>
     */
    private static RenderPipeline buildPipeline(MachineFlag flag) {
        // Unique location per flag so each variant gets its own slot in
        // RenderPipelines.PIPELINES_BY_LOCATION.
        Identifier location = CompactMachines.identifier(
                "pipeline/" + flag.id().getPath().replace('/', '_'));

        RenderPipeline.Builder b = RenderPipeline.builder(RenderPipelines.GLOBALS_SNIPPET)
                .withLocation(location)
                .withVertexShader(CompactMachines.identifier("pride_stripes"))
                .withFragmentShader(CompactMachines.identifier("pride_stripes"))
                .withVertexFormat(DefaultVertexFormat.POSITION_COLOR_NORMAL, VertexFormat.Mode.QUADS)
                .withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
                .withUniform("Projection", UniformType.UNIFORM_BUFFER)
                .withCull(true)
                // Depth-write ON: this is what vanilla TRANSLUCENT_BLOCK does (it
                // inherits DepthStencilState.DEFAULT = LEQUAL, writeDepth = true via
                // GENERIC_BLOCKS_SNIPPET). With sortOnUpload back-to-front, near
                // pixels write depth and properly occlude far overlays. The
                // diagnostic showed 5 machines all submit + emit correctly, so the
                // remaining leak-through was caused by depth-write being off:
                // without it, draw order alone decides which overlay wins, and the
                // single-pass buffer source flushes mid-stream when any other RT
                // request comes in, breaking the cross-batch sort.
                .withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, true))
                .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                // Float, cast to int in the shader; matches the C{i}_* float defines.
                .withShaderDefine("PALETTE_SIZE", (float) flag.size());

        // Per-component RGB defines. The shader has #ifndef fallbacks for every
        // slot, so unspecified ones harmlessly stay at their defaults.
        int n = flag.size();
        for (int i = 0; i < n; i++) {
            b.withShaderDefine("C" + i + "_R", flag.r(i));
            b.withShaderDefine("C" + i + "_G", flag.g(i));
            b.withShaderDefine("C" + i + "_B", flag.b(i));
        }

        return b.build();
    }
}
