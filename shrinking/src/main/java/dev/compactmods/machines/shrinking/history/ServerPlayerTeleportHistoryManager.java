package dev.compactmods.machines.shrinking.history;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.machines.api.room.RoomInstance;
import dev.compactmods.machines.core.data.CMDataFile;
import dev.compactmods.machines.room.data.CMRoomDataLocations;
import dev.compactmods.machines.shrinking.api.history.PlayerTeleportHistoryManager;
import dev.compactmods.machines.shrinking.api.history.PlayerRoomHistoryEntry;
import dev.compactmods.machines.shrinking.api.history.RoomEntryMethod;
import dev.compactmods.machines.shrinking.api.history.RoomEntryResult;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ServerPlayerTeleportHistoryManager implements CMDataFile<ServerPlayerTeleportHistoryManager>, PlayerTeleportHistoryManager {

    public static final Codec<ServerPlayerTeleportHistoryManager> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            UUIDUtil.STRING_CODEC.fieldOf("player").forGetter(x -> x.playerId),
            Codec.INT.fieldOf("max_depth").forGetter(x -> x.maxDepth),
            PlayerRoomHistoryEntry.CODEC.listOf()
                    .fieldOf("history")
                    .forGetter(x -> x.history.stream().toList())
    ).apply(inst, ServerPlayerTeleportHistoryManager::new));

    private final ConcurrentLinkedDeque<PlayerRoomHistoryEntry> history;
    private final int maxDepth;
    private final UUID playerId;

    public ServerPlayerTeleportHistoryManager(UUID playerId, int maxDepth, List<PlayerRoomHistoryEntry> history) {
        this.history = new ConcurrentLinkedDeque<>();
        this.maxDepth = maxDepth;
        this.playerId = playerId;

        history.stream()
                .sorted(Comparator.comparing(PlayerRoomHistoryEntry::instant))
                .forEach(this::addRoomEntryUnsafe);
    }

    private Path getDataLocation(MinecraftServer server) {
        return CMRoomDataLocations.PLAYER_SPAWNS.apply(server);
    }

    public Optional<PlayerRoomHistoryEntry> pop(int steps) {
        var historyNodes = history.stream()
                .limit(steps)
                .collect(Collectors.toSet());

        historyNodes.forEach(history::remove);
        return peek();
    }

    @Override
    public Optional<PlayerRoomHistoryEntry> peek() {
        if (history.isEmpty())
            return Optional.empty();

        return Optional.ofNullable(history.peek());
    }

    public RoomEntryResult push(PlayerRoomHistoryEntry history) {
        return addRoomEntryUnsafe(history);
    }

    private @NotNull RoomEntryResult addRoomEntryUnsafe(PlayerRoomHistoryEntry history) {
        if (this.history.size() >= maxDepth)
            return RoomEntryResult.FAILED_TOO_FAR_DOWN;

        this.history.addLast(history);
        return RoomEntryResult.SUCCESS;
    }

    @Override
    public Codec<ServerPlayerTeleportHistoryManager> codec() {
        return CODEC;
    }

    @Override
    public Stream<PlayerRoomHistoryEntry> stream() {
        return history.stream();
    }

    @Override
    public RoomEntryResult push(RoomInstance room, RoomEntryMethod entryMethod) {
        return push(new PlayerRoomHistoryEntry(room.code(), Instant.now(), entryMethod));
    }

    @Override
    public void clear() {
        history.clear();
    }
}
