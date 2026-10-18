package dev.compactmods.machines.machine.client;

import dev.compactmods.machines.machine.ui.MachineUIMenu;
import dev.compactmods.machines.room.preview.client.gui.RoomPreviewPanel;
import dev.robotgryphon.screenlib.ui.api.UiScreenBuilder;
import dev.robotgryphon.screenlib.ui.api.UiScreens;
import dev.robotgryphon.screenlib.ui.api.layout.HorizontalAnchor;
import dev.robotgryphon.screenlib.ui.api.layout.Placement;
import dev.robotgryphon.screenlib.ui.api.layout.VerticalAnchor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/**
 * The machine screen, built with screenlib. A machine bound to a room shows its live room preview
 * panel (with the core in the panel's corner); otherwise an info panel describes the core — e.g. the
 * template a pending room will be created from. Register with {@link UiScreens#register} as the
 * {@link MachineUIMenu} screen.
 *
 * <p>Screenlib's menu screens don't draw or handle slots, so the core is inserted/removed through the
 * block itself (use a core on it / sneak-use with an empty hand).
 */
public final class MachineUI {

    private MachineUI() {}

    public static UiScreenBuilder create(MachineUIMenu menu, Inventory inventory, Component title) {
        final var builder = UiScreens.builder(title);
        final var placement = Placement.anchored(HorizontalAnchor.CENTER, VerticalAnchor.CENTER);

        menu.currentRoom().ifPresentOrElse(
                room -> builder.panel(placement, host -> new RoomPreviewPanel(host, room, () -> core(menu))),
                () -> builder.panel(placement, host -> new MachineInfoPanel(menu)));
        return builder;
    }

    private static ItemStack core(MachineUIMenu menu) {
        final var slot = menu.getCoreSlot();
        return slot != null ? slot.getItem() : ItemStack.EMPTY;
    }
}
