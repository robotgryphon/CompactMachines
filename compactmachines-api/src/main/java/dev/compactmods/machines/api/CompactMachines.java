package dev.compactmods.machines.api;

import dev.compactmods.machines.api.room.RoomInstance;
import dev.compactmods.machines.api.room.capability.RoomCapabilities;
import dev.compactmods.machines.api.room.registry.RoomRegistry;
import dev.compactmods.machines.api.room.spatial.RoomChunkManager;
import dev.compactmods.machines.core.CompactMachinesCore;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;

import java.util.Optional;

public class CompactMachines {
	public final static String MOD_ID = CompactMachinesCore.MOD_ID;

	public static Identifier identifier(String path) {
		return CompactMachinesCore.identifier(path);
	}

//
//	public static IPlayerHistoryApi playerHistoryApi() {
//		return PLAYER_HISTORY_API;
//	}
//
	public static Optional<RoomInstance> room(MinecraftServer server, String roomCode) {
		return roomRegistrar(server).get(roomCode);
	}
//
//	public static Optional<? extends IAttachmentHolder> existingRoomData(String roomCode) {
//		return ROOM_DATA_ACCESSOR.get(roomCode);
//	}
//
//	public static IRoomDataAttachmentAccessor roomDataAccessor() {
//		return ROOM_DATA_ACCESSOR;
//	}
//
//	public static IAttachmentHolder roomData(String roomCode) {
//		return ROOM_DATA_ACCESSOR.getOrCreate(roomCode);
//	}
//
//	public static IRoomUpgradeAccessor upgradeAccessor(RoomInstance instance) {
//		return UPGRADE_MANAGER.upgradeAccessor(instance);
//	}
//
//	public static IRoomUpgradeDataAttachmentAccessor upgradeDataAccessor() {
//		return ROOM_UPGRADE_DATA_ACCESSOR;
//	}
//
//	public static IAttachmentHolder roomUpgradeData(String roomCode, UUID upgradeId) {
//		return ROOM_UPGRADE_DATA_ACCESSOR.getOrCreate(roomCode, upgradeId);
//	}
//
//	public static IRoomUpgradeManager upgradeManager() {
//		return UPGRADE_MANAGER;
//	}
//
	public static RoomRegistry roomRegistrar(MinecraftServer server) {
		return RoomCapabilities.REGISTRY.getCapability(server);
	}
//
//	public static IRoomSpawnManagers spawnManagers() {
//		return SPAWN_MANAGERS;
//	}
//
	public static RoomChunkManager chunkManager(MinecraftServer server) {
		return RoomCapabilities.CHUNK_MANAGER.getCapability(server);
	}
//
//	public static IRoomChunks roomChunks(String roomCode) {
//		return chunkManager().get(roomCode);
//	}
}
