package dev.compactmods.machines.neoforge.network;

import dev.compactmods.machines.neoforge.room.RoomHelper;
import net.neoforged.neoforge.network.NetworkEvent;

public record PlayerRequestedLeavePacket() {

    public void handle(NetworkEvent.Context context) {
        final var sender = context.getSender();
        RoomHelper.teleportPlayerOutOfRoom(sender);
    }
}
