package dev.compactmods.machines.api.room.registry;

import dev.compactmods.machines.api.room.generation.RoomGenerator;
import dev.compactmods.machines.core.data.Saveable;
import dev.compactmods.machines.api.room.RoomInstance;

import java.util.Optional;
import java.util.stream.Stream;

public interface RoomRegistry extends Saveable {

    boolean isRegistered(String room);

    Optional<RoomInstance> get(String room);

    int count();

    Stream<String> allRoomCodes();

    Stream<RoomInstance> allRooms();
}
