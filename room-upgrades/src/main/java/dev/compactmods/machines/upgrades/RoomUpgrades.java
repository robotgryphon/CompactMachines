package dev.compactmods.machines.upgrades;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.compactmods.machines.api.room.capability.RoomCapability;
import dev.compactmods.machines.upgrades.api.RoomUpgradeComponentType;
import dev.compactmods.machines.upgrades.api.RoomUpgradesApi;
import dev.compactmods.machines.upgrades.api.capability.RoomUpgradeCapabilities;
import dev.compactmods.machines.upgrades.api.component.RoomUpgradeComponentList;
import dev.compactmods.machines.core.CompactMachinesCore;
import dev.compactmods.machines.room.CMFeatureFlags;
import dev.compactmods.machines.upgrades.command.RUCommands;
import dev.compactmods.machines.upgrades.command.RoomUpgradesSubcommand;
import dev.compactmods.machines.upgrades.example.TreeCutterUpgradeComponent;
import net.minecraft.commands.Commands;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.ItemTags;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(CompactMachinesCore.MOD_ID)
public class RoomUpgrades {

    interface RURegistries {
        DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, CompactMachinesCore.MOD_ID);

        DeferredRegister<RoomUpgradeComponentType<?>> ROOM_UPGRADE_DEFINITIONS = RoomUpgradesApi.roomUpgradeDR(CompactMachinesCore.MOD_ID);
    }

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<RoomUpgradeComponentList>> UPGRADE_LIST_COMPONENT = RoomUpgrades.RURegistries.DATA_COMPONENTS
            .registerComponentType("room_upgrades", (builder) -> builder
                    .persistent(RoomUpgradeComponentList.CODEC)
                    .networkSynchronized(RoomUpgradeComponentList.STREAM_CODEC));


    public static final DeferredHolder<RoomUpgradeComponentType<?>, RoomUpgradeComponentType<TreeCutterUpgradeComponent>> TREECUTTER = RURegistries.ROOM_UPGRADE_DEFINITIONS
            .register("tree_cutter", () -> RoomUpgradeComponentType.builder(TreeCutterUpgradeComponent::new, TreeCutterUpgradeComponent.CODEC)
                    .requiredFeatures(CMFeatureFlags.ROOM_UPGRADES)
                    .itemPredicate(stack -> stack.is(ItemTags.AXES))
                    .build());
//
//    DeferredHolder<RoomUpgradeComponentType<?>, RoomUpgradeComponentType<ChunkLoaderUpgradeComponent>> CHUNK_LOADER = ROOM_UPGRADE_DEFINITIONS
//            .register("chunk_loader", () -> RoomUpgradeComponentType.builder(ChunkLoaderUpgradeComponent::new, ChunkLoaderUpgradeComponent.CODEC)
//                    .requiredFeatures(CMFeatureFlags.ROOM_UPGRADES)
//                    .build());

    //    interface Menus {
//        DeferredHolder<MenuType<?>, MenuType<RoomUpgradeMenu>> ROOM_UPGRADES = Rooms.CONTAINERS.register("room_upgrades",
//                () -> IMenuTypeExtension.create(RoomUpgradeMenu::createClientMenu));
//
//        static void prepare() {
//        }
//    }

    public RoomUpgrades(IEventBus modBus) {

        RURegistries.ROOM_UPGRADE_DEFINITIONS.makeRegistry(builder -> {
            builder.sync(true);
        });

        registerEvents(modBus);

        modBus.addListener(RoomUpgrades::onRegisterCapabilities);

        RURegistries.DATA_COMPONENTS.register(modBus);
        RURegistries.ROOM_UPGRADE_DEFINITIONS.register(modBus);
    }

    static void registerEvents(IEventBus modBus) {
        modBus.addListener(RoomUpgrades::commonSetup);

        modBus.addListener(RoomUpgrades::onCommandsRegister);
        
//        NeoForge.EVENT_BUS.addListener(RoomUpgradeEventHandlers::onLevelLoad);
//        NeoForge.EVENT_BUS.addListener(RoomUpgradeEventHandlers::onLevelUnload);
//        NeoForge.EVENT_BUS.addListener(RoomUpgradeEventHandlers::onLevelTick);
//        NeoForge.EVENT_BUS.addListener(RoomUpgradeEventHandlers::onTooltips);
    }

    private static void commonSetup(FMLCommonSetupEvent evt) {
//        evt.enqueueWork(RoomUpgradeEventHandlers::collectUpgradeEvents);
    }

    private static void onCommandsRegister(final RegisterCommandsEvent event) {
        CompactMachinesCore.CM_COMMAND_ROOT.then(RoomUpgradesSubcommand.make());
    }

    static void onRegisterCapabilities(RegisterCapabilitiesEvent r) {

    }
}
