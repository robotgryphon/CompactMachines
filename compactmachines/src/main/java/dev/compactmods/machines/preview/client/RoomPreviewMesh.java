package dev.compactmods.machines.preview.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.compactmods.machines.preview.RoomPreviewSnapshot;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

/**
 * A precomputed, face-culled voxel mesh for one {@link RoomPreviewSnapshot}, in room-local space
 * (a cube per non-air cell, spanning {@code [x,x+1] × [y,y+1] × [z,z+1]}).
 *
 * <p>Only faces that border an air cell (or the snapshot edge) are emitted, so solid interiors cost
 * nothing — the result is the room's visible surface. Each face's colour is the block's
 * {@link MapColor} multiplied by a fixed per-direction brightness (the classic top-bright /
 * bottom-dark voxel look), baked into the vertex colour so the render pipeline needs no lighting.
 *
 * <p>Building walks the whole volume once and is done off the hot path (only when a room's snapshot
 * version changes — see {@link RoomPreviewMeshCache}); {@link #emit} just streams the cached vertices
 * each frame.
 */
public final class RoomPreviewMesh {

    // Vanilla-style directional shading: up brightest, bottom darkest, sides between.
    private static final float SHADE_DOWN = 0.5f;
    private static final float SHADE_UP = 1.0f;
    private static final float SHADE_NORTH_SOUTH = 0.8f;
    private static final float SHADE_EAST_WEST = 0.6f;

    /** Fallback colour for blocks whose map colour is {@link MapColor#NONE} (glass, etc.). */
    private static final int FALLBACK_RGB = 0x8891A0;

    private final float[] positions; // x,y,z per vertex
    private final int[] colors;      // one packed 0xAARRGGBB per vertex
    public final int sizeX, sizeY, sizeZ;

    private RoomPreviewMesh(float[] positions, int[] colors, int sizeX, int sizeY, int sizeZ) {
        this.positions = positions;
        this.colors = colors;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
    }

    /** The number of vertices; a mesh with none should be skipped by the renderer. */
    public int vertexCount() {
        return colors.length;
    }

    public boolean isEmpty() {
        return colors.length == 0;
    }

    /** {@return the largest of the room's three dimensions in blocks} (for uniform-fit scaling). */
    public int maxDimension() {
        return Math.max(sizeX, Math.max(sizeY, sizeZ));
    }

    /** Streams the cached quads into {@code buffer} at {@code pose}. Room-local coords, origin at corner. */
    public void emit(PoseStack.Pose pose, VertexConsumer buffer) {
        for (int v = 0; v < colors.length; v++) {
            final int c = colors[v];
            buffer.addVertex(pose, positions[v * 3], positions[v * 3 + 1], positions[v * 3 + 2])
                    .setColor(c >> 16 & 0xFF, c >> 8 & 0xFF, c & 0xFF, c >>> 24);
        }
    }

    /** Builds the surface mesh for {@code snapshot}. */
    public static RoomPreviewMesh build(RoomPreviewSnapshot snapshot) {
        final int sx = snapshot.sizeX(), sy = snapshot.sizeY(), sz = snapshot.sizeZ();
        final FloatArrayList positions = new FloatArrayList();
        final IntArrayList colors = new IntArrayList();

        for (int y = 0; y < sy; y++) {
            for (int z = 0; z < sz; z++) {
                for (int x = 0; x < sx; x++) {
                    final BlockState state = snapshot.blockAt(x, y, z);
                    if (state.isAir()) continue;

                    final int base = colorOf(state);
                    for (Direction dir : Direction.values()) {
                        // Cull the face unless the neighbour cell is empty (air or off the edge).
                        if (!snapshot.blockAt(x + dir.getStepX(), y + dir.getStepY(), z + dir.getStepZ()).isAir())
                            continue;
                        emitFace(positions, colors, x, y, z, dir, shade(base, dir));
                    }
                }
            }
        }

        return new RoomPreviewMesh(positions.toFloatArray(), colors.toIntArray(), sx, sy, sz);
    }

    // --- geometry --------------------------------------------------------------------

    private static void emitFace(FloatArrayList pos, IntArrayList col, int x, int y, int z, Direction dir, int color) {
        final float x0 = x, y0 = y, z0 = z, x1 = x + 1, y1 = y + 1, z1 = z + 1;
        switch (dir) {
            case DOWN -> quad(pos, x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1);
            case UP -> quad(pos, x0, y1, z0, x0, y1, z1, x1, y1, z1, x1, y1, z0);
            case NORTH -> quad(pos, x0, y0, z0, x0, y1, z0, x1, y1, z0, x1, y0, z0);
            case SOUTH -> quad(pos, x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1);
            case WEST -> quad(pos, x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0);
            case EAST -> quad(pos, x1, y0, z0, x1, y1, z0, x1, y1, z1, x1, y0, z1);
        }
        for (int i = 0; i < 4; i++) col.add(color);
    }

    private static void quad(FloatArrayList pos,
                             float ax, float ay, float az, float bx, float by, float bz,
                             float cx, float cy, float cz, float dx, float dy, float dz) {
        pos.add(ax); pos.add(ay); pos.add(az);
        pos.add(bx); pos.add(by); pos.add(bz);
        pos.add(cx); pos.add(cy); pos.add(cz);
        pos.add(dx); pos.add(dy); pos.add(dz);
    }

    // --- colour ----------------------------------------------------------------------

    private static int colorOf(BlockState state) {
        final MapColor mapColor = state.getMapColor(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
        int rgb = mapColor.calculateARGBColor(MapColor.Brightness.NORMAL) & 0xFFFFFF;
        if (rgb == 0) rgb = FALLBACK_RGB;
        return rgb;
    }

    private static int shade(int rgb, Direction dir) {
        final float f = switch (dir) {
            case DOWN -> SHADE_DOWN;
            case UP -> SHADE_UP;
            case NORTH, SOUTH -> SHADE_NORTH_SOUTH;
            case WEST, EAST -> SHADE_EAST_WEST;
        };
        final int r = Math.round((rgb >> 16 & 0xFF) * f);
        final int g = Math.round((rgb >> 8 & 0xFF) * f);
        final int b = Math.round((rgb & 0xFF) * f);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }
}
