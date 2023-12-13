package dev.compactmods.machines.neoforge.network;

import dev.compactmods.machines.neoforge.room.RoomHelper;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.network.NetworkEvent;

public record PlayerRequestedTeleportPacket(GlobalPos machine, String room) {

    public PlayerRequestedTeleportPacket(FriendlyByteBuf buf) {
        this(buf.readJsonWithCodec(GlobalPos.CODEC), buf.readUtf());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeJsonWithCodec(GlobalPos.CODEC, machine);
        buf.writeUtf(room);
    }

    public boolean handle(NetworkEvent.Context ctx) {
        ctx.enqueueWork(() -> {
            final var player = ctx.getSender();
            if (player != null) {
                RoomHelper.teleportPlayerIntoMachine(player.level(), player, machine, room);
            }
        });

        return true;
    }
}
