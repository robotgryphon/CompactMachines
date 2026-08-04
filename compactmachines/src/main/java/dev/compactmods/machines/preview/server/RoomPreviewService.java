package dev.compactmods.machines.preview.server;

import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.api.room.RoomInstance;
import dev.compactmods.machines.api.room.capability.RoomCapabilities;
import dev.compactmods.machines.api.room.registry.RoomRegistry;
import dev.compactmods.machines.core.CompactMachinesCore;
import dev.compactmods.machines.network.room.RoomPreviewSnapshotPacket;
import dev.compactmods.machines.preview.RoomPreviewSnapshot;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

/**
 * Server-side driver for room previews: tracks which players want which rooms, periodically captures
 * the interior of every <em>subscribed and loaded</em> room, and pushes {@link RoomPreviewSnapshot}s
 * to the interested clients only when a room's contents actually change.
 *
 * <p>All work runs on the server thread — {@link #onLevelTick} fires on the compact dimension's tick,
 * which is where the room interiors live, and the block reads must not run off-thread. The scan is
 * throttled to {@link #GATHER_INTERVAL_TICKS} and further limited to rooms someone is subscribed to,
 * so an idle server does no work. The (heavier) job of turning a snapshot into a mesh happens on the
 * client, off the render thread.
 *
 * <p>State is kept per {@link MinecraftServer} behind a weak key so it drops when an integrated
 * server is collected on world reload.
 */
public final class RoomPreviewService {

    /** How often (in ticks) loaded rooms are rescanned. 40 ticks ≈ 2 s — "every few ticks", not every tick. */
    public static final int GATHER_INTERVAL_TICKS = 40;

    /**
     * The chunk ticket that keeps a watched room's interior loaded so it can be read.
     *
     * <p>{@link TicketType#FLAG_LOADING} only — no {@code FLAG_SIMULATION} (the room never ticks: no
     * mobs, redstone, or block ticks run just because someone is looking at it) and no
     * {@code FLAG_PERSIST} (the ticket is transient, never written to the save). Added with radius 0
     * it holds the chunk at the "full/loaded, non-ticking" level, which is all a block read needs.
     */
    private static final TicketType PREVIEW_TICKET = new TicketType(TicketType.NO_TIMEOUT, TicketType.FLAG_LOADING);

    private static final Map<MinecraftServer, ServerState> STATES = new WeakHashMap<>();

    private RoomPreviewService() {}

    // --- subscription management ------------------------------------------------------

    /**
     * Replaces {@code player}'s subscription with {@code roomCodes} and immediately sends any already
     * cached snapshots for rooms the player didn't previously have (so a client that comes into range
     * of a machine doesn't wait a full scan interval for its first frame).
     */
    public static void setSubscription(ServerPlayer player, List<String> roomCodes) {
        final MinecraftServer server = player.level().getServer();
        if (server == null) return;

        final ServerState state = STATES.computeIfAbsent(server, s -> new ServerState());
        final Set<String> requested = new HashSet<>(roomCodes);
        final Set<String> previous = state.byPlayer.getOrDefault(player.getUUID(), Set.of());

        state.subscribe(player.getUUID(), requested);
        CompactMachinesCore.modLog().info("[RoomPreview] subscription from {}: {}", player.getName().getString(), requested);

        // Push cached snapshots for the rooms this player just added.
        for (String code : requested) {
            if (previous.contains(code)) continue;
            final CachedSnapshot cached = state.cache.get(code);
            if (cached != null)
                PacketDistributor.sendToPlayer(player, new RoomPreviewSnapshotPacket(code, cached.snapshot));
        }
    }

    // --- ticking ----------------------------------------------------------------------

    public static void onLevelTick(final LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (!level.dimension().equals(CompactDimension.LEVEL_KEY)) return;
        if (level.getGameTime() % GATHER_INTERVAL_TICKS != 0L) return;

        final MinecraftServer server = level.getServer();
        final ServerState state = STATES.get(server);
        if (state == null) return;

        final RoomRegistry rooms = RoomCapabilities.REGISTRY.getCapability(server);
        if (rooms == null) return;

        // Keep watched rooms loaded (and release the rest) before trying to read them.
        reconcilePreviewTickets(level, rooms, state);
        if (state.byRoom.isEmpty()) return;

        // Snapshot the subscribed-room set so a subscription change mid-loop can't disturb iteration.
        for (String code : new ArrayList<>(state.byRoom.keySet())) {
            final Set<UUID> subscribers = state.byRoom.get(code);
            if (subscribers == null || subscribers.isEmpty()) continue;

            final RoomInstance room = rooms.get(code).orElse(null);
            if (room == null) {
                CompactMachinesCore.modLog().info("[RoomPreview] gather {}: no room instance", code);
                continue;
            }
            if (!isLoaded(level, room)) {
                CompactMachinesCore.modLog().info("[RoomPreview] gather {}: room NOT loaded (chunks absent) — skipping", code);
                continue;
            }

            final RoomPreviewSnapshot snapshot = RoomPreviewSnapshot.capture(level, room.boundaries().innerBounds());
            final int hash = snapshot.contentHash();

            final CachedSnapshot cached = state.cache.get(code);
            if (cached != null && cached.hash == hash) continue; // unchanged — don't resend

            state.cache.put(code, new CachedSnapshot(hash, snapshot));
            CompactMachinesCore.modLog().info("[RoomPreview] gather {}: sending {}x{}x{} palette={} empty={} to {} player(s)",
                    code, snapshot.sizeX(), snapshot.sizeY(), snapshot.sizeZ(), snapshot.palette().size(), snapshot.isEmpty(), subscribers.size());
            broadcast(server, subscribers, new RoomPreviewSnapshotPacket(code, snapshot));
        }
    }

