package dev.compactmods.machines.capabilities;

import dev.compactmods.machines.api.room.capability.RoomCapabilities;
import dev.compactmods.machines.api.room.capability.RoomCapability;
import dev.compactmods.machines.core.capability.ServerCapability;
import dev.compactmods.machines.server.CompactMachinesServer;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public class BasicRoomCapabilities {

    public static void register(RegisterCapabilitiesEvent ignored) {

        ServerCapability.registerVoid(RoomCapabilities.GENERATOR, (server, _) -> {
            var caps = CompactMachinesServer.caps(server);
            return caps.roomGenerator();
        });

        RoomCapability.register(RoomCapabilities.ROOM_DATA_ATTACHMENTS, (server, roomCode, _) -> {
            var caps = CompactMachinesServer.caps(server);
            return caps.roomDataAttachments(roomCode);
        });
    }
}
