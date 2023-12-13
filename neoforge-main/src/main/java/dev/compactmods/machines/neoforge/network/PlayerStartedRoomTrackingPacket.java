package dev.compactmods.machines.neoforge.network;

import dev.compactmods.machines.api.dimension.MissingDimensionException;
import dev.compactmods.machines.neoforge.room.Rooms;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.neoforged.neoforge.network.NetworkEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public record PlayerStartedRoomTrackingPacket(String room) {

    public PlayerStartedRoomTrackingPacket(FriendlyByteBuf buf) {
        this(buf.readUtf());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(room);
    }

    public boolean handle(NetworkEvent.Context ctx) {
        var sender = ctx.getSender();
        ctx.enqueueWork(() -> {
            StructureTemplate blocks;
            try {
                blocks = Rooms.getInternalBlocks(sender.server, room).get(5, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException | MissingDimensionException e) {
                throw new RuntimeException(e);
            }
            RoomNetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> sender), new InitialRoomBlockDataPacket(blocks));
        });

        return true;
    }
}
