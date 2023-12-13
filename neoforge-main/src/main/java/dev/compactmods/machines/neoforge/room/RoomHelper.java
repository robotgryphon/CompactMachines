package dev.compactmods.machines.neoforge.room;

import dev.compactmods.compactmachines.api.room.IRoomInstance;
import dev.compactmods.compactmachines.api.room.Rooms;
import dev.compactmods.compactmachines.api.room.exceptions.NonexistentRoomException;
import dev.compactmods.machines.LoggingUtil;
import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.api.dimension.MissingDimensionException;
import dev.compactmods.machines.location.PreciseDimensionalPosition;
import dev.compactmods.machines.neoforge.dimension.SimpleTeleporter;
import dev.compactmods.machines.neoforge.util.ForgePlayerUtil;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public abstract class RoomHelper {

    private static final Logger LOGS = LoggingUtil.modLog();

    public static void teleportPlayerIntoMachine(Level machineLevel, ServerPlayer player, GlobalPos machinePos, String roomCode) {
        MinecraftServer serv = machineLevel.getServer();

        Rooms.registrar().get(roomCode).ifPresent(roomInfo -> {
            // Recursion check. Player tried to enter the room they're already in.
            if (player.level().dimension().equals(CompactDimension.LEVEL_KEY)) {
                final boolean recursion = roomInfo.chunks().hasChunk(player.chunkPosition());
                if (recursion) {
                    // TODO: Secret Advancement
                    // AdvancementTriggers.RECURSIVE_ROOMS.trigger(player);
                    return;
                }
            }

            try {
                final var entry = PreciseDimensionalPosition.fromPlayer(player);

                teleportPlayerIntoRoom(serv, player, roomInfo);

                // Mark the player as inside the machine, set external spawn, and yeet
                // FIXME - Player history
//                player.getCapability(RoomCapabilities.ROOM_HISTORY).ifPresent(hist -> {
//                    hist.addHistory(new PlayerRoomHistoryItem(entry, machinePos));
//                });
            } catch (MissingDimensionException | NonexistentRoomException e) {
                LOGS.fatal("Critical error; could not enter a freshly-created room instance.", e);
            }
        });
    }

    public static void setCurrentRoom(MinecraftServer server, ServerPlayer player, IRoomInstance room) {
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

    public static void teleportPlayerIntoRoom(MinecraftServer serv, ServerPlayer player, IRoomInstance room) throws MissingDimensionException, NonexistentRoomException {
        teleportPlayerIntoRoom(serv, player, room, null);
    }

    public static void teleportPlayerIntoRoom(MinecraftServer serv, ServerPlayer player, IRoomInstance room, @Nullable GlobalPos from)
            throws MissingDimensionException, NonexistentRoomException {
        final var compactDim = CompactDimension.forServer(serv);
        serv.submitAsync(() -> {
            final var spawn = room.spawns().forPlayer(player.getUUID()).orElse(room.spawns().defaultSpawn());
            player.changeDimension(compactDim, SimpleTeleporter.to(spawn.position(), spawn.rotation()));
        });

        if (from != null) {
            // Mark the player as inside the machine, set external spawn
            // FIXME - Player history
//            player.getCapability(RoomCapabilities.ROOM_HISTORY).ifPresent(hist -> {
//                var entry = PreciseDimensionalPosition.fromPlayer(player);
//                hist.addHistory(new PlayerRoomHistoryItem(entry, from));
//            });
        }

        // Mark current room, invalidates any listeners + debug screen
        RoomHelper.setCurrentRoom(serv, player, room);
    }

    public static void teleportPlayerOutOfRoom(@Nonnull ServerPlayer serverPlayer) {

        MinecraftServer serv = serverPlayer.getServer();
        if (!serverPlayer.level().dimension().equals(CompactDimension.LEVEL_KEY))
            return;

        // FIXME - GET ME OUT OF HERE
        ForgePlayerUtil.teleportPlayerToRespawnOrOverworld(serv, serverPlayer);

//        serverPlayer.getCapability(RoomCapabilities.ROOM_HISTORY)
//                .resolve()
//                .ifPresentOrElse(hist -> {
//                    if (hist.hasHistory()) {
//                        final var roomProvider = CompactRoomProvider.instance(serv);
//                        final IRoomHistoryItem prevArea = hist.pop();
//                        // Mark current room, invalidates any listeners + debug screen
//                        serverPlayer.getCapability(CURRENT_ROOM_META).ifPresent(provider -> {
//                            // Check entry dimension - if it isn't a machine room, clear room info
//                            if (!prevArea.getEntryLocation().dimension().equals(CompactDimension.LEVEL_KEY))
//                                provider.clearCurrent();
//                            else {
//                                roomProvider.findByChunk(prevArea.getEntryLocation().chunkPos()).ifPresent(roomMeta -> {
//                                    provider.setCurrent(new PlayerRoomMetadataProvider.CurrentRoomData(roomMeta.code(), roomMeta.owner(roomProvider)));
//                                });
//                            }
//                        });
//
//                        var spawnPoint = prevArea.getEntryLocation();
//                        final var enteredMachine = prevArea.getMachine().pos();
//
//                        final var level = spawnPoint.level(serv);
//                        serverPlayer.changeDimension(level, SimpleTeleporter.lookingAt(spawnPoint.position(), enteredMachine));
//                    } else {
//                        PlayerUtil.howDidYouGetThere(serverPlayer);
//
//                        hist.clear();
//                        ForgePlayerUtil.teleportPlayerToRespawnOrOverworld(serv, serverPlayer);
//                    }
//                }, () -> {
//                    PlayerUtil.howDidYouGetThere(serverPlayer);
//                    ForgePlayerUtil.teleportPlayerToRespawnOrOverworld(serv, serverPlayer);
//                });
    }
}
