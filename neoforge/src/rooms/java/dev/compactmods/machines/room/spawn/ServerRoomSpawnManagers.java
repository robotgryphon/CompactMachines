package dev.compactmods.machines.room.spawn;

import dev.compactmods.machines.api.room.RoomInstance;
import dev.compactmods.machines.api.room.spawn.IRoomSpawnManager;
import dev.compactmods.machines.api.room.spawn.IRoomSpawnManagers;
import dev.compactmods.machines.core.data.manager.CodecFileManager;
import dev.compactmods.machines.room.data.CMRoomDataLocations;
import net.minecraft.server.MinecraftServer;

public final class ServerRoomSpawnManagers implements IRoomSpawnManagers {

    private final MinecraftServer server;
    private final CodecFileManager.Keyed<RoomInstance, SpawnManager> spawnManagers;

    public ServerRoomSpawnManagers(MinecraftServer server) {
        this.spawnManagers = CodecFileManager.keyed(RoomInstance::code, SpawnManager.CODEC)
                .at(CMRoomDataLocations.PLAYER_SPAWNS)
                .build(server);
        this.server = server;
    }

    public IRoomSpawnManager get(RoomInstance room) {
        if (this.spawnManagers.hasData(room)) {
            return this.spawnManagers.data(room);
        } else {
            SpawnManager newManager = new SpawnManager(server, room);
            this.spawnManagers.set(room, newManager);
            this.spawnManagers.save();
            return newManager;
        }
    }

    public void save() {
        this.spawnManagers.save();
    }
}
