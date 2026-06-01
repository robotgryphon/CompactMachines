package dev.compactmods.machines.shrinking;

import com.mojang.serialization.Codec;
import dev.compactmods.machines.api.room.RoomInstance;
import dev.compactmods.machines.core.CompactMachinesCore;
import dev.compactmods.machines.core.GameRulesHelper;
import dev.compactmods.machines.core.capability.ServerCapability;
import dev.compactmods.machines.shrinking.api.ShrinkingDeviceConfiguration;
import dev.compactmods.machines.shrinking.api.capability.PlayerEntryPointHistoryManager;
import dev.compactmods.machines.shrinking.api.capability.PlayerShrinkingHandler;
import dev.compactmods.machines.shrinking.api.history.RoomEntryPoint;
import dev.compactmods.machines.shrinking.capability.ServerPlayerShrinkingHandler;
import dev.compactmods.machines.shrinking.history.ServerPlayerEntryPointHistoryManager;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.gamerules.GameRule;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Collections;
import java.util.function.Supplier;

import static dev.compactmods.machines.room.Rooms.ATTACHMENT_TYPES;

public class Shrinking {

    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CompactMachinesCore.MOD_ID);
    private static final DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, CompactMachinesCore.MOD_ID);

    private static final DeferredRegister<GameRule<?>> GAME_RULES = DeferredRegister.create(BuiltInRegistries.GAME_RULE, CompactMachinesCore.MOD_ID);

    public static final DeferredItem<PersonalShrinkingDevice> PERSONAL_SHRINKING_DEVICE = ITEMS.register("personal_shrinking_device",
            () -> new PersonalShrinkingDevice(new Item.Properties()
                    .setId(PersonalShrinkingDevice.RESOURCE_KEY)
                    .component(DataComponents.SHRINKING_CONFIG, ShrinkingDeviceConfiguration.DEFAULT_CONFIG)
                    .stacksTo(1)));

    public static final DeferredItem<Item> SHRINKING_MODULE = ITEMS.registerItem("shrinking_module", Item::new);
    public static final DeferredItem<Item> ENLARGING_MODULE = ITEMS.registerItem("enlarging_module", Item::new);

    public static final EntityCapability<PlayerShrinkingHandler, RoomInstance> SHRINK = EntityCapability.create(CompactMachinesCore.identifier("shrink"),
            PlayerShrinkingHandler.class, RoomInstance.class);

    public static final ServerCapability<PlayerEntryPointHistoryManager, Void> HISTORY_MANAGER = ServerCapability.createVoid(
            CompactMachinesCore.identifier("shrinking_history"), PlayerEntryPointHistoryManager.class);

    public static final Supplier<AttachmentType<String>> CURRENT_ROOM_CODE = ATTACHMENT_TYPES.register("current_room_code", () -> AttachmentType
            .<String>builder(() -> null)
            .serialize(Codec.STRING.fieldOf("roomCode"))
            .build());

    public static final Supplier<AttachmentType<RoomEntryPoint>> LAST_ROOM_ENTRYPOINT = ATTACHMENT_TYPES.register("last_entrypoint", () -> AttachmentType.builder(() -> RoomEntryPoint.INVALID)
            .serialize(RoomEntryPoint.CODEC)
            .build());

    // public static final DeferredItem<Item> RESIZING_MODULE = Registries.ITEMS.register("resizing_module", Registries::basicItem);

    public interface GameRules {
        /// For hardcore-style packs. If a shrinking item is successfully used to LEAVE a room,
        /// it will also be damaged. Off by default.
        Identifier DAMAGE_PSD_ITEMS_ON_ROOM_EXIT_KEY = CompactMachinesCore.identifier("damage_psd_on_exit");

        DeferredHolder<GameRule<?>, GameRule<Boolean>> DAMAGE_PSD_ITEMS_ON_ROOM_EXIT = GAME_RULES
                .register(DAMAGE_PSD_ITEMS_ON_ROOM_EXIT_KEY.getPath(), () -> GameRulesHelper.makeBooleanRule(false));

        static void prepare() {
        }
    }

    public interface DataComponents {
        String KEY_SHRINKING_CONFIG = "shrinking_device";

        DeferredHolder<DataComponentType<?>, DataComponentType<ShrinkingDeviceConfiguration>> SHRINKING_CONFIG = DATA_COMPONENTS
                .registerComponentType(KEY_SHRINKING_CONFIG, (builder) -> builder
                        .persistent(ShrinkingDeviceConfiguration.CODEC)
                        .networkSynchronized(ShrinkingDeviceConfiguration.STREAM_CODEC));

        static void prepare() {
        }
    }

    public static void prepare() {
        DataComponents.prepare();
        GameRules.prepare();
    }

    public static void registerContent(IEventBus modBus) {
        ITEMS.register(modBus);
        DATA_COMPONENTS.register(modBus);
        GAME_RULES.register(modBus);

        modBus.addListener((RegisterCapabilitiesEvent caps) -> {
            caps.registerEntity(Shrinking.SHRINK, EntityType.PLAYER, (player, roomInstance) -> {
                if (player instanceof ServerPlayer serverPlayer)
                    return new ServerPlayerShrinkingHandler(serverPlayer, roomInstance);

                return null;
            });

            ServerCapability.registerVoid(HISTORY_MANAGER, (server, ctx)
                    -> new ServerPlayerEntryPointHistoryManager(5, Collections.emptyMap()));
        });

        modBus.addListener((BuildCreativeModeTabContentsEvent addToTabs) -> {
            if (addToTabs.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
                addToTabs.accept(Shrinking.PERSONAL_SHRINKING_DEVICE.get());
            }
        });
    }
}

