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
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class MachineFlagRenderTypes {

    // Happy Pride y'all
    public static final OutputTarget TRANSLUCENT_TARGET = new OutputTarget("trans_target",
            () -> Minecraft.getInstance().levelRenderer.getTranslucentTarget());

    public static final RenderPipeline PRIDE_STRIPES_PIPELINE = RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET, RenderPipelines.BLOCK_SNIPPET)
            .withLocation(CompactMachines.identifier("pride_stripes"))
            .withVertexShader(CompactMachines.identifier("pride_stripes"))
            .withFragmentShader(CompactMachines.identifier("pride_stripes"))
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR_NORMAL, VertexFormat.Mode.QUADS)
            .withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
            .withUniform("Projection", UniformType.UNIFORM_BUFFER)
            // Palette UBO — populated each frame by MachineShaderRenderer from
            // the resolved FlagShader registry entry. See pride_stripes.fsh for
            // the std140 layout of the block.
            .withUniform("FlagPalette", UniformType.UNIFORM_BUFFER)
            .withCull(true)
            .withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, true))
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .build();

    public static final RenderType PRIDE_STRIPES_RENDER_TYPE = RenderType.create(
            CompactMachinesCore.id("pride_stripes"),
            RenderSetup.builder(PRIDE_STRIPES_PIPELINE)
                    .sortOnUpload()
                    .setOutputTarget(TRANSLUCENT_TARGET)
                    .createRenderSetup());



    /**
     * Listener target for {@link RegisterRenderPipelinesEvent}. Idempotent —
     * safe to call again after a resource reload once the pipeline map is
     * itself rebuilt.
     */
    public static void registerAll(RegisterRenderPipelinesEvent event) {
        event.registerPipeline(PRIDE_STRIPES_PIPELINE);
    }
}
