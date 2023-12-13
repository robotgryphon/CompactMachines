package dev.compactmods.machines.neoforge.network;

import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.neoforge.util.VersionUtil;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.NetworkRegistry;
import net.neoforged.neoforge.network.PlayNetworkDirection;
import net.neoforged.neoforge.network.simple.SimpleChannel;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;

public class CompactMachinesNet {
    private static final ArtifactVersion PROTOCOL_VERSION = new DefaultArtifactVersion("6.0.0");

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Constants.MOD_ID, "main"),
            PROTOCOL_VERSION::toString,
            clientVer -> VersionUtil.checkMajor(clientVer, PROTOCOL_VERSION),
            serverVer -> VersionUtil.checkMajor(serverVer, PROTOCOL_VERSION)
    );


    public static void setupMessages() {
        CHANNEL.messageBuilder(PlayerRequestedTeleportPacket.class, 2, PlayNetworkDirection.PLAY_TO_SERVER)
                .encoder(PlayerRequestedTeleportPacket::encode)
                .decoder(PlayerRequestedTeleportPacket::new)
                .consumerMainThread(PlayerRequestedTeleportPacket::handle)
                .add();

        CHANNEL.messageBuilder(SyncRoomMetadataPacket.class, 3, PlayNetworkDirection.PLAY_TO_CLIENT)
                .encoder(SyncRoomMetadataPacket::encode)
                .decoder(SyncRoomMetadataPacket::new)
                .consumerMainThread(SyncRoomMetadataPacket::handle)
                .add();

        CHANNEL.messageBuilder(PlayerRequestedLeavePacket.class, 4, PlayNetworkDirection.PLAY_TO_SERVER)
                .encoder((pkt, buf) -> {})
                .decoder((buf) -> new PlayerRequestedLeavePacket())
                .consumerMainThread(PlayerRequestedLeavePacket::handle)
                .add();
    }
}
