package dev.compactmods.machines.preview.client;

import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexSorting;
import dev.compactmods.machines.core.CompactMachinesCore;
import dev.compactmods.machines.machine.block.CompactMachineBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.util.context.ContextKey;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.OptionalDouble;

/**
 * Draws the in-block room previews as opaque 3D geometry with a <em>real, self-owned depth buffer</em>.
 *
 * <p>The preview is opaque, interpenetrating voxel geometry, so it can't be composited through the
 * translucent/OIT-style targets (there is no true order-independent transparency in this version) —
 * it needs genuine depth sorting. So, mirroring exactly how the vanilla Fabulous aux targets work, we
 * keep our <em>own</em> {@link TextureTarget}: each frame we {@linkplain RenderTarget#copyDepthFrom
 * copy the world's solid depth} into it (so nearer blocks / the machine frame occlude the preview),
 * render the batched preview mesh into it with full depth test + write (so the voxels sort correctly
 * amongst themselves), then {@linkplain RenderTarget#blitAndBlendToTexture blend it back onto the main
 * target}. This runs at {@link RenderLevelStageEvent.AfterOpaqueBlocks} — after the world's solid
 * pass, before the machines' (shader-drawn) translucent panes, which then composite over the preview.
 *
 * <p>Every visible machine contributes its {@link RoomPreviewMesh} to one vertex buffer, each
 * transformed to camera-relative world space with the uniform-fit-centred placement.
 */
public final class RoomPreviewRenderer {

    /** How much of the block the preview fills (the machine's inner pane window). */
    private static final float FIT = 14f / 16f;
    /** Block-space Y of the inner window's floor — the room's floor is anchored here. */
    private static final float FLOOR_Y = (1f - FIT) / 2f;

    private static final ContextKey<State> KEY = new ContextKey<>(CompactMachinesCore.identifier("room_preview_renderer"));

    private static final VertexFormat FORMAT = DefaultVertexFormat.POSITION_COLOR;
    private static final ByteBufferBuilder SCRATCH = new ByteBufferBuilder(FORMAT.getVertexSize() * 4 * 64);

    // DynamicTransforms inputs — the vertices already carry world position, so no extra offset/tint.
    private static final Vector4f COLOR_MODULATOR = new Vector4f(1, 1, 1, 1);
    private static final Vector3f WORLD_OFFSET = new Vector3f();
    private static final Matrix4f TEXTURE_TRANSFORM = new Matrix4f();
    /** Fully transparent clear for our colour buffer, so only preview pixels blend back onto main. */
    private static final Vector4fc CLEAR_TRANSPARENT = new Vector4f(0f, 0f, 0f, 0f);

    /** Our own screen-sized colour+depth buffer; lazily created and resized on the render thread. */
    private static @Nullable TextureTarget previewTarget;

    private RoomPreviewRenderer() {}

    // --- extraction (build the batched mesh) -----------------------------------------

    public static void extract(ExtractLevelRenderStateEvent event) {
        final State state = event.getRenderState().getRenderDataOrDefault(KEY, State.INSTANCE);
        state.reset();

        final var frustum = event.getFrustum();
        final double camX = frustum.getCamX(), camY = frustum.getCamY(), camZ = frustum.getCamZ();

        final BufferBuilder builder = new BufferBuilder(SCRATCH, PrimitiveTopology.QUADS, FORMAT);
        final PoseStack pose = new PoseStack();
        final boolean[] any = {false};

        event.getLevelExtractor().iterateVisibleBlockEntities(be -> {
            if (!(be instanceof CompactMachineBlockEntity machine)) return;
            final String code = machine.connectedRoom().orElse(null);
            if (code == null) return;
            final RoomPreviewMesh mesh = RoomPreviewMeshCache.get(code);
            if (mesh == null) return;

            final var p = machine.getBlockPos();
            final float scale = FIT / mesh.maxDimension();

            // X/Z centred in the block; Y anchored so the room's floor sits on the inner-window floor
            // (the room then grows upward — a short room stays low, a tall one fills toward the top).
            pose.pushPose();
            pose.translate(p.getX() - camX, p.getY() - camY, p.getZ() - camZ);
            pose.translate(0.5f, FLOOR_Y, 0.5f);
            pose.scale(scale, scale, scale);
            pose.translate(-mesh.sizeX / 2f, 0f, -mesh.sizeZ / 2f);
            mesh.emit(pose.last(), builder);
            pose.popPose();
            any[0] = true;
        });

        if (!any[0]) {
            event.getRenderState().setRenderData(KEY, state);
            return;
        }

        final MeshData data = builder.build();
        if (data != null) {
            try (data) {
                // QUADS meshes carry no index buffer until sorted (they'd otherwise use the shared
                // sequential indices); sorting materialises an explicit index buffer to upload.
                data.sortQuads(SCRATCH, VertexSorting.DISTANCE_TO_ORIGIN);
                state.drawState = data.drawState();
                state.vertexBuffer = uploadVertices(state.vertexBuffer, data);
                state.indexBuffer = uploadIndices(state.indexBuffer, data);
            }
        }
        event.getRenderState().setRenderData(KEY, state);
    }

