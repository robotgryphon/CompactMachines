package dev.compactmods.machines.datagen.base.tags;

import dev.compactmods.machines.api.CompactMachines;
import dev.compactmods.machines.core.machine.MachineConstants;
import dev.compactmods.machines.dimension.Dimension;
import dev.compactmods.machines.machine.Machines;
import dev.compactmods.machines.room.Rooms;
import dev.compactmods.machines.villager.Villagers;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;

import java.util.concurrent.CompletableFuture;

public class BlockTagGenerator extends BlockTagsProvider {

    public BlockTagGenerator(PackOutput packOut, CompletableFuture<HolderLookup.Provider> lookup) {
        super(packOut, lookup, CompactMachines.MOD_ID);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        final var breakableWall = Rooms.Blocks.BREAKABLE_WALL.get();
        final var solidWall = Rooms.Blocks.SOLID_WALL.get();
        final var machineBlock = Machines.Blocks.MACHINE.get();
        final var voidAir = Dimension.BLOCK_MACHINE_VOID_AIR.get();
        final var spatialWorkbench = Villagers.SPATIAL_WORKBENCH.get();

        tag(MachineConstants.MACHINE_BLOCK)
                .add(machineBlock);

        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(breakableWall)
                .add(machineBlock);

        tag(BlockTags.NEEDS_IRON_TOOL)
                .add(breakableWall)
                .add(machineBlock);

        tag(Tags.Blocks.RELOCATION_NOT_SUPPORTED)
                .add(machineBlock)
                .add(solidWall)
                .add(voidAir);

        tag(Tags.Blocks.VILLAGER_JOB_SITES)
                .add(spatialWorkbench);
    }
}
