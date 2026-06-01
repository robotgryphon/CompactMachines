package dev.compactmods.machines.upgrades;

import dev.compactmods.machines.api.room.capability.RoomCapabilities;
import dev.compactmods.machines.api.room.capability.RoomCapability;
import dev.compactmods.machines.api.room.upgrade.RoomUpgradeComponentType;
import dev.compactmods.machines.api.room.upgrade.RoomUpgradesApi;
import dev.compactmods.machines.api.room.upgrade.capability.RoomUpgradeCapabilities;
import dev.compactmods.machines.api.room.upgrade.component.RoomUpgradeComponentList;
import dev.compactmods.machines.core.CompactMachinesCore;
import dev.compactmods.machines.upgrades.event.NeoForgeServerEventProcessor;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Map;

@Mod(CompactMachinesCore.MOD_ID)
public class RoomUpgrades {

    interface RURegistries {
        DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, CompactMachinesCore.MOD_ID);

        DeferredRegister<RoomUpgradeComponentType<?>> ROOM_UPGRADE_DEFINITIONS = RoomUpgradesApi.roomUpgradeDR(CompactMachinesCore.MOD_ID);
    }

    static Map<Class<? extends Event>, NeoForgeServerEventProcessor<?>> EVENT_PROCESSORS = new Reference2ObjectArrayMap<>();

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<RoomUpgradeComponentList>> UPGRADE_LIST_COMPONENT = RoomUpgrades.RURegistries.DATA_COMPONENTS
            .registerComponentType("room_upgrades", (builder) -> builder
                    .persistent(RoomUpgradeComponentList.CODEC)
                    .networkSynchronized(RoomUpgradeComponentList.STREAM_CODEC));


//    DeferredHolder<RoomUpgradeComponentType<?>, RoomUpgradeComponentType<TreeCutterUpgradeComponent>> TREECUTTER = ROOM_UPGRADE_DEFINITIONS
//            .register("tree_cutter", () -> RoomUpgradeComponentType.builder(TreeCutterUpgradeComponent::new, TreeCutterUpgradeComponent.CODEC)
//                    .requiredFeatures(CMFeatureFlags.ROOM_UPGRADES)
//                    .itemPredicate(stack -> stack.is(ItemTags.AXES))
//                    .build());
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

//        NeoForge.EVENT_BUS.addListener(RoomUpgradeEventHandlers::onLevelLoad);
//        NeoForge.EVENT_BUS.addListener(RoomUpgradeEventHandlers::onLevelUnload);
//        NeoForge.EVENT_BUS.addListener(RoomUpgradeEventHandlers::onLevelTick);
//        NeoForge.EVENT_BUS.addListener(RoomUpgradeEventHandlers::onTooltips);
    }

    private static void commonSetup(FMLCommonSetupEvent evt) {
//        evt.enqueueWork(RoomUpgradeEventHandlers::collectUpgradeEvents);
    }

    static void onRegisterCapabilities(RegisterCapabilitiesEvent r) {
        RoomCapability.register(RoomUpgradeCapabilities.UPGRADE_DATA_ATTACHMENTS, (server, roomCode, upgradeId)
                -> new RoomUpgradeDataAttachments(server, new RoomUpgradeInstanceKey(roomCode, upgradeId)));;

        RoomCapability.register(RoomUpgradeCapabilities.UPGRADES, (server, roomCode, _) -> {
            var reg = RoomCapabilities.REGISTRY.getCapability(server);
            if(reg == null)
                return null;

            return reg.get(roomCode)
                    .map(inst -> inst.getCapability(RoomUpgradeCapabilities.UPGRADES))
                    .orElse(null);
        });
    }

    @SuppressWarnings("unchecked")
    static <TEvt extends Event> NeoForgeServerEventProcessor<TEvt> eventProcessor(final MinecraftServer server, final Class<TEvt> event) {
        return (NeoForgeServerEventProcessor<TEvt>) EVENT_PROCESSORS.computeIfAbsent(event, _ -> {
            var processor = new NeoForgeServerEventProcessor<>(server, event);
            NeoForge.EVENT_BUS.addListener(event, processor::process);
            return processor;
        });
    }
}
