package dev.compactmods.machines.neoforge.room;

import dev.compactmods.machines.api.room.RoomApi;
import dev.compactmods.machines.api.room.RoomInstance;
import dev.compactmods.machines.api.room.history.RoomEntryPoint;
import dev.compactmods.machines.LoggingUtil;
import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.api.dimension.MissingDimensionException;
import dev.compactmods.machines.neoforge.dimension.SimpleTeleporter;
import dev.compactmods.machines.neoforge.util.ForgePlayerUtil;
import dev.compactmods.machines.player.PlayerEntryPointHistory;
import net.minecraft.ChatFormatting;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;

public abstract class RoomHelper {

    private static final Logger LOGS = LoggingUtil.modLog();

    public static boolean entityInsideRoom(LivingEntity entity, String roomCode) {
        // Recursion check. Player is inside the room being queried.
        if (entity.level().dimension().equals(CompactDimension.LEVEL_KEY)) {
            return RoomApi.chunks(roomCode).hasChunk(entity.chunkPosition());
        }

        return false;
    }

    public static void teleportPlayerIntoMachine(Level machineLevel, ServerPlayer player, GlobalPos machinePos, String roomCode) {
        MinecraftServer serv = machineLevel.getServer();

        LOGS.debug("Player {} entering machine at: {}", player.getName(), machinePos);
        RoomApi.room(roomCode).ifPresent(roomInfo -> {
            try {
                teleportPlayerIntoRoom(serv, player, roomInfo, RoomEntryPoint.playerEnteringMachine(player));
            } catch (MissingDimensionException e) {
                LOGS.fatal("Critical error; could not enter a freshly-created room instance.", e);
            }
        });
    }

    public static void teleportPlayerIntoRoom(MinecraftServer serv, ServerPlayer player, RoomInstance room, RoomEntryPoint entryPoint)
            throws MissingDimensionException {
        final var compactDim = CompactDimension.forServer(serv);

        final var history = PlayerEntryPointHistory.forServer(serv, 5);
        final var result = history.enterRoom(player, room.code(), entryPoint);

        LOGS.debug("Entry result: {}", result);

        switch (result) {
            case FAILED_TOO_FAR_DOWN -> {
                player.displayClientMessage(Component.translatableWithFallback("compactmachines.errors.too_far_down", "An otherworldly force prevents you from shrinking more.")
                        .withStyle(ChatFormatting.DARK_RED)
                        .withStyle(ChatFormatting.ITALIC), true);
            }

            case SUCCESS -> {
                serv.submitAsync(() -> {
                    final var spawns = RoomApi.spawnManager(room.code()).spawns();
                    final var spawn = spawns.forPlayer(player.getUUID()).orElse(spawns.defaultSpawn());
                    player.changeDimension(compactDim, SimpleTeleporter.to(spawn.position(), spawn.rotation()));
                });

                // Mark current room
                player.setData(Rooms.LAST_ROOM_ENTRYPOINT, RoomEntryPoint.playerEnteringMachine(player));
            }
        }
    }

    public static void teleportPlayerOutOfRoom(@Nonnull ServerPlayer serverPlayer) {
        if (!serverPlayer.level().dimension().equals(CompactDimension.LEVEL_KEY))
            return;

        MinecraftServer serv = serverPlayer.getServer();
        PlayerEntryPointHistory history = null;
        try {
            history = PlayerEntryPointHistory.forServer(serv, 5);
        } catch (MissingDimensionException e) {
            ForgePlayerUtil.teleportPlayerToRespawnOrOverworld(serv, serverPlayer);
        }

        if (history == null) {
            LOGS.error("Error: could not build historical data for players. BAD. Sending player to their overworld spawn instead..");
            ForgePlayerUtil.teleportPlayerToRespawnOrOverworld(serv, serverPlayer);
            return;
        }

        // Nice to have: serverPlayer.getDataOrElse(Rooms.LAST_ROOM_ENTRYPOINT, lastEntry -> {}, () -> {})
        if (!serverPlayer.hasData(Rooms.LAST_ROOM_ENTRYPOINT)) {
            // PlayerUtil.howDidYouGetThere(serverPlayer);
            ForgePlayerUtil.teleportPlayerToRespawnOrOverworld(serv, serverPlayer);
            history.clearHistory(serverPlayer);
        } else {
            final var lastEntry = serverPlayer.getData(Rooms.LAST_ROOM_ENTRYPOINT);

            final var location = lastEntry.entryLocation();
            assert serv != null;
            final var level = serv.getLevel(location.dimension());
            assert level != null;
            serverPlayer.changeDimension(level, SimpleTeleporter.to(location.position(), location.rotation()));

//            serv.submitAsync(() -> {
//                final PlayerEntryPointHistory h;
//                try {
//                    h = PlayerEntryPointHistory.forServer(serv, 5);
//                    final var stack = h.history(serverPlayer, 1);
//                    serverPlayer.setData(Rooms.LAST_ROOM_ENTRYPOINT, stack.peek());
//                } catch (MissingDimensionException e) {
//                    throw new RuntimeException(e);
//                }
//            });
        }

    }
}
