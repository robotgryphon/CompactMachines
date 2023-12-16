package dev.compactmods.machines.neoforge.client;

import dev.compactmods.compactmachines.api.room.Rooms;
import dev.compactmods.machines.machine.item.ICompactMachineItem;
import dev.compactmods.machines.neoforge.CompactMachines;
import dev.compactmods.machines.neoforge.machine.item.UnboundCompactMachineItem;
import dev.compactmods.machines.neoforge.shrinking.Shrinking;
import dev.compactmods.machines.neoforge.wall.Walls;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.registries.DeferredHolder;

import static dev.compactmods.machines.neoforge.Registries.TABS;

public interface CreativeTabs {

    DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = TABS.register("main", () -> CreativeModeTab.builder()
            .icon(() -> {
                final var ub = UnboundCompactMachineItem.unbound();
                ICompactMachineItem.setColor(ub, CompactMachines.BRAND_MACHINE_COLOR);
                return ub;
            })
            .title(Component.translatable("itemGroup.compactmachines"))
            .displayItems(CreativeTabs::fillItems)
            .build());

    static void fillItems(CreativeModeTab.ItemDisplayParameters params, CreativeModeTab.Output output) {
        output.accept(Shrinking.PERSONAL_SHRINKING_DEVICE.get(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        output.accept(Walls.ITEM_BREAKABLE_WALL.get(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);

        final var lookup = params.holders().lookupOrThrow(Rooms.TEMPLATE_REG_KEY);
        final var machines = lookup.listElements()
                .map(k -> UnboundCompactMachineItem.forTemplate(k.key().location(), k.value()))
                .toList();

        output.acceptAll(machines, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
    }

    static void prepare() {}
}
