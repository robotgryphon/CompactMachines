package dev.compactmods.machines.network;

import dev.compactmods.machines.machine.network.MachineColorSyncPacket;
import dev.compactmods.machines.network.machine.OpenMachinePreviewScreenPacket;
import dev.compactmods.machines.network.room.RoomNetworking;
import dev.compactmods.machines.shrinking.network.ShrinkingNetworking;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/// Central payload-registration listener. Delegates to each feature's own
/// network handler (main → feature is fine); remaining direct registrations
/// are for packets not yet relocated to a feature.
public class CMNetworks {

    public static void onPacketRegistration(final RegisterPayloadHandlersEvent payloads) {
        final PayloadRegistrar main = payloads.registrar("7.1.0");

        // Per-feature handlers
        RoomNetworking.register(main);
        ShrinkingNetworking.register(main);

        // Machine packets — not yet relocated
        main.playToClient(MachineColorSyncPacket.TYPE, MachineColorSyncPacket.STREAM_CODEC, MachineColorSyncPacket.HANDLER);
        main.playToClient(OpenMachinePreviewScreenPacket.TYPE, OpenMachinePreviewScreenPacket.STREAM_CODEC, OpenMachinePreviewScreenPacket.HANDLER);
    }
}
