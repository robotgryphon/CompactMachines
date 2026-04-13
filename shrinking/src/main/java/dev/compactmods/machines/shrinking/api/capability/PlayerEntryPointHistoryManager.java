package dev.compactmods.machines.shrinking.api.capability;

import dev.compactmods.machines.shrinking.api.history.PlayerRoomHistoryEntry;
import dev.compactmods.machines.shrinking.api.history.RoomEntryResult;
import dev.compactmods.machines.shrinking.api.history.RoomExitResult;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public interface PlayerEntryPointHistoryManager {

   Optional<PlayerRoomHistoryEntry> lastHistory(Player player);

   Stream<PlayerRoomHistoryEntry> history(Player player);

   Stream<PlayerRoomHistoryEntry> history(UUID player);

   RoomEntryResult push(UUID player, PlayerRoomHistoryEntry history);

   RoomExitResult pop(UUID player, int steps);

   void clearHistory(ServerPlayer player);
}
