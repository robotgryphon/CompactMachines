package dev.compactmods.machines.server;

import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.core.data.Saveable;
import dev.compactmods.machines.api.room.generation.RoomGenerator;
import dev.compactmods.machines.api.room.data.IRoomDataAttachmentAccessor;
import dev.compactmods.machines.room.ServerRoomGenerator;
import dev.compactmods.machines.shrinking.api.capability.IPlayerHistoryApi;
import dev.compactmods.machines.api.room.registration.RoomRegistry;
import dev.compactmods.machines.api.room.spatial.RoomChunkManager;
import dev.compactmods.machines.api.room.spawn.IRoomSpawnManagers;
import dev.compactmods.machines.api.room.upgrade.IRoomUpgradeAccessor;
import dev.compactmods.machines.api.room.upgrade.IRoomUpgradeManager;
import dev.compactmods.machines.api.room.upgrade.data.IRoomUpgradeDataAttachmentAccessor;
import dev.compactmods.machines.room.ServerRoomRegistry;
import dev.compactmods.machines.room.spatial.MemoryGraphChunkManager;
import dev.compactmods.machines.room.spawn.RoomSpawnManagers;
import dev.compactmods.machines.shrinking.capability.PlayerHistoryApi;
import dev.compactmods.machines.upgrades.service.RoomUpgradeDataAccessor;
import dev.compactmods.machines.server.service.RoomDataAttachmentAccessor;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.attachment.IAttachmentHolder;

import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

class ServerCapabilities implements Saveable, IServerCapabilities {

    private final RoomRegistry roomRegistry;
    private final RoomSpawnManagers spawnManagers;
    private final RoomGenerator roomGenerator;
//    private final IRoomUpgradeManager upgradeManager;
    private final IRoomDataAttachmentAccessor roomDataAttachments;
    private final IRoomUpgradeDataAttachmentAccessor roomUpgradeDataAttachments;
    private final IPlayerHistoryApi playerHistory;
    private final RoomChunkManager roomChunkManager;

    ServerCapabilities(MinecraftServer server) {
        this.roomRegistry = new ServerRoomRegistry(server);
        this.spawnManagers = new RoomSpawnManagers(server, roomRegistry);
        this.roomGenerator = new ServerRoomGenerator(CompactDimension.forServer(server), roomRegistry, spawnManagers);
//        this.upgradeManager = new RoomUpgradeManager(server);
        this.roomDataAttachments = new RoomDataAttachmentAccessor(server);
        this.roomUpgradeDataAttachments = new RoomUpgradeDataAccessor(server);
        this.playerHistory = new PlayerHistoryApi(server);
        this.roomChunkManager = new MemoryGraphChunkManager(server);
    }

    @Override
    public RoomRegistry roomRegistry() {
        return roomRegistry;
    }

    @Override
    public IRoomSpawnManagers spawnManagers() {
        return spawnManagers;
    }

    @Override
    public RoomGenerator roomGenerator() {
        return roomGenerator;
    }

    public IAttachmentHolder roomDataAttachments(String roomCode) {
        return roomDataAttachments.getOrCreate(roomCode);
    }

    public IAttachmentHolder roomUpgradeDataAttachments(String roomCode, UUID upgradeId) {
        return roomUpgradeDataAttachments.getOrCreate(roomCode, upgradeId);
    }

//    @Override
//    public IRoomUpgradeAccessor upgradeDataAccessor(String roomCode) {
//        return roomRegistry.get(roomCode)
//                .map(upgradeManager::upgradeAccessor)
//                .orElseThrow();
//    }

    @Override
    public RoomChunkManager chunkManager() {
        return roomChunkManager;
    }

    public void save() {
        Stream.of(roomRegistry, roomGenerator,
                        playerHistory,
                        spawnManagers,
                        roomDataAttachments,
                        roomUpgradeDataAttachments)
                .filter(Objects::nonNull)
                .filter(Saveable.class::isInstance)
                .map(Saveable.class::cast)
                .forEach(Saveable::save);
    }
}
