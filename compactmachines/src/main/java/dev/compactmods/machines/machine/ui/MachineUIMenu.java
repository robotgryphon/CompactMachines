package dev.compactmods.machines.machine.ui;

import dev.compactmods.machines.CMDataComponents;
import dev.compactmods.machines.api.room.template.RoomTemplate;
import dev.compactmods.machines.api.room.template.RoomTemplateHelper;
import dev.compactmods.machines.machine.Machines;
import dev.compactmods.machines.machine.block.CompactMachineBlockEntity;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public class MachineUIMenu extends AbstractContainerMenu {
    private final GlobalPos machinePos;
    private final @Nullable CompactMachineBlockEntity machine;

    @ApiStatus.Internal
    public MachineUIMenu(int containerId, Player player, GlobalPos machinePos) {
        super(Machines.MACHINE_UI_MENU.get(), containerId);
        this.machinePos = machinePos;

        final var inv = player.getInventory();
        this.addStandardInventorySlots(inv, 0, 84);
//        this.addInventoryHotbarSlots(inv, 0, 112);

        final var level = player.level();
        if(level.getBlockEntity(machinePos.pos()) instanceof CompactMachineBlockEntity tile) {
            this.machine = tile;

            final var coreHandler = tile.coreHandler();
            final var coreSlot = new ResourceHandlerSlot(coreHandler, coreHandler::set, 0, 10, 10);
            this.addSlot(coreSlot);
        } else {
            this.machine = null;
        }
    }

    public MachineUIMenu(int containerId, Inventory playerInv, FriendlyByteBuf extraData) {
        this(containerId, playerInv.player, extraData.readGlobalPos());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int i) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        if(machinePos == null)
            return false;

        return player.isWithinBlockInteractionRange(machinePos.pos(), 5);
    }

    public Optional<RoomTemplate> currentTemplate() {
        final var currentCore = machine.coreHandler().getResource(0);
        if(currentCore.isEmpty())
            return Optional.empty();

        final var tid = currentCore.get(CMDataComponents.ROOM_TEMPLATE_ID);
        if(tid == null)
            return Optional.empty();

        return RoomTemplateHelper.getTemplateOptional(machine.getLevel().registryAccess(), tid);
    }
}
