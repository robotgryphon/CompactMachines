package dev.compactmods.machines.preview.client;

import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Caches the built {@link RoomPreviewMesh} per room so it is rebuilt only when that room's snapshot
 * actually changes (tracked by {@link ClientRoomPreviews.Entry#version()}), not every frame.
 *
 * <p>Accessed only from the render thread (during block-entity render-state extraction), so it needs
 * no synchronisation of its own; it reads snapshots from the concurrent {@link ClientRoomPreviews}
 * store.
 */
public final class RoomPreviewMeshCache {

    private static final Map<String, Cached> CACHE = new HashMap<>();

    private RoomPreviewMeshCache() {}

    private record Cached(int version, RoomPreviewMesh mesh) {}

    /**
     * {@return the mesh for {@code roomCode}, building it if the stored snapshot is newer than the
     * cached mesh, or {@code null} if there is no snapshot / nothing to draw}
     */
    public static @Nullable RoomPreviewMesh get(String roomCode) {
        final ClientRoomPreviews.Entry entry = ClientRoomPreviews.get(roomCode);
        if (entry == null) {
            CACHE.remove(roomCode);
            return null;
        }

        final Cached cached = CACHE.get(roomCode);
        if (cached != null && cached.version == entry.version())
            return cached.mesh.isEmpty() ? null : cached.mesh;

        final RoomPreviewMesh mesh = RoomPreviewMesh.build(entry.snapshot());
        CACHE.put(roomCode, new Cached(entry.version(), mesh));
        dev.compactmods.machines.core.CompactMachinesCore.modLog().info("[RoomPreview] built mesh {} v{}: {} vertices",
                roomCode, entry.version(), mesh.vertexCount());
        return mesh.isEmpty() ? null : mesh;
    }

    /** Drops all cached meshes — call on disconnect alongside {@link ClientRoomPreviews#clear()}. */
    public static void clear() {
        CACHE.clear();
    }
}
