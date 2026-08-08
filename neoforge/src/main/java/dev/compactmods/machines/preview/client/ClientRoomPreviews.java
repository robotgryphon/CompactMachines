package dev.compactmods.machines.preview.client;

import dev.compactmods.machines.preview.RoomPreviewSnapshot;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client-side store of the latest {@link RoomPreviewSnapshot} received per room code.
 *
 * <p>Snapshots arrive on the client thread (via {@link #accept}) but are read by the preview mesher
 * off the render thread, so the backing map is concurrent and each entry carries a monotonically
 * increasing {@link Entry#version() version}. A mesher caches meshes keyed by {@code (roomCode,
 * version)} and rebuilds only when the version advances — that's what lets the (heavier) mesh build
 * run "every second or so" independent of how often snapshots land.
 */
public final class ClientRoomPreviews {

    private static final Map<String, Entry> PREVIEWS = new ConcurrentHashMap<>();

    private ClientRoomPreviews() {}

    /** A stored snapshot plus a version that increments each time the room's snapshot is replaced. */
    public record Entry(RoomPreviewSnapshot snapshot, int version) {}

    /** Stores the newest snapshot for {@code roomCode}, bumping its version. Called on the client thread. */
    public static void accept(String roomCode, RoomPreviewSnapshot snapshot) {
        PREVIEWS.compute(roomCode, (code, previous) ->
                new Entry(snapshot, previous == null ? 1 : previous.version() + 1));
        dev.compactmods.machines.core.CompactMachinesCore.modLog().info("[RoomPreview] client received snapshot {}: {}x{}x{} palette={} empty={}",
                roomCode, snapshot.sizeX(), snapshot.sizeY(), snapshot.sizeZ(), snapshot.palette().size(), snapshot.isEmpty());
    }

    /** {@return the latest stored preview for {@code roomCode}, or {@code null} if none has arrived} */
    public static @Nullable Entry get(String roomCode) {
        return PREVIEWS.get(roomCode);
    }

    public static boolean has(String roomCode) {
        return PREVIEWS.containsKey(roomCode);
    }

    /** Drops all stored previews — call on disconnect so a new session starts clean. */
    public static void clear() {
        PREVIEWS.clear();
    }
}
