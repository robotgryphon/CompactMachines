package dev.compactmods.machines.neoforge;

import dev.compactmods.compactmachines.api.room.RoomTemplate;
import dev.compactmods.compactmachines.api.room.Rooms;
import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.api.room.upgrade.RoomUpgrade;
import dev.compactmods.machines.neoforge.shrinking.Shrinking;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import static dev.compactmods.machines.api.core.Constants.MOD_ID;

public class Registries {

    // Machines, Walls, Shrinking
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);

    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(BuiltInRegistries.CREATIVE_MODE_TAB, MOD_ID);

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, MOD_ID);

    // UIRegistration
    public static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(BuiltInRegistries.MENU, MOD_ID);

    // MachineRoomUpgrades
    public static Registry<RoomUpgrade> UPGRADES;

    // Commands
    public static final DeferredRegister<ArgumentTypeInfo<?, ?>> COMMAND_ARGUMENT_TYPES = DeferredRegister.create(BuiltInRegistries.COMMAND_ARGUMENT_TYPE, MOD_ID);

    // LootFunctions
    public static final DeferredRegister<LootItemFunctionType> LOOT_FUNCS = DeferredRegister.create(BuiltInRegistries.LOOT_FUNCTION_TYPE, MOD_ID);

    public static Registry<RoomTemplate> ROOM_TEMPLATES;

    // Villagers
    public static final DeferredRegister<VillagerProfession> VILLAGERS = DeferredRegister.create(BuiltInRegistries.VILLAGER_PROFESSION, Constants.MOD_ID);

    public static final DeferredRegister<PoiType> POINTS_OF_INTEREST = DeferredRegister.create(BuiltInRegistries.POINT_OF_INTEREST_TYPE, Constants.MOD_ID);

    public static void setup(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        CONTAINERS.register(modBus);
        COMMAND_ARGUMENT_TYPES.register(modBus);
        LOOT_FUNCS.register(modBus);
        VILLAGERS.register(modBus);
        // Villagers.TRADES.register(bus);
        POINTS_OF_INTEREST.register(modBus);
        TABS.register(modBus);

        modBus.addListener((DataPackRegistryEvent.NewRegistry newRegistries) -> {
            newRegistries.dataPackRegistry(Rooms.TEMPLATE_REG_KEY, RoomTemplate.CODEC, RoomTemplate.CODEC);
        });

        modBus.addListener((BuildCreativeModeTabContentsEvent addToTabs) -> {
            if(addToTabs.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
                addToTabs.accept(Shrinking.PERSONAL_SHRINKING_DEVICE.get());
            }
        });
    }
}
