package dev.compactmods.machines.datagen;

import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.neoforge.machine.Machines;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ItemModelGenerator extends ItemModelProvider {

    public ItemModelGenerator(PackOutput packOutput, ExistingFileHelper existingFileHelper) {
        super(packOutput, Constants.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        withExistingParent(Machines.BOUND_MACHINE_BLOCK_ITEM.getId().getPath(), modLoc("block/machine/machine"));
        withExistingParent(Machines.UNBOUND_MACHINE_BLOCK_ITEM.getId().getPath(), modLoc("block/machine/machine"));
        
        withExistingParent("solid_wall", modLoc("block/wall"));
        withExistingParent("wall", modLoc("block/wall"));

        withExistingParent("personal_shrinking_device", mcLoc("item/generated"))
                .texture("layer0", modLoc("item/personal_shrinking_device"));

        withExistingParent("tunnel", mcLoc("item/generated"))
                .texture("layer0", modLoc("item/tunnel"));

//        withExistingParent(MachineRoomUpgrades.ROOM_UPGRADE.getId().toString(), mcLoc("item/generated"))
//                .texture("layer0", modLoc("upgrades/chunkloader"));
//
//        withExistingParent(MachineRoomUpgrades.WORKBENCH_ITEM.getId().getPath(), modLoc("block/workbench"));
    }
}
