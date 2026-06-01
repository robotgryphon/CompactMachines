package dev.compactmods.machines.server;

import dev.compactmods.machines.api.room.capability.RoomCapabilities;
import dev.compactmods.machines.api.room.capability.RoomCapability;
import dev.compactmods.machines.core.capability.ServerCapability;
import dev.compactmods.machines.core.data.Saveable;
import dev.compactmods.machines.api.room.generation.RoomGenerator;
import dev.compactmods.machines.api.room.data.IRoomDataAttachmentAccessor;
import dev.compactmods.machines.room.generation.ServerRoomGenerator;
import dev.compactmods.machines.shrinking.api.capability.IPlayerHistoryApi;
import dev.compactmods.machines.api.room.registry.RoomRegistry;
import dev.compactmods.machines.api.room.spatial.RoomChunkManager;
import dev.compactmods.machines.api.room.spawn.IRoomSpawnManagers;
import dev.compactmods.machines.api.room.upgrade.data.IRoomUpgradeDataAttachmentAccessor;
import dev.compactmods.machines.room.registry.ServerRoomRegistry;
import dev.compactmods.machines.room.spatial.MemoryGraphChunkManager;
import dev.compactmods.machines.room.spawn.RoomSpawnManagers;
import dev.compactmods.machines.shrinking.capability.PlayerHistoryApi;
import dev.compactmods.machines.upgrades.service.RoomUpgradeDataAccessor;
import dev.compactmods.machines.server.service.RoomDataAttachmentAccessor;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

class ServerCapabilities implements Saveable, IServerCapabilities {

    private final RoomRegistry roomRegistry;
    private final RoomSpawnManagers spawnManagers;
    private final RoomGenerator roomGenerator;
    private final IRoomDataAttachmentAccessor roomDataAttachments;
    private final IRoomUpgradeDataAttachmentAccessor roomUpgradeDataAttachments;
    private final IPlayerHistoryApi playerHistory;
    private final RoomChunkManager roomChunkManager;

    ServerCapabilities(MinecraftServer server) {
        this.roomRegistry = new ServerRoomRegistry(server);
        this.spawnManagers = new RoomSpawnManagers(server, roomRegistry);
        this.roomGenerator = new ServerRoomGenerator(server, roomRegistry);
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

    static void register(RegisterCapabilitiesEvent ignored) {

        ServerCapability.registerVoid(RoomCapabilities.REGISTRY, (server, _) -> {
            var caps = server.getData(CompactMachinesServer.SERVER_CAPABILITIES);
            return caps.roomRegistry();
        });

        ServerCapability.registerVoid(RoomCapabilities.GENERATOR, (server, _) -> {
            var caps = server.getData(CompactMachinesServer.SERVER_CAPABILITIES);
            return caps.roomGenerator();
        });

        ServerCapability.registerVoid(RoomCapabilities.CHUNK_MANAGER, (server, _) -> {
            var caps = server.getData(CompactMachinesServer.SERVER_CAPABILITIES);
            return caps.chunkManager();
        });

        RoomCapability.register(RoomCapabilities.SPAWN_MANAGER, (server, roomCode, _) -> {
            var caps = server.getData(CompactMachinesServer.SERVER_CAPABILITIES);
            return caps.spawnManagers().get(roomCode);
        });

        RoomCapability.register(RoomCapabilities.ROOM_DATA_ATTACHMENTS, (server, roomCode, _) -> {
            var caps = server.getData(CompactMachinesServer.SERVER_CAPABILITIES);
            return caps.roomDataAttachments(roomCode);
        });
    }
}
