package dev.compactmods.machines.api.room.registration;

import dev.compactmods.machines.core.data.Saveable;
import dev.compactmods.machines.api.room.RoomInstance;
import dev.compactmods.machines.api.room.generation.NewRoomBuilder;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.Optional;
import java.util.stream.Stream;

public interface RoomRegistry extends Saveable {

    default MinecraftServer server() {
        return ServerLifecycleHooks.getCurrentServer();
    }

    NewRoomBuilder builder();

    boolean isRegistered(String room);

    Optional<RoomInstance> get(String room);

    int count();

    Stream<String> allRoomCodes();

    Stream<RoomInstance> allRooms();
}
