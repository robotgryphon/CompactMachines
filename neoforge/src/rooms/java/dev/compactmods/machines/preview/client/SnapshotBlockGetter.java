package dev.compactmods.machines.preview.client;

import dev.compactmods.machines.preview.RoomPreviewSnapshot;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import org.jspecify.annotations.Nullable;

/**
 * A read-only, fully-lit virtual level over a {@link RoomPreviewSnapshot}, in room-local coordinates
 * (the snapshot's {@code (x,y,z)} indices). It's just enough of a {@link BlockAndTintGetter} for
 * {@link net.minecraft.client.renderer.block.ModelBlockRenderer} to bake the room's block models:
 * neighbour states drive face culling, and brightness is pinned to full so the mesh isn't rendered
 * black (the snapshot carries no light data). No block entities, no biome tint.
 */
public final class SnapshotBlockGetter implements BlockAndTintGetter {

    private final RoomPreviewSnapshot snapshot;

    public SnapshotBlockGetter(RoomPreviewSnapshot snapshot) {
        this.snapshot = snapshot;
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        return snapshot.blockAt(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        return getBlockState(pos).getFluidState();
    }

    @Override
    public @Nullable BlockEntity getBlockEntity(BlockPos pos) {
        return null;
    }

    @Override
    public int getHeight() {
        return snapshot.sizeY();
    }

    @Override
    public int getMinY() {
        return 0;
    }

    @Override
    public CardinalLighting cardinalLighting() {
        return CardinalLighting.DEFAULT;
    }

    @Override
    public LevelLightEngine getLightEngine() {
        return LevelLightEngine.EMPTY;
    }

    @Override
    public int getBlockTint(BlockPos pos, ColorResolver color) {
        return -1; // white: no biome tint (tinted blocks render untinted)
    }

    // Full-bright: LevelLightEngine.EMPTY reports 0 everywhere, which would bake a black mesh.
    @Override
    public int getBrightness(LightLayer layer, BlockPos pos) {
        return 15;
    }

    @Override
    public int getRawBrightness(BlockPos pos, int darkening) {
        return 15;
    }
}
