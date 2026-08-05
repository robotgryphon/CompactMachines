package dev.compactmods.machines.preview.client;

import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Caches the baked {@link RoomPreviewMesh} per room so its (textured) geometry is rebuilt only when
 * that room's snapshot changes (tracked by {@link ClientRoomPreviews.Entry#version()}). Because each
 * mesh owns GPU buffers, superseding or clearing an entry frees the old one.
 *
 * <p>Accessed only from the render thread (block-entity render-state extraction), so it needs no
 * synchronisation; it reads snapshots from the concurrent {@link ClientRoomPreviews} store.
 */
public final class RoomPreviewMeshCache {

    private static final Map<String, Cached> CACHE = new HashMap<>();

    private RoomPreviewMeshCache() {}

    private record Cached(int version, RoomPreviewMesh mesh) {}

    /**
     * {@return the mesh for {@code roomCode}, baking it if the stored snapshot is newer than the
     * cached mesh, or {@code null} if there is no snapshot / nothing to draw}
     */
    public static @Nullable RoomPreviewMesh get(String roomCode) {
        final ClientRoomPreviews.Entry entry = ClientRoomPreviews.get(roomCode);
        if (entry == null) {
            evict(roomCode);
            return null;
        }

        final Cached cached = CACHE.get(roomCode);
        if (cached != null && cached.version == entry.version())
            return cached.mesh.isEmpty() ? null : cached.mesh;

        evict(roomCode); // free the stale mesh's GPU buffers before rebaking
        final RoomPreviewMesh mesh = RoomPreviewMesh.build(entry.snapshot());
        CACHE.put(roomCode, new Cached(entry.version(), mesh));
        return mesh.isEmpty() ? null : mesh;
    }

    private static void evict(String roomCode) {
        final Cached old = CACHE.remove(roomCode);
        if (old != null) old.mesh.close();
    }

    /** Frees and drops all cached meshes — call on disconnect alongside {@link ClientRoomPreviews#clear()}. */
    public static void clear() {
        for (Cached cached : CACHE.values())
            cached.mesh.close();
        CACHE.clear();
    }
}
