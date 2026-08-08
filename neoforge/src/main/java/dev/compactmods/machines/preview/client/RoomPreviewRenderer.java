package dev.compactmods.machines.preview.client;

import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.compactmods.machines.core.CompactMachinesCore;
import dev.compactmods.machines.machine.block.CompactMachineBlockEntity;
import dev.compactmods.machines.preview.RoomEntitySnapshot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;

/**
 * Draws the textured room previews. Each room is baked once into per-layer GPU buffers
 * ({@link RoomPreviewMesh}, in room-local space); this renderer draws that mesh once per visible
 * machine, applying the machine's placement transform via the per-draw model-view matrix.
 *
 * <p>Because the preview is opaque, depth-sorted 3D geometry (and this MC has no true OIT), it needs
 * a real depth buffer. We keep our <em>own</em> {@link TextureTarget}: {@linkplain
 * RenderTarget#copyDepthFrom copy the world's solid depth} into it (so nearer blocks / the machine
 * frame occlude the preview), draw with the vanilla {@code *_BLOCK} pipelines (reversed-Z depth
 * test+write, block atlas + lightmap), then {@linkplain RenderTarget#blitAndBlendToTexture blend it
 * onto the main target}. Runs at {@link RenderLevelStageEvent.AfterOpaqueBlocks} — between the solid
 * pass and the machines' translucent panes, which then composite over the preview.
 */
public final class RoomPreviewRenderer {

    /** How much of the block the preview fills (the machine's inner pane window). */
    private static final float FIT = 14f / 16f;
    /** Block-space Y of the inner window's floor — the room's floor is anchored here. */
    private static final float FLOOR_Y = (1f - FIT) / 2f;

    /** Draw order: opaque, then cutout, then translucent last. */
    private static final ChunkSectionLayer[] LAYERS =
            {ChunkSectionLayer.SOLID, ChunkSectionLayer.CUTOUT, ChunkSectionLayer.TRANSLUCENT};

    private static final ContextKey<State> KEY = new ContextKey<>(CompactMachinesCore.identifier("room_preview_renderer"));

    private static final Vector4f COLOR_MODULATOR = new Vector4f(1, 1, 1, 1);
    private static final Vector3f MODEL_OFFSET = new Vector3f();
    private static final Matrix4f TEXTURE_TRANSFORM = new Matrix4f();
    private static final Vector4fc CLEAR_TRANSPARENT = new Vector4f(0f, 0f, 0f, 0f);

    private static @Nullable TextureTarget previewTarget;

    private RoomPreviewRenderer() {}

    // --- extraction: collect visible machines + ensure their room meshes are baked ---

    public static void extract(ExtractLevelRenderStateEvent event) {
        final State state = event.getRenderState().getRenderDataOrDefault(KEY, State.INSTANCE);
        state.entries.clear();

        final var frustum = event.getFrustum();
        state.camX = frustum.getCamX();
        state.camY = frustum.getCamY();
        state.camZ = frustum.getCamZ();

        event.getLevelExtractor().iterateVisibleBlockEntities(be -> {
            if (!(be instanceof CompactMachineBlockEntity machine)) return;
            final String code = machine.connectedRoom().orElse(null);
            if (code == null) return;
            final RoomPreviewMesh mesh = RoomPreviewMeshCache.get(code);
            if (mesh == null) return;
            state.entries.add(new Entry(machine.getBlockPos(), code, mesh));
        });

        event.getRenderState().setRenderData(KEY, state);
    }

    // --- drawing ---------------------------------------------------------------------

