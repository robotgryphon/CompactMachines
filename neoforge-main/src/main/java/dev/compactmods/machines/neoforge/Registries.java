package dev.compactmods.machines.neoforge;

import dev.compactmods.compactmachines.api.room.RoomTemplate;
import dev.compactmods.compactmachines.api.room.Rooms;
import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.api.room.upgrade.RoomUpgrade;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.neoforged.neoforge.registries.DeferredRegister;

import static dev.compactmods.machines.api.core.Constants.MOD_ID;

public class Registries {

    // Machines, Walls, Shrinking
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, MOD_ID);

    // UIRegistration
    public static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(BuiltInRegistries.MENU, MOD_ID);

    // MachineRoomUpgrades
    public static final DeferredRegister<RoomUpgrade> UPGRADES = DeferredRegister.create(RoomUpgrade.REG_KEY, MOD_ID);

    // Commands
    public static final DeferredRegister<ArgumentTypeInfo<?, ?>> COMMAND_ARGUMENT_TYPES = DeferredRegister.create(BuiltInRegistries.COMMAND_ARGUMENT_TYPE, MOD_ID);

    // LootFunctions
    public static final DeferredRegister<LootItemFunctionType> LOOT_FUNCS = DeferredRegister.create(BuiltInRegistries.LOOT_FUNCTION_TYPE, MOD_ID);

    public static final DeferredRegister<RoomTemplate> ROOM_TEMPLATES_DR = DeferredRegister.create(Rooms.TEMPLATE_REG_KEY, Constants.MOD_ID);

    public static final Registry<RoomTemplate> ROOM_TEMPLATES = ROOM_TEMPLATES_DR.makeRegistry(b -> {});

    // Villagers
    public static final DeferredRegister<VillagerProfession> VILLAGERS = DeferredRegister.create(BuiltInRegistries.VILLAGER_PROFESSION, Constants.MOD_ID);

    public static final DeferredRegister<PoiType> POINTS_OF_INTEREST = DeferredRegister.create(BuiltInRegistries.POINT_OF_INTEREST_TYPE, Constants.MOD_ID);

    public static void setup() {

    }
}
