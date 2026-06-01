package dev.compactmods.machines.client.machine.render;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

/**
 * Snapshot taken from a {@link dev.compactmods.machines.machine.block.CompactMachineBlockEntity}
 * each frame by {@link CompactMachineRenderer}. Holds only what the submit pass needs to draw
 * the optional shader overlay — keeps the BER deferral pipeline cheap.
 */
public class MachineRenderState extends BlockEntityRenderState {

    /** Resolved shader id ({@code null} = no overlay; falls back to the chunk-baked tinted glass). */
    public @Nullable Identifier shaderId;

    /** Smoothed game-time in ticks ({@code gameTime + partialTick}). Drives shader animation. */
    public float gameTime;

    /**
     * Bitmask of directions whose neighbour block is another compact machine.
     * Indexed by {@code Direction#get3DDataValue()}: bit 0 = DOWN, 1 = UP,
     * 2 = NORTH, 3 = SOUTH, 4 = WEST, 5 = EAST. The BER skips overlay quads
     * on those faces so the rainbow doesn't show up between touching cubes.
     */
    public int neighborMachineMask;
}
