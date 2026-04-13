package dev.compactmods.machines.shrinking.capability;

import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.api.dimension.CompactDimensionTransitions;
import dev.compactmods.machines.api.dimension.MissingDimensionException;
import dev.compactmods.machines.api.room.RoomInstance;
import dev.compactmods.machines.api.room.capability.RoomCapabilities;
import dev.compactmods.machines.core.CompactMachinesCore;
import dev.compactmods.machines.shrinking.ShrinkingHelper;
import dev.compactmods.machines.shrinking.api.ShrinkingDeviceConfiguration;
import dev.compactmods.machines.shrinking.api.capability.PlayerEntryPointHistoryManager;
import dev.compactmods.machines.shrinking.api.history.PlayerRoomHistoryEntry;
import dev.compactmods.machines.shrinking.api.history.RoomEntryPoint;
import dev.compactmods.machines.shrinking.api.history.RoomEntryResult;
import dev.compactmods.machines.shrinking.api.history.RoomExitResult;
import dev.compactmods.machines.shrinking.Shrinking;
import dev.compactmods.machines.shrinking.api.capability.PlayerShrinkingHandler;
import dev.compactmods.machines.shrinking.network.SyncRoomMetadataPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Util;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

public class ServerPlayerShrinkingHandler implements PlayerShrinkingHandler {

    final static Logger LOGGER = LogManager.getLogger();

    final MinecraftServer server;
    final ServerPlayer player;
    private final ServerLevel compactDim;
    private final RoomInstance roomInstance;
    private final PlayerEntryPointHistoryManager historyManager;

    public ServerPlayerShrinkingHandler(ServerPlayer player, RoomInstance roomInstance) {
        this.server = player.level().getServer();
        this.historyManager = Shrinking.HISTORY_MANAGER.getCapability(server);
        this.roomInstance = roomInstance;
        this.player = player;
        this.compactDim = CompactDimension.forServer(server);
    }

    @Override
    public CompletableFuture<RoomEntryResult> tryEnter(RoomEntryPoint entryPoint) {
        final var entry = new PlayerRoomHistoryEntry(roomInstance.code(), Instant.now(), entryPoint);
        final var result = historyManager.push(player.getUUID(), entry);

        if (result == RoomEntryResult.FAILED_TOO_FAR_DOWN) {
            player.sendOverlayMessage(Component.translatableWithFallback("compactmachines.errors.too_far_down", "An otherworldly force prevents you from shrinking more.")
                    .withStyle(ChatFormatting.DARK_RED)
                    .withStyle(ChatFormatting.ITALIC));

            return CompletableFuture.completedFuture(result);
        }

        if (result.successful()) {
            // Mark current room
            player.setData(Shrinking.CURRENT_ROOM_CODE, roomInstance.code());
            player.setData(Shrinking.LAST_ROOM_ENTRYPOINT, RoomEntryPoint.playerEnteringMachine(player));

            return server.submit(() -> {
                final var spawnManager = roomInstance.getCapability(RoomCapabilities.SPAWN_MANAGER);

                player.getCooldowns().addCooldown(Shrinking.PERSONAL_SHRINKING_DEVICE.getId(), 25);

                final var spawns = spawnManager.spawns();
                final var spawn = spawns.forPlayer(player.getUUID()).orElse(spawns.defaultSpawn());
                player.teleport(CompactDimensionTransitions.to(compactDim, spawn.position(), spawn.rotation()));

                PacketDistributor.sendToPlayer(player, new SyncRoomMetadataPacket(roomInstance.code(), Util.NIL_UUID));

                return result;
            });
        }

        return CompletableFuture.completedFuture(result);
    }

    @Override
    public CompletableFuture<RoomEntryResult> tryEnter(GlobalPos machinePosition, ShrinkingDeviceConfiguration config) {
        return tryEnter(RoomEntryPoint.playerEnteringMachine(player));
    }

    @Override
    public CompletableFuture<RoomExitResult> tryExit() {
        if (!CompactDimension.isLevelCompact(player.level()))
            return CompletableFuture.completedFuture(RoomExitResult.FAILED_NOT_IN_COMPACT_DIM);

        return server.submit(() -> {
            final var lastHistory = historyManager.lastHistory(player).orElse(null);
            if (lastHistory != null) {
                player.getCooldowns().addCooldown(Shrinking.PERSONAL_SHRINKING_DEVICE.getId(), 25);

                player.setData(Shrinking.LAST_ROOM_ENTRYPOINT, lastHistory.entryPoint());
                historyManager.pop(player.getUUID(), 1);

                player.setData(Shrinking.CURRENT_ROOM_CODE, lastHistory.roomCode());

                final var location = lastHistory.entryPoint().entryLocation();
                final var level = server.getLevel(location.dimension());
                if (level != null) {
                    LOGGER.debug("Teleporting player {} to {} as they jump up a level...", player.getUUID(), location);
                    player.teleport(CompactDimensionTransitions.to(level, location.position(), location.rotation()));

                    return RoomExitResult.SUCCESS_WENT_TO_LAST_ENTRYPOINT;
                } else {
                    LOGGER.error("Player tracking points to an unknown dimension. Teleporting player {} to their default spawn instead.", player.getUUID());
                    ShrinkingHelper.teleportPlayerToRespawnOrOverworld(server, player);

                    return RoomExitResult.SUCCESS_WENT_TO_SPAWN;
                }

            } else {
                player.removeData(Shrinking.LAST_ROOM_ENTRYPOINT);
                ShrinkingHelper.teleportPlayerToRespawnOrOverworld(server, player);

                return RoomExitResult.SUCCESS_WENT_TO_SPAWN;
            }
        });
    }
}
