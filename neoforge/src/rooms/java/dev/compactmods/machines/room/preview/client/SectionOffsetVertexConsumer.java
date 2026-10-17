package dev.compactmods.machines.room.preview.client;

import com.mojang.blaze3d.vertex.VertexConsumer;

/**
 * Re-adds the 16-block section origin that {@link net.minecraft.client.renderer.block.FluidRenderer}
 * strips from vertex positions ({@code pos & 15}) — it assumes it's writing a single chunk section,
 * but a room preview mesh is one buffer in room-local space and rooms can exceed 16 blocks.
 */
final class SectionOffsetVertexConsumer implements VertexConsumer {

    private VertexConsumer delegate;
    private float offsetX, offsetY, offsetZ;

    /** Point at the layer buffer for the block at room-local {@code (x, y, z)}. */
    SectionOffsetVertexConsumer target(VertexConsumer delegate, int x, int y, int z) {
        this.delegate = delegate;
        this.offsetX = x & ~15;
        this.offsetY = y & ~15;
        this.offsetZ = z & ~15;
        return this;
    }

    @Override
    public VertexConsumer addVertex(float x, float y, float z) {
        delegate.addVertex(x + offsetX, y + offsetY, z + offsetZ);
        return this;
    }

    @Override
    public void addVertex(float x, float y, float z, int color, float u, float v, int overlayCoords, int lightCoords,
                          float nx, float ny, float nz) {
        delegate.addVertex(x + offsetX, y + offsetY, z + offsetZ, color, u, v, overlayCoords, lightCoords, nx, ny, nz);
    }

    @Override
    public VertexConsumer setColor(int r, int g, int b, int a) {
        delegate.setColor(r, g, b, a);
        return this;
    }

    @Override
    public VertexConsumer setColor(int color) {
        delegate.setColor(color);
        return this;
    }

    @Override
    public VertexConsumer setUv(float u, float v) {
        delegate.setUv(u, v);
        return this;
    }

    @Override
    public VertexConsumer setUv1(int u, int v) {
        delegate.setUv1(u, v);
        return this;
    }

    @Override
    public VertexConsumer setUv2(int u, int v) {
        delegate.setUv2(u, v);
        return this;
    }

    @Override
    public VertexConsumer setUv3(float u, float v) {
        delegate.setUv3(u, v);
        return this;
    }

    @Override
    public VertexConsumer setNormal(float x, float y, float z) {
        delegate.setNormal(x, y, z);
        return this;
    }

    @Override
    public VertexConsumer setLineWidth(float width) {
        delegate.setLineWidth(width);
        return this;
    }
}
