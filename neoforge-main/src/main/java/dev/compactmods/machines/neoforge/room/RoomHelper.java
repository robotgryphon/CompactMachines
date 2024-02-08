package dev.compactmods.machines.neoforge.room;

import dev.compactmods.machines.api.room.RoomApi;
import dev.compactmods.machines.api.room.RoomInstance;
import dev.compactmods.machines.api.room.history.RoomEntryPoint;
import dev.compactmods.machines.LoggingUtil;
import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.api.dimension.MissingDimensionException;
import dev.compactmods.machines.neoforge.dimension.SimpleTeleporter;
import dev.compactmods.machines.neoforge.util.ForgePlayerUtil;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;

public abstract class RoomHelper {

    private static final Logger LOGS = LoggingUtil.modLog();

    public static void teleportPlayerIntoMachine(Level machineLevel, ServerPlayer player, GlobalPos machinePos, String roomCode) {
        MinecraftServer serv = machineLevel.getServer();

        RoomApi.room(roomCode).ifPresent(roomInfo -> {
            // Recursion check. Player tried to enter the room they're already in.
            if (player.level().dimension().equals(CompactDimension.LEVEL_KEY)) {
                final boolean recursion = roomInfo.chunks().get().hasChunk(player.chunkPosition());
                if (recursion) {
                    // TODO: Secret Advancement
                    // AdvancementTriggers.RECURSIVE_ROOMS.trigger(player);
                    return;
                }
            }

            try {
                teleportPlayerIntoRoom(serv, player, roomInfo);
            } catch (MissingDimensionException e) {
                LOGS.fatal("Critical error; could not enter a freshly-created room instance.", e);
            }
        });
    }

    public static void setCurrentRoom(MinecraftServer server, ServerPlayer player, RoomInstance room) {
        // Mark current room, invalidates any listeners + debug screen
        // FIXME - Data attachment/packet sync current room to client
//        final var roomProvider = CompactRoomProvider.instance(server);
//        final var roomOwner = room.owner(roomProvider);
//        player.getCapability(CURRENT_ROOM_META).ifPresent(provider -> {
//            provider.setCurrent(new PlayerRoomMetadataProvider.CurrentRoomData(room.code(), roomOwner));
//        });

//        final var sync = new SyncRoomMetadataPacket(room.code(), roomOwner);
//        CompactMachinesNet.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), sync);
    }

    public static void teleportPlayerIntoRoom(MinecraftServer serv, ServerPlayer player, RoomInstance room)
            throws MissingDimensionException {
        final var compactDim = CompactDimension.forServer(serv);

        addEntryForPlayer(player);

        serv.submitAsync(() -> {
            final var spawns = room.spawns().get().spawns();
            final var spawn = spawns.forPlayer(player.getUUID()).orElse(spawns.defaultSpawn());
            player.changeDimension(compactDim, SimpleTeleporter.to(spawn.position(), spawn.rotation()));
        });

        // Mark current room, invalidates any listeners + debug screen
        RoomHelper.setCurrentRoom(serv, player, room);
    }

    private static void addEntryForPlayer(ServerPlayer player) {
        // Mark the player as inside the machine, set external spawn
        player.setData(Rooms.LAST_ROOM_ENTRYPOINT, RoomEntryPoint.fromPlayer(player));
        // TODO: Full history tracking here
        // RoomApi.playerTracking(player).push();
    }

    public static void teleportPlayerOutOfRoom(@Nonnull ServerPlayer serverPlayer) {

        MinecraftServer serv = serverPlayer.getServer();
        if (!serverPlayer.level().dimension().equals(CompactDimension.LEVEL_KEY))
            return;

        // Nice to have: serverPlayer.getDataOrElse(Rooms.LAST_ROOM_ENTRYPOINT, lastEntry -> {}, () -> {})
        if(!serverPlayer.hasData(Rooms.LAST_ROOM_ENTRYPOINT)) {
            // PlayerUtil.howDidYouGetThere(serverPlayer);
            ForgePlayerUtil.teleportPlayerToRespawnOrOverworld(serv, serverPlayer);
        } else {
            final var lastEntry = serverPlayer.getData(Rooms.LAST_ROOM_ENTRYPOINT);

            final var location = lastEntry.entryLocation();
            assert serv != null;
            final var level = serv.getLevel(location.dimension());
            assert level != null;
            serverPlayer.changeDimension(level, SimpleTeleporter.to(location.position(), location.rotation()));

            // TODO: FULL HISTORY TRACKING
            serverPlayer.removeData(Rooms.LAST_ROOM_ENTRYPOINT);
        }

    }
}
