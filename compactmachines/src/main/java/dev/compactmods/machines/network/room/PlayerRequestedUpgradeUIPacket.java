package dev.compactmods.machines.network.room;

import dev.compactmods.machines.core.CompactMachinesCore;
import dev.compactmods.machines.api.room.capability.RoomCapabilities;
import dev.compactmods.machines.server.CompactMachinesServer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

public record PlayerRequestedUpgradeUIPacket(String roomCode, boolean isIsolated) implements CustomPacketPayload {

    public static final Type<PlayerRequestedUpgradeUIPacket> TYPE = new Type<>(CompactMachinesCore.identifier("player_wants_to_open_room_upgrade_menu"));

    public static final IPayloadHandler<PlayerRequestedUpgradeUIPacket> HANDLER = (pkt, ctx) -> {
        if (ctx.player() instanceof ServerPlayer sp) {
            final var server = sp.level().getServer();
            server.getCapability(RoomCapabilities.REGISTRY)
                    .get(pkt.roomCode())
                    .ifPresent(roomInstance -> {
                        final var player = ctx.player();

                        player.sendOverlayMessage(Component.literal("Working on it! Try again later."));

//                    player.openMenu(RoomUpgradeMenu.provider(roomInstance), buf -> {
//                        buf.writeBoolean(pkt.isIsolated);
//                        buf.writeUtf(pkt.roomCode());
//                    });
                    });
        }
    };

    public static final StreamCodec<FriendlyByteBuf, PlayerRequestedUpgradeUIPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, PlayerRequestedUpgradeUIPacket::roomCode,
            ByteBufCodecs.BOOL, PlayerRequestedUpgradeUIPacket::isIsolated,
            PlayerRequestedUpgradeUIPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
