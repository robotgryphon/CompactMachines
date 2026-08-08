package dev.compactmods.machines.upgrades.storage;

import dev.compactmods.machines.api.room.capability.RoomCapability;
import dev.compactmods.machines.core.CompactMachinesCore;
import net.minecraft.server.MinecraftServer;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Room capabilities exposed by the storage system.
 *
 * <p>{@link #ROOM_STORAGE} gives a {@link RoomStorageCache} for a room, queried from a
 * {@link dev.compactmods.machines.api.room.RoomInstance RoomInstance} via
 * {@code room.getCapability(StorageCapabilities.ROOM_STORAGE)}.
 */
public final class StorageCapabilities {

    public static final RoomCapability<RoomStorageCache, Void> ROOM_STORAGE =
            RoomCapability.createVoid(CompactMachinesCore.identifier("room_storage"), RoomStorageCache.class);

    // One cache instance per (server, room). The cache must persist between ticks to be useful, so it
    // cannot be rebuilt per capability query. Scoped by server via a weak key so entries drop when a
    // server is collected (e.g. integrated-server world reloads); room code keys are unique per server.
    private static final Map<MinecraftServer, Map<String, RoomStorageCache>> CACHES = new WeakHashMap<>();

    private StorageCapabilities() {}

    /** Registers the {@link #ROOM_STORAGE} provider. Call during {@code RegisterCapabilitiesEvent}. */
    public static void registerProviders() {
        RoomCapability.register(ROOM_STORAGE, (server, room, _) ->
                CACHES.computeIfAbsent(server, s -> new HashMap<>())
                        .computeIfAbsent(room.code(), code -> new RoomStorageCache()));
    }
}
