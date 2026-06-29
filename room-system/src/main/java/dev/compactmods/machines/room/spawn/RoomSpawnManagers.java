package dev.compactmods.machines.room.spawn;

import dev.compactmods.machines.api.room.spawn.IRoomSpawnManager;
import dev.compactmods.machines.api.room.spawn.IRoomSpawnManagers;
import dev.compactmods.machines.core.data.manager.CMKeyedDataFileManager;
import dev.compactmods.machines.room.data.CMRoomDataLocations;
import net.minecraft.server.MinecraftServer;

public class RoomSpawnManagers implements IRoomSpawnManagers {

    private final CMKeyedDataFileManager<String, SpawnManager> spawnManagers;

    public RoomSpawnManagers(MinecraftServer server) {
        this.spawnManagers = new CMKeyedDataFileManager<>(server, () -> SpawnManager.CODEC,
                CMRoomDataLocations.PLAYER_SPAWNS.apply(server));
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
