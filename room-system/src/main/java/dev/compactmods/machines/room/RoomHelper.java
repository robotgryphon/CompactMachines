// FIXME: Reorganization
//package dev.compactmods.machines.room;
//
//import dev.compactmods.machines.api.room.capability.RoomCapabilities;
//import dev.compactmods.machines.api.room.RoomInstance;
//import dev.compactmods.machines.api.room.history.IPlayerEntryPointHistoryManager;
//import dev.compactmods.machines.api.room.history.RoomEntryPoint;
//import dev.compactmods.machines.api.dimension.CompactDimension;
//import dev.compactmods.machines.api.room.history.RoomEntryResult;
//import dev.compactmods.machines.api.room.history.RoomExitResult;
//import net.minecraft.ChatFormatting;
//import net.minecraft.util.Util;
//import net.minecraft.core.GlobalPos;
//import net.minecraft.network.chat.Component;
//import net.minecraft.server.MinecraftServer;
//import net.minecraft.server.level.ServerPlayer;
//import net.minecraft.world.level.Level;
//import net.neoforged.neoforge.network.PacketDistributor;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//import org.jetbrains.annotations.NotNull;
//
//import javax.annotation.Nonnull;
//import java.util.concurrent.CompletableFuture;
//
//public abstract class RoomHelper {
//
//    private static final Logger LOGS = LogManager.getLogger();
//
//    public static CompletableFuture<RoomEntryResult> teleportPlayerIntoMachine(Level machineLevel, ServerPlayer player, GlobalPos machinePos, String roomCode) {
//        MinecraftServer serv = machineLevel.getServer();
//
//        LOGS.debug("Player {} entering machine at: {}", player.getName(), machinePos);
//
//        var registrar = RoomCapabilities.REGISTRY.getCapability(serv);
//        return registrar.get(roomCode)
//                .map(roomInfo -> teleportPlayerIntoRoom(serv, player, roomInfo, RoomEntryPoint.playerEnteringMachine(player)))
//                .orElse(CompletableFuture.completedFuture(RoomEntryResult.FAILED_ROOM_INVALID));
//    }
//
//}
