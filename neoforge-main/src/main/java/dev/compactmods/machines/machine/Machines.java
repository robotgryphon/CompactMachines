package dev.compactmods.machines.machine;

import dev.compactmods.machines.api.CompactMachines;
import dev.compactmods.machines.api.component.CMDataComponents;
import dev.compactmods.machines.api.machine.MachineConstants;
import dev.compactmods.machines.api.room.template.RoomTemplate;
import dev.compactmods.machines.CMRegistries;
import dev.compactmods.machines.machine.block.CompactMachineBlock;
import dev.compactmods.machines.machine.block.CompactMachineBlockEntity;
import dev.compactmods.machines.machine.item.BoundCompactMachineItem;
import dev.compactmods.machines.machine.ui.MachineUIMenu;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public interface Machines {
    UnaryOperator<BlockBehaviour.Properties> MACHINE_BLOCK_PROPS = props -> props
            .instrument(NoteBlockInstrument.COW_BELL)
            .pushReaction(PushReaction.IGNORE)
            .sound(SoundType.METAL)
            .strength(8.0F, 20.0F)
            .requiresCorrectToolForDrops();

    Supplier<Item.Properties> MACHINE_ITEM_PROPS = Item.Properties::new;

    interface Blocks {
        ResourceKey<Block> MACHINE_KEY = ResourceKey.create(Registries.BLOCK, CompactMachines.identifier("machine"));

        DeferredBlock<CompactMachineBlock> MACHINE = CMRegistries.BLOCKS.registerBlock("machine", props ->
                new CompactMachineBlock(MACHINE_BLOCK_PROPS.apply(props).setId(MACHINE_KEY)));

        static void prepare() {
        }
    }

    interface Items {
        DeferredItem<BoundCompactMachineItem> MACHINE = CMRegistries.ITEMS.register("machine",
                () -> new BoundCompactMachineItem(MACHINE_ITEM_PROPS.get()
                        .setId(ResourceKey.create(Registries.ITEM, CompactMachines.identifier("machine")))
                        .overrideDescription(BoundCompactMachineItem.FALLBACK_ID)));

        static void prepare() {
        }

        static ItemStack boundToRoom(String roomCode) {
            ItemStack stack = new ItemStack(net.minecraft.world.item.Items.PAPER);
            stack.set(CMDataComponents.BOUND_ROOM_CODE, roomCode);
            return stack;
        }

        static ItemStack forNewRoom(Holder.Reference<RoomTemplate> templateHolder) {
            var template = templateHolder.value();

            final var stack = new ItemStack(net.minecraft.world.item.Items.PAPER);
            stack.set(CMDataComponents.ROOM_TEMPLATE_ID, templateHolder.key().identifier());
            stack.set(CMDataComponents.MACHINE_COLOR, template.defaultMachineColor());
            return stack;
        }
    }

    interface BlockEntities {
        DeferredHolder<BlockEntityType<?>, BlockEntityType<CompactMachineBlockEntity>> MACHINE = CMRegistries.BLOCK_ENTITIES
                .register(MachineConstants.MACHINE_IDENTIFIER.getPath(),
                        () -> new BlockEntityType<>(CompactMachineBlockEntity::new, Blocks.MACHINE.get()));

        static void prepare() {
        }
    }

    DeferredHolder<MenuType<?>, MenuType<MachineUIMenu>> MACHINE_UI_MENU = CMRegistries.MENUS.register("machine_ui",
            () -> IMenuTypeExtension.create(MachineUIMenu::new));

    static void prepare() {
        Blocks.prepare();
        Items.prepare();
        BlockEntities.prepare();
    }

    static void registerEvents(IEventBus modBus) {

    }
}
