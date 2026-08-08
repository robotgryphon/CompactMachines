package dev.compactmods.machines.dimension;

import dev.compactmods.machines.CMRegistries;
import net.neoforged.neoforge.registries.DeferredBlock;

public class Dimension {

    public static final DeferredBlock<VoidAirBlock> BLOCK_MACHINE_VOID_AIR = CMRegistries.BLOCKS.register("machine_void_air", VoidAirBlock::new);

    public static void prepare() {

    }
}
