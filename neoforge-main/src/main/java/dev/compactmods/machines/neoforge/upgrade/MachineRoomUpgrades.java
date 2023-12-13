package dev.compactmods.machines.neoforge.upgrade;

import dev.compactmods.machines.api.room.upgrade.RoomUpgrade;
import dev.compactmods.machines.neoforge.Registries;
import dev.compactmods.machines.neoforge.room.upgrade.RoomUpgradeWorkbench;
import dev.compactmods.machines.neoforge.room.upgrade.RoomUpgradeWorkbenchEntity;
import net.minecraft.core.Registry;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.RegistryBuilder;

public class MachineRoomUpgrades {

    public static final Registry<RoomUpgrade> REGISTRY = Registries.UPGRADES.makeRegistry(RegistryBuilder::create);

    // ================================================================================================================

    public static final DeferredHolder<Item, RoomUpgradeItem> ROOM_UPGRADE = Registries.ITEMS.register("room_upgrade", () -> new RoomUpgradeItem(new Item.Properties()
            .stacksTo(1)));

    public static final DeferredHolder<Block, RoomUpgradeWorkbench> WORKBENCH_BLOCK = Registries.BLOCKS.register("workbench", () ->
            new RoomUpgradeWorkbench(BlockBehaviour.Properties.of()
                    .requiresCorrectToolForDrops()
                    .lightLevel(state -> 3)));

    public static final DeferredHolder<Item, BlockItem> WORKBENCH_ITEM = Registries.ITEMS.register("workbench", () ->
            new BlockItem(WORKBENCH_BLOCK.get(), new Item.Properties()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RoomUpgradeWorkbenchEntity>> ROOM_UPDATE_ENTITY = Registries.BLOCK_ENTITIES.register(
            "workbench", () -> BlockEntityType.Builder.of(RoomUpgradeWorkbenchEntity::new, WORKBENCH_BLOCK.get())
                    .build(null));

    public static void prepare() {

    }
}
