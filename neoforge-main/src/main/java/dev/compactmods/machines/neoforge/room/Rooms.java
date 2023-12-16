package dev.compactmods.machines.neoforge.room;

import dev.compactmods.machines.neoforge.Registries;
import dev.compactmods.machines.neoforge.room.ui.MachineRoomMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;

public class Rooms {

    public static final DeferredHolder<MenuType<?>, MenuType<MachineRoomMenu>> MACHINE_MENU = Registries.CONTAINERS.register("machine",
            () -> IMenuTypeExtension.create(MachineRoomMenu::createRoomMenu));

    public static void prepare() {
    }
}
