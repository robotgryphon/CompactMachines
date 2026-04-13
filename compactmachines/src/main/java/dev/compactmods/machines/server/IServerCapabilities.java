package dev.compactmods.machines.server;

import dev.compactmods.machines.api.room.generation.RoomGenerator;
import dev.compactmods.machines.api.room.registration.RoomRegistry;
import dev.compactmods.machines.api.room.spatial.RoomChunkManager;
import dev.compactmods.machines.api.room.spawn.IRoomSpawnManagers;
import dev.compactmods.machines.api.room.upgrade.IRoomUpgradeAccessor;
import dev.compactmods.machines.core.capability.ServerCapability;
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
