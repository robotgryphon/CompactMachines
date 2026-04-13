package dev.compactmods.machines.api.room.generation;

import dev.compactmods.machines.api.dimension.MissingDimensionException;
import dev.compactmods.machines.api.room.RoomInstance;

public interface RoomGenerator {

    NewRoomBuilder createNew();

    RoomInstance generate(String roomCode, RoomGenerationDetails details) throws MissingDimensionException;
}
