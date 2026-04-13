package dev.compactmods.machines.shrinking;

import dev.compactmods.machines.api.dimension.CompactDimensionTransitions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ShrinkingHelper {
    public static void teleportPlayerToRespawnOrOverworld(MinecraftServer serv, @NotNull ServerPlayer player) {
        final var config = player.getRespawnConfig();

        player.removeData(Shrinking.CURRENT_ROOM_CODE);
        player.removeData(Shrinking.LAST_ROOM_ENTRYPOINT);

        final var transition = Optional.ofNullable(config)
                .map(c -> CompactDimensionTransitions.to(serv.getLevel(c.respawnData().dimension()), Vec3.atBottomCenterOf(c.respawnData().pos())))
                .orElse(CompactDimensionTransitions.to(serv.overworld(),
                        Vec3.atBottomCenterOf(serv.overworld().getRespawnData().pos())));

        player.teleport(transition);
    }
}
