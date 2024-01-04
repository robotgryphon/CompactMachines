package dev.compactmods.machines.neoforge.network;

import dev.compactmods.machines.api.core.Constants;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.registration.IPayloadRegistrar;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CMNetworks {

    public static IPayloadRegistrar main;

    @SubscribeEvent
    public static void onPacketRegistration(final RegisterPayloadHandlerEvent payloads) {
        main = payloads.registrar(Constants.MOD_ID)
                .versioned("6.0.0");

        main.play(PlayerRequestedTeleportPacket.ID, PlayerRequestedTeleportPacket.READER, builder ->
                builder.server(PlayerRequestedTeleportPacket.HANDLER));

        main.play(SyncRoomMetadataPacket.ID, SyncRoomMetadataPacket.READER, builder ->
                builder.client(SyncRoomMetadataPacket.HANDLER));

        main.play(PlayerRequestedLeavePacket.ID, (b) -> new PlayerRequestedLeavePacket(), builder ->
                builder.server(PlayerRequestedLeavePacket.HANDLER));
    }
}