    // --- drawing (one manual pass) ---------------------------------------------------

    /**
     * Drawn at {@link RenderLevelStageEvent.AfterOpaqueBlocks} — after the world's solid pass, before
     * the machines' translucent panes. See the class doc for the self-owned-depth-buffer approach.
     */
    public static void afterSolidBlocks(RenderLevelStageEvent.AfterOpaqueBlocks event) {
        final State state = event.getLevelRenderState().getRenderData(KEY);
        if (state == null || state.drawState == null || state.vertexBuffer == null || state.indexBuffer == null)
            return;

        final RenderTarget main = Minecraft.getInstance().gameRenderer.mainRenderTarget();
        final TextureTarget target = ensureTarget(main.width, main.height);

        // Seed our depth with the world's solid depth so nearer blocks / the machine frame occlude it.
        target.copyDepthFrom(main);

        final var transforms = RenderSystem.getDynamicUniforms()
                .writeTransform(RenderSystem.getModelViewMatrixCopy(), COLOR_MODULATOR, WORLD_OFFSET, TEXTURE_TRANSFORM);

        // Clear only our colour (transparent) and keep the copied depth; render with full depth write.
        try (var pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                () -> CompactMachinesCore.dotPrefix("room_preview"),
                target.getColorTextureView(), Optional.of(CLEAR_TRANSPARENT),
                target.getDepthTextureView(), OptionalDouble.empty())) {
            pass.setPipeline(RoomPreviewRenderType.PIPELINE);
            RenderSystem.bindDefaultUniforms(pass);
            pass.setVertexBuffer(0, state.vertexBuffer.slice());
            pass.setIndexBuffer(state.indexBuffer, state.drawState.indexType());
            pass.setUniform("DynamicTransforms", transforms);
            pass.drawIndexed(state.drawState.indexCount(), 1, 0, 0, 0);
        }

        // Alpha-blend our preview colour back onto the main scene (same call the vanilla aux targets use).
        target.blitAndBlendToTexture(main.getColorTextureView(), main.getDepthTextureView());
    }

    private static TextureTarget ensureTarget(int width, int height) {
        if (previewTarget == null) {
            previewTarget = new TextureTarget("compactmachines:room_preview", width, height, true, GpuFormat.RGBA8_UNORM);
        } else if (previewTarget.width != width || previewTarget.height != height) {
            previewTarget.resize(width, height);
        }
        return previewTarget;
    }

    // --- GPU buffer helpers (create-or-reuse, mirrors VerticesHelper) -----------------

    private static GpuBuffer uploadVertices(@Nullable GpuBuffer existing, MeshData mesh) {
        final var buf = mesh.vertexBuffer();
        if (existing == null || existing.size() < buf.remaining()) {
            if (existing != null) existing.close();
            return RenderSystem.getDevice().createBuffer(
                    () -> CompactMachinesCore.dotPrefix("room_preview_vertices"),
                    GpuBuffer.USAGE_COPY_DST | GpuBuffer.USAGE_VERTEX, buf);
        }
        RenderSystem.getDevice().createCommandEncoder().writeToBuffer(existing.slice(), buf);
        return existing;
    }

    private static GpuBuffer uploadIndices(@Nullable GpuBuffer existing, MeshData mesh) {
        final var buf = mesh.indexBuffer();
        if (existing == null || existing.size() < buf.remaining()) {
            if (existing != null) existing.close();
            return RenderSystem.getDevice().createBuffer(
                    () -> CompactMachinesCore.dotPrefix("room_preview_indices"),
                    GpuBuffer.USAGE_COPY_DST | GpuBuffer.USAGE_INDEX, buf);
        }
        RenderSystem.getDevice().createCommandEncoder().writeToBuffer(existing.slice(), buf);
        return existing;
    }

    /** Per-level-render-state batched mesh; buffers persist across frames and are reused. */
    private static final class State {
        static final State INSTANCE = new State();

        MeshData.@Nullable DrawState drawState;
        @Nullable GpuBuffer vertexBuffer;
        @Nullable GpuBuffer indexBuffer;

        void reset() {
            drawState = null;
            // Keep the GPU buffers allocated for reuse; only the draw state is per-frame.
        }
    }
}
