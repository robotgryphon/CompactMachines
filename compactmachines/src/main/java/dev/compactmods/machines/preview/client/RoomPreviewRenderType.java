package dev.compactmods.machines.preview.client;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import dev.compactmods.machines.core.CompactMachinesCore;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.RenderPipelines;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;

/**
 * The pipeline and render type used to draw the in-block room preview.
 *
 * <p>It is intentionally minimal: opaque, depth-tested, {@code POSITION_COLOR} geometry driven by
 * vanilla's stock {@code core/position_color} shader — the same shader/bind-group set vanilla uses
 * for its debug-fill pipelines, so no custom GLSL ships with the mod. Shading and block colour are
 * baked into the per-vertex colour by {@link RoomPreviewMesh}, which is why no lightmap/normal is
 * needed. Backface culling is left <em>off</em> so the preview is robust to winding — with only the
 * surface faces emitted and depth-writes on, the nearest face still wins.
 */
public final class RoomPreviewRenderType {

    public static final RenderPipeline PIPELINE = RenderPipeline.builder(RenderPipelines.GLOBALS_SNIPPET)
            .withLocation(CompactMachinesCore.identifier("pipeline/room_preview"))
            .withVertexShader("core/position_color")
            .withFragmentShader("core/position_color")
            .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
            .withPrimitiveTopology(PrimitiveTopology.QUADS)
            .withBindGroupLayout(BindGroupLayouts.MATRICES_PROJECTION)
            .withCull(false)
            // Opaque geometry with a real depth buffer: test AND write. GREATER_THAN_OR_EQUAL because
            // this MC uses reversed-Z depth (near = far value) — same as every vanilla block pipeline;
            // LESS_*_EQUAL renders the voxels inside-out and mis-sorts against the world.
            .withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, true))
            .withColorTargetState(ColorTargetState.DEFAULT)
            .build();

    private RoomPreviewRenderType() {}

    /** Registers {@link #PIPELINE} so its shaders compile on resource load. Mod-bus event. */
    public static void registerPipelines(RegisterRenderPipelinesEvent event) {
        event.registerPipeline(PIPELINE);
    }
}
