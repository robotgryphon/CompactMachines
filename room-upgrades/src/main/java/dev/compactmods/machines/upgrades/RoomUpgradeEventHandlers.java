//package dev.compactmods.machines.upgrades;
//
//import dev.compactmods.machines.api.room.RoomInstance;
//import dev.compactmods.machines.api.room.capability.RoomCapabilities;
//import dev.compactmods.machines.api.room.upgrade.event.RoomUpgradeComponentEvent;
//import dev.compactmods.machines.api.room.upgrade.event.level.LevelLoadedUpgradeEventListener;
//import dev.compactmods.machines.api.room.upgrade.event.level.LevelUnloadedUpgradeEventListener;
//import dev.compactmods.machines.api.room.upgrade.event.lifecycle.UpgradeTickedEventListener;
//import dev.compactmods.machines.api.room.upgrade.event.NeoForgeEventHandler;
//import dev.compactmods.machines.api.room.upgrade.event.NeoForgeEventListener;
//import dev.compactmods.machines.room.CMFeatureFlags;
//import net.minecraft.network.chat.Component;
//import net.minecraft.server.MinecraftServer;
//import net.minecraft.server.level.ServerLevel;
//import net.minecraft.util.CommonColors;
//import net.minecraft.world.item.Item;
//import net.minecraft.world.item.ItemStack;
//import net.minecraft.world.item.TooltipFlag;
//import net.neoforged.bus.api.Event;
//import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
//import net.neoforged.neoforge.event.level.LevelEvent;
//import net.neoforged.neoforge.event.tick.LevelTickEvent;
//
//import java.util.HashSet;
//import java.util.Set;
//import java.util.function.Consumer;
//import java.util.stream.Collectors;
//
//public class RoomUpgradeEventHandlers {
//
////    public static void collectUpgradeEvents() {
////        Set<Class<? extends Event>> allEvents = new HashSet<>();
////
////        RoomUpgradesContent.ROOM_UPGRADE_DEFINITIONS.getRegistry()
////                .get()
////                .forEach(upgradeType -> {
////                    var inst = upgradeType.constructor().get();
////                    if (inst instanceof NeoForgeEventListener listener) {
////                        var eventTypes = listener.gatherNeoEvents()
////                                .map(NeoForgeEventHandler::eventType)
////                                .distinct()
////                                .toList();
////
////                        allEvents.addAll(eventTypes);
////                    }
////                });
////
////        allEvents.forEach(RoomUpgradesContent::eventProcessor);
////    }
//
//    private static void doRoomUpgradeLoop(MinecraftServer server, Consumer<RoomInstance> forEach) {
//        final var caps = CompactMachinesServer.caps(server);
//
//        final var registeredRooms = caps.roomRegistrar()
//                .allRooms()
//                .collect(Collectors.toUnmodifiableSet());
//
//        for (var room : registeredRooms) {
//            room.getCapability(RoomCapabilities.ROOM_DATA_ATTACHMENTS)
//                    .getExistingData(CMDataAttachments.UPGRADE_ITEMS)
//                    .ifPresent(i -> forEach.accept(room));
//        }
//    }
//
//    public static void onLevelLoad(final LevelEvent.Load loaded) {
//        if (!CMFeatureFlags.ROOM_UPGRADES.isSubsetOf(loaded.getLevel().enabledFeatures()))
//            return;
//
//        final var server = loaded.getLevel().getServer();
//        if (loaded.getLevel() instanceof ServerLevel serverLevel && CompactDimension.isLevelCompact(serverLevel)) {
//            doRoomUpgradeLoop(server, (room) -> handleBasicEvent(room, LevelLoadedUpgradeEventListener.class));
//        }
//    }
//
//    public static void onLevelUnload(final LevelEvent.Unload loaded) {
//        if (!CMFeatureFlags.ROOM_UPGRADES.isSubsetOf(loaded.getLevel().enabledFeatures()))
//            return;
//
//        final var server = loaded.getLevel().getServer();
//        if (loaded.getLevel() instanceof ServerLevel serverLevel && CompactDimension.isLevelCompact(serverLevel)) {
//            doRoomUpgradeLoop(server, (room) -> handleBasicEvent(room, LevelUnloadedUpgradeEventListener.class));
//        }
//    }
//
//    public static void onLevelTick(LevelTickEvent.Post postTick) {
//        if (!CMFeatureFlags.ROOM_UPGRADES.isSubsetOf(postTick.getLevel().enabledFeatures()))
//            return;
//
//        final var server = postTick.getLevel().getServer();
//        if (postTick.getLevel() instanceof ServerLevel serverLevel && CompactDimension.isLevelCompact(serverLevel)) {
//            doRoomUpgradeLoop(server, (room) -> handleBasicEvent(room, UpgradeTickedEventListener.class));
//        }
//    }
//
//    private static <Evt extends RoomUpgradeComponentEvent> void handleBasicEvent(RoomInstance room, Class<Evt> eventType) {
//        final var upgradeInstances = room.getCapability(RoomCapabilities.UPGRADES)
//                .all()
//                .collect(Collectors.toUnmodifiableSet());
//
//        for (final var upgrade : upgradeInstances) {
//            final var upgrades = upgrade.components();
//            upgrades
//                    .flatMap(ru -> ru.gatherEvents().filter(eventType::isInstance))
//                    .map(eventType::cast)
//                    .forEach(loadHandler -> loadHandler.handle(room));
//        }
//    }
//
//    public static void onTooltips(ItemTooltipEvent evt) {
//        Item.TooltipContext ctx = evt.getContext();
//        Consumer<Component> tooltips = evt.getToolTip()::add;
//        TooltipFlag flags = evt.getFlags();
//
//        ItemStack stack = evt.getItemStack();
//
//        if (stack.has(CMDataComponents.ROOM_TEMPLATE_ID)) {
//            final var template = stack.get(CMDataComponents.ROOM_TEMPLATE_ID);
//            tooltips.accept(Component.literal(template.toString()));
//        }
//
//        if (stack.has(CMDataComponents.BOUND_ROOM_CODE)) {
//            final var roomCode = stack.get(CMDataComponents.BOUND_ROOM_CODE);
//            tooltips.accept(Component.literal(roomCode));
//        }
//
//        if (stack.has(CMDataComponents.UPGRADE_INSTANCE_ID)) {
//            var id = stack.get(CMDataComponents.UPGRADE_INSTANCE_ID);
//            tooltips.accept(Component.literal("ID: " + id).withColor(CommonColors.GRAY));
//        }
//
////        stack.addToTooltip(CMDataComponents.UPGRADE_LIST_COMPONENT,
////                ctx,
////                TooltipDisplay.DEFAULT,
////                tooltips,
////                flags);
//    }
//}