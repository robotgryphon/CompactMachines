package dev.compactmods.machines.neoforge.network;

import dev.compactmods.machines.neoforge.room.client.ClientRoomPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.network.NetworkEvent;

import java.util.UUID;

public record SyncRoomMetadataPacket(String roomCode, UUID owner) {
    public SyncRoomMetadataPacket(FriendlyByteBuf buffer) {
        this(buffer.readUtf(), buffer.readUUID());
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUtf(roomCode);
        buffer.writeUUID(owner);
    }

    public void handle(NetworkEvent.Context context) {
        ClientRoomPacketHandler.handleRoomSync(this.roomCode, this.owner);
        context.setPacketHandled(true);
    }
}