    /** {@return true if every chunk the room's interior spans is already loaded} */
    private static boolean isLoaded(ServerLevel level, RoomInstance room) {
        return room.boundaries().innerChunkPositions().allMatch(cp -> level.hasChunk(cp.x(), cp.z()));
    }

    /**
     * Adds preview load tickets for rooms that gained a watcher and removes them for rooms that lost
     * their last one, so exactly the currently-watched rooms are held loaded. Runs each gather tick;
     * a newly watched room becomes readable a cycle or two later, once its chunks finish loading.
     */
    private static void reconcilePreviewTickets(ServerLevel level, RoomRegistry rooms, ServerState state) {
        final Set<String> desired = state.byRoom.keySet();

        for (String code : new ArrayList<>(desired)) {
            if (state.ticketed.contains(code)) continue;
            final RoomInstance room = rooms.get(code).orElse(null);
            if (room == null) continue;
            setPreviewTickets(level, room, true);
            state.ticketed.add(code);
        }

        state.ticketed.removeIf(code -> {
            if (desired.contains(code)) return false;
            rooms.get(code).ifPresent(room -> setPreviewTickets(level, room, false));
            return true;
        });
    }

    private static void setPreviewTickets(ServerLevel level, RoomInstance room, boolean add) {
        final var chunkSource = level.getChunkSource();
        room.boundaries().innerChunkPositions().forEach(cp -> {
            if (add) chunkSource.addTicketWithRadius(PREVIEW_TICKET, cp, 0);
            else chunkSource.removeTicketWithRadius(PREVIEW_TICKET, cp, 0);
        });
    }

    private static void broadcast(MinecraftServer server, Set<UUID> subscribers, RoomPreviewSnapshotPacket packet) {
        for (UUID id : subscribers) {
            final ServerPlayer player = server.getPlayerList().getPlayer(id);
            if (player != null)
                PacketDistributor.sendToPlayer(player, packet);
        }
    }

    // --- lifecycle cleanup ------------------------------------------------------------

    public static void onPlayerLoggedOut(final PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        final MinecraftServer server = player.level().getServer();
        if (server == null) return;
        final ServerState state = STATES.get(server);
        if (state != null)
            state.unsubscribe(player.getUUID());
    }

    public static void onServerStopping(final ServerStoppingEvent event) {
        STATES.remove(event.getServer());
    }

    // --- per-server state -------------------------------------------------------------

    private record CachedSnapshot(int hash, RoomPreviewSnapshot snapshot) {}

    private static final class ServerState {
        /** Player → the rooms they're subscribed to. */
        private final Map<UUID, Set<String>> byPlayer = new HashMap<>();
        /** Reverse index: room code → the players subscribed to it (drives sends). */
        private final Map<String, Set<UUID>> byRoom = new HashMap<>();
        /** Latest sent snapshot per room, with its content hash, so unchanged rooms aren't re-sent. */
        private final Map<String, CachedSnapshot> cache = new HashMap<>();
        /** Rooms currently held loaded by a preview ticket (see {@link #reconcilePreviewTickets}). */
        private final Set<String> ticketed = new HashSet<>();

        void subscribe(UUID player, Set<String> rooms) {
            unsubscribe(player);
            if (rooms.isEmpty()) return;
            byPlayer.put(player, rooms);
            for (String code : rooms)
                byRoom.computeIfAbsent(code, c -> new HashSet<>()).add(player);
        }

        void unsubscribe(UUID player) {
            final Set<String> previous = byPlayer.remove(player);
            if (previous == null) return;
            for (String code : previous) {
                final Set<UUID> subs = byRoom.get(code);
                if (subs == null) continue;
                subs.remove(player);
                if (subs.isEmpty()) {
                    byRoom.remove(code);
                    cache.remove(code); // no one watching — drop the cached snapshot too
                }
            }
        }
    }
}
