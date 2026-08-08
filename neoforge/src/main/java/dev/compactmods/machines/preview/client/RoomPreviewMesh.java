package dev.compactmods.machines.preview.client;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import dev.compactmods.machines.core.CompactMachinesCore;
import dev.compactmods.machines.preview.RoomPreviewSnapshot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockQuadOutput;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

/**
 * A baked, textured preview of a room: the room's real block models tessellated (via
 * {@link ModelBlockRenderer}) into GPU vertex buffers, one per {@link ChunkSectionLayer}
 * (SOLID/CUTOUT/TRANSLUCENT), in <em>room-local</em> space (unit cubes at {@code (x,y,z)}). The
 * per-machine placement transform is applied at draw time by {@link RoomPreviewRenderer}, so one
 * baked mesh serves every machine bound to that room.
 *
 * <p>Baking runs on the render thread (it touches the model manager and GPU) and is cached per room
 * version by {@link RoomPreviewMeshCache}; {@link #close()} frees the GPU buffers when a newer
 * snapshot supersedes it.
 */
public final class RoomPreviewMesh {

    public final int sizeX, sizeY, sizeZ;
    private final Map<ChunkSectionLayer, LayerMesh> layers;

    /** One layer's uploaded geometry: a vertex buffer plus the index count for the shared quad indices. */
    public record LayerMesh(GpuBuffer vertexBuffer, int indexCount) {}

    private RoomPreviewMesh(int sizeX, int sizeY, int sizeZ, Map<ChunkSectionLayer, LayerMesh> layers) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        this.layers = layers;
    }

    public int maxDimension() {
        return Math.max(sizeX, Math.max(sizeY, sizeZ));
    }

    public boolean isEmpty() {
        return layers.isEmpty();
    }

    public @Nullable LayerMesh layer(ChunkSectionLayer layer) {
        return layers.get(layer);
    }

    /** Frees the GPU buffers. Call when this mesh is evicted/superseded. */
    public void close() {
        for (LayerMesh mesh : layers.values())
            mesh.vertexBuffer().close();
        layers.clear();
    }

    /** Bakes {@code snapshot}'s block models into per-layer GPU buffers. Render thread only. */
    public static RoomPreviewMesh build(RoomPreviewSnapshot snapshot) {
        final int sx = snapshot.sizeX(), sy = snapshot.sizeY(), sz = snapshot.sizeZ();

        final var mc = Minecraft.getInstance();
        final var modelSet = mc.getModelManager().getBlockStateModelSet();
        final var level = new SnapshotBlockGetter(snapshot);
        final var renderer = new ModelBlockRenderer(true /*AO*/, true /*cull internal faces*/, mc.getBlockColors());

        final Map<ChunkSectionLayer, ByteBufferBuilder> scratch = new EnumMap<>(ChunkSectionLayer.class);
        final Map<ChunkSectionLayer, BufferBuilder> builders = new EnumMap<>(ChunkSectionLayer.class);

        // Route each baked quad to its own layer's buffer (a block may emit into several layers).
        final BlockQuadOutput out = (x, y, z, quad, instance) ->
                layerBuilder(scratch, builders, quad.materialInfo().layer()).putBlockBakedQuad(x, y, z, quad, instance);
        final BlockQuadOutput forcedSolid = (x, y, z, quad, instance) ->
                layerBuilder(scratch, builders, ChunkSectionLayer.SOLID).putBlockBakedQuad(x, y, z, quad, instance);

        final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int y = 0; y < sy; y++) {
            for (int z = 0; z < sz; z++) {
                for (int x = 0; x < sx; x++) {
                    final BlockState state = snapshot.blockAt(x, y, z);
                    if (state.isAir() || state.getRenderShape() != RenderShape.MODEL) continue;
                    pos.set(x, y, z);
                    renderer.tesselateBlock(
                            ModelBlockRenderer.forceOpaque(false, state) ? forcedSolid : out,
                            x, y, z, level, pos, state, modelSet.get(state), state.getSeed(pos));
                }
            }
        }

        final Map<ChunkSectionLayer, LayerMesh> result = new EnumMap<>(ChunkSectionLayer.class);
        for (var entry : builders.entrySet()) {
            final MeshData mesh = entry.getValue().build();
            if (mesh == null) continue;
            try (mesh) {
                final GpuBuffer vb = RenderSystem.getDevice().createBuffer(
                        () -> CompactMachinesCore.dotPrefix("room_preview_verts"),
                        GpuBuffer.USAGE_COPY_DST | GpuBuffer.USAGE_VERTEX, mesh.vertexBuffer());
                result.put(entry.getKey(), new LayerMesh(vb, mesh.drawState().indexCount()));
            }
        }
        for (ByteBufferBuilder b : scratch.values())
            b.close();

        return new RoomPreviewMesh(sx, sy, sz, result);
    }

    private static BufferBuilder layerBuilder(Map<ChunkSectionLayer, ByteBufferBuilder> scratch,
                                              Map<ChunkSectionLayer, BufferBuilder> builders, ChunkSectionLayer layer) {
        return builders.computeIfAbsent(layer, l -> {
            final var bb = new ByteBufferBuilder(l.bufferSize());
            scratch.put(l, bb);
            return new BufferBuilder(bb, PrimitiveTopology.QUADS, l.vertexFormat());
        });
    }
}
