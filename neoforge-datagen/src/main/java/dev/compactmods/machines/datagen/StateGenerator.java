package dev.compactmods.machines.datagen;

import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.api.room.RoomSize;
import dev.compactmods.machines.neoforge.machine.Machines;
import dev.compactmods.machines.neoforge.machine.block.LegacySizedCompactMachineBlock;
import dev.compactmods.machines.neoforge.upgrade.MachineRoomUpgrades;
import dev.compactmods.machines.neoforge.wall.Walls;
import net.minecraft.data.DataGenerator;
import net.neoforged.client.model.generators.BlockModelProvider;
import net.neoforged.client.model.generators.BlockStateProvider;
import net.neoforged.client.model.generators.ConfiguredModel;
import net.neoforged.common.data.ExistingFileHelper;

public class StateGenerator extends BlockStateProvider {
    public StateGenerator(DataGenerator gen, ExistingFileHelper exFileHelper) {
        super(gen, Constants.MOD_ID, exFileHelper);
    }

    @Override
    @SuppressWarnings("removal")
    protected void registerStatesAndModels() {
        // Wall block model
        BlockModelProvider models = models();

        var wall = models.cubeAll("block/wall", modLoc("block/wall"));
        simpleBlock(Walls.BLOCK_SOLID_WALL.get(), wall);
        simpleBlock(Walls.BLOCK_BREAKABLE_WALL.get(), wall);

        // New machine block
        final var m = models
                .withExistingParent("block/machine/machine", mcLoc("block/block"))
                .texture("border", modLoc("block/machine/border"))
                .texture("tint", modLoc("block/machine/tint"))
                .texture("overlay", modLoc("block/machine/overlay"))
                .renderType(mcLoc("cutout"))
                .element()
                .allFaces((dir, face) -> face.texture("#border")
                        .uvs(0, 0, 16, 16)
                        .cullface(dir)
                        .end())
                .end()
                .element()
                .allFaces((dir, face) -> face.texture("#tint")
                        .emissivity(2)
                        .uvs(0, 0, 16, 16)
                        .cullface(dir)
                        .tintindex(0)
                        .end())
                .end()
                .element()
                .allFaces((dir, face) -> face.texture("#overlay")
                        .uvs(0, 0, 16, 16)
                        .cullface(dir)
                        .tintindex(1)
                        .end())
                .end();

        simpleBlock(Machines.UNBOUND_MACHINE_BLOCK.get(), ConfiguredModel.builder()
                .modelFile(m)
                .build());

        simpleBlock(Machines.MACHINE_BLOCK.get(), ConfiguredModel.builder()
                .modelFile(m)
                .build());

        // Legacy-sized machines
        for (RoomSize size : RoomSize.values()) {
            String sizeName = size.getName();
            simpleBlock(LegacySizedCompactMachineBlock.getBySize(size), ConfiguredModel.builder()
                    .modelFile(models.cubeAll("block/machine/machine_" + sizeName, modLoc("block/machine/machine_" + sizeName)))
                    .build());
        }

        this.simpleBlock(MachineRoomUpgrades.WORKBENCH_BLOCK.get(), models()
                .cubeTop("block/workbench", modLoc("block/workbench/top"), modLoc("block/workbench/sides")));
    }
}
