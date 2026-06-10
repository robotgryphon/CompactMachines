package dev.compactmods.machines;

import dev.compactmods.machines.api.room.template.RoomTemplate;
import dev.compactmods.machines.client.machine.shader.flag.FlagShader;
import dev.compactmods.machines.core.CompactMachinesCore;
import dev.compactmods.machines.villager.Villagers;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.gamerules.GameRule;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.stream.Stream;

public interface CMRegistries {

	// Machines, Walls, Shrinking
	DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(CompactMachinesCore.MOD_ID);
	DeferredRegister.Items ITEMS = DeferredRegister.createItems(CompactMachinesCore.MOD_ID);

	DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(BuiltInRegistries.CREATIVE_MODE_TAB, CompactMachinesCore.MOD_ID);

	DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, CompactMachinesCore.MOD_ID);

	// UIRegistration
	DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(BuiltInRegistries.MENU, CompactMachinesCore.MOD_ID);

	// Commands
	DeferredRegister<ArgumentTypeInfo<?, ?>> COMMAND_ARGUMENT_TYPES = DeferredRegister.create(BuiltInRegistries.COMMAND_ARGUMENT_TYPE, CompactMachinesCore.MOD_ID);

	DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, CompactMachinesCore.MOD_ID);

	DeferredRegister<GameRule<?>> GAME_RULES = DeferredRegister.create(BuiltInRegistries.GAME_RULE, CompactMachinesCore.MOD_ID);

    DeferredRegister<PoiType> POINTS_OF_INTEREST = DeferredRegister.create(BuiltInRegistries.POINT_OF_INTEREST_TYPE, CompactMachinesCore.MOD_ID);

	DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(BuiltInRegistries.MENU, CompactMachinesCore.MOD_ID);

	DeferredRegister<FlagShader> FLAG_SHADERS = DeferredRegister.create(FlagShader.REGISTRY_KEY, CompactMachinesCore.MOD_ID);

    static void setup(IEventBus modBus) {
		Stream.of(BLOCKS, ITEMS, BLOCK_ENTITIES, CONTAINERS, COMMAND_ARGUMENT_TYPES, GAME_RULES,
                POINTS_OF_INTEREST, Villagers.VILLAGERS, TABS,
				CMDataAttachments.ATTACHMENT_TYPES,
				DATA_COMPONENTS,
				MENUS
		).forEach(r -> r.register(modBus));

		modBus.addListener((DataPackRegistryEvent.NewRegistry newRegistries) -> {
			newRegistries.dataPackRegistry(RoomTemplate.REGISTRY_KEY, RoomTemplate.CODEC, RoomTemplate.CODEC);
			newRegistries.dataPackRegistry(FlagShader.REGISTRY_KEY, FlagShader.CODEC, FlagShader.CODEC);
		});
	}
}
