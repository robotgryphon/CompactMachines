package dev.compactmods.machines.server;

import dev.compactmods.machines.api.room.generation.RoomGenerator;
import dev.compactmods.machines.api.room.registry.RoomRegistry;
import dev.compactmods.machines.api.room.spatial.RoomChunkManager;
import dev.compactmods.machines.api.room.spawn.IRoomSpawnManagers;
import net.neoforged.neoforge.attachment.IAttachmentHolder;

import java.util.UUID;

public interface IServerCapabilities {
    RoomRegistry roomRegistry();

    IRoomSpawnManagers spawnManagers();

    RoomGenerator roomGenerator();

    IAttachmentHolder roomDataAttachments(String roomCode);

    IAttachmentHolder roomUpgradeDataAttachments(String roomCode, UUID upgradeId);

//    IRoomUpgradeAccessor upgradeDataAccessor(String roomCode);

    RoomChunkManager chunkManager();
}