    public static void afterSolidBlocks(RenderLevelStageEvent.AfterOpaqueBlocks event) {
        final State state = event.getLevelRenderState().getRenderData(KEY);
        if (state == null || state.entries.isEmpty()) return;

        final Minecraft mc = Minecraft.getInstance();
        final RenderTarget main = mc.gameRenderer.mainRenderTarget();
        final TextureTarget target = ensureTarget(main.width, main.height);
        target.copyDepthFrom(main);

        final Matrix4f view = RenderSystem.getModelViewMatrixCopy();
        final var atlas = mc.getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS).getTextureView();
        final var lightmap = mc.gameRenderer.lightmap();
        final var atlasSampler = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST);
        final var lightmapSampler = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR);
        final var quadIndices = RenderSystem.getSequentialBuffer(PrimitiveTopology.QUADS);

        try (var pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                () -> CompactMachinesCore.dotPrefix("room_preview"),
                target.getColorTextureView(), Optional.of(CLEAR_TRANSPARENT),
                target.getDepthTextureView(), OptionalDouble.empty())) {

            for (ChunkSectionLayer layer : LAYERS) {
                final RenderPipeline pipeline = blockPipeline(layer);
                pass.setPipeline(pipeline);
                RenderSystem.bindDefaultUniforms(pass);
                pass.bindTexture("Sampler0", atlas, atlasSampler);
                pass.bindTexture("Sampler2", lightmap, lightmapSampler);

                for (Entry entry : state.entries) {
                    final RoomPreviewMesh.LayerMesh lm = entry.mesh.layer(layer);
                    if (lm == null) continue;

                    final Matrix4f modelView = modelView(view, entry, state.camX, state.camY, state.camZ);
                    final var transforms = RenderSystem.getDynamicUniforms()
                            .writeTransform(modelView, COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_TRANSFORM);

                    pass.setUniform("DynamicTransforms", transforms);
                    pass.setVertexBuffer(0, lm.vertexBuffer().slice());
                    pass.setIndexBuffer(quadIndices.getBuffer(lm.indexCount()), quadIndices.type());
                    pass.drawIndexed(lm.indexCount(), 1, 0, 0, 0);
                }
            }
        }

        // Bridge our preview blocks' depth into the main target so the entities we inject into the
        // level's own submit pass (see onSubmitCustomGeometry) occlude correctly against them.
        main.copyDepthFrom(target);
        target.blitAndBlendToTexture(main.getColorTextureView(), main.getDepthTextureView());
    }

    /**
     * Injects the live tracked entities into the level's own submit pass, so they are drawn by the
     * regular entity/feature renderers (which can't be invoked re-entrantly from a stage event). Each
     * entity is submitted under its machine's placement transform, matching the block mesh; the level
     * then draws them into the world at the shrunk-down preview location.
     */
    public static void onSubmitCustomGeometry(net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent event) {
        final State state = event.getLevelRenderState().getRenderData(KEY);
        if (state == null || state.entries.isEmpty()) return;

        final Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        RoomPreviewEntities.retainCurrent();

        final var dispatcher = mc.getEntityRenderDispatcher();
        final float partialTick = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        final var camera = event.getLevelRenderState().cameraRenderState;
        final var collector = event.getSubmitNodeCollector();
        final PoseStack pose = event.getPoseStack();

        for (Entry entry : state.entries) {
            final var snapshots = ClientRoomEntities.get(entry.roomCode);
            if (snapshots.isEmpty()) continue;
            final float scale = FIT / entry.mesh.maxDimension();

            for (RoomEntitySnapshot snapshot : snapshots) {
                final Entity entity = RoomPreviewEntities.get(entry.roomCode, snapshot.entityId());
                if (entity == null) continue;

                final EntityRenderState rs = dispatcher.extractEntity(entity, partialTick);
                rs.lightCoords = LightCoordsUtil.FULL_BRIGHT;
                rs.shadowPieces.clear();
                rs.shadowRadius = 0f;

                // The entity is ticked at 20 Hz; interpolate its room-local feet by partial tick.
                final double ex = Mth.lerp((double) partialTick, entity.xOld, entity.getX());
                final double ey = Mth.lerp((double) partialTick, entity.yOld, entity.getY());
                final double ez = Mth.lerp((double) partialTick, entity.zOld, entity.getZ());

                // Camera-relative machine transform (camera view is applied by the level), then the
                // entity's room-local feet — matching the block mesh placement.
                pose.pushPose();
                pose.translate(entry.pos.getX() - state.camX, entry.pos.getY() - state.camY, entry.pos.getZ() - state.camZ);
                pose.translate(0.5f, FLOOR_Y, 0.5f);
                pose.scale(scale, scale, scale);
                pose.translate(-entry.mesh.sizeX / 2f, 0f, -entry.mesh.sizeZ / 2f);
                pose.translate(ex, ey, ez);
                dispatcher.submit(rs, camera, 0, 0, 0, pose, collector);
                pose.popPose();
            }
        }
    }

    /** Model-view for one machine: camera view × (block-relative-to-camera) × uniform-fit, floor-anchored. */
    private static Matrix4f modelView(Matrix4f view, Entry entry, double camX, double camY, double camZ) {
        final RoomPreviewMesh mesh = entry.mesh;
        final BlockPos p = entry.pos;
        final float scale = FIT / mesh.maxDimension();

        return new Matrix4f(view)
                .translate((float) (p.getX() - camX), (float) (p.getY() - camY), (float) (p.getZ() - camZ))
                .translate(0.5f, FLOOR_Y, 0.5f)
                .scale(scale)
                .translate(-mesh.sizeX / 2f, 0f, -mesh.sizeZ / 2f);
    }

    private static RenderPipeline blockPipeline(ChunkSectionLayer layer) {
        return switch (layer) {
            case SOLID -> RenderPipelines.SOLID_BLOCK;
            case CUTOUT -> RenderPipelines.CUTOUT_BLOCK;
            case TRANSLUCENT -> RenderPipelines.TRANSLUCENT_BLOCK;
        };
    }

    private static TextureTarget ensureTarget(int width, int height) {
        if (previewTarget == null) {
            previewTarget = new TextureTarget("compactmachines:room_preview", width, height, true, GpuFormat.RGBA8_UNORM);
        } else if (previewTarget.width != width || previewTarget.height != height) {
            previewTarget.resize(width, height);
        }
        return previewTarget;
    }

    private record Entry(BlockPos pos, String roomCode, RoomPreviewMesh mesh) {}

    private static final class State {
        static final State INSTANCE = new State();
        final List<Entry> entries = new ArrayList<>();
        double camX, camY, camZ;
    }
}
