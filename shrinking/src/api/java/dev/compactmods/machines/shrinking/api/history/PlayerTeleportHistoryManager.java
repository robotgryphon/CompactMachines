package dev.compactmods.machines.shrinking.api.history;

import dev.compactmods.machines.api.room.RoomInstance;

import java.util.Optional;
import java.util.stream.Stream;

public interface PlayerTeleportHistoryManager {

   Optional<PlayerRoomHistoryEntry> peek();

   Stream<PlayerRoomHistoryEntry> stream();

   RoomEntryResult push(RoomInstance room, RoomEntryMethod entryMethod);

   Optional<PlayerRoomHistoryEntry> pop(int steps);

   void clear();
}

