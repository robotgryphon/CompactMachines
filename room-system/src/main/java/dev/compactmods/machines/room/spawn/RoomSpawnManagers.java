package dev.compactmods.machines.room.spawn;

import dev.compactmods.machines.api.room.registry.RoomRegistry;
import dev.compactmods.machines.api.room.spawn.IRoomSpawnManager;
import dev.compactmods.machines.api.room.spawn.IRoomSpawnManagers;
import dev.compactmods.machines.core.data.CMKeyedDataFileManager;
import net.minecraft.server.MinecraftServer;

public class RoomSpawnManagers implements IRoomSpawnManagers {

    private final CMKeyedDataFileManager<String, SpawnManager> spawnManagers;

    public RoomSpawnManagers(MinecraftServer server, RoomRegistry roomRegistry) {
        this.spawnManagers = new CMKeyedDataFileManager<>(server, (serv, code) -> {
            var instance = roomRegistry.get(code).orElseThrow();
            return new SpawnManager(instance);
        });
    }

    @Override
    public IRoomSpawnManager get(String roomCode) {
        return spawnManagers.data(roomCode);
    }

    @Override
    public void save() {
        spawnManagers.save();
    }
}
