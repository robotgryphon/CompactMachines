package dev.compactmods.machines.neoforge.command.subcommand;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.compactmods.compactmachines.api.room.Rooms;
import dev.compactmods.compactmachines.api.room.exceptions.NonexistentRoomException;
import dev.compactmods.machines.api.core.CMCommands;
import dev.compactmods.machines.i18n.TranslationUtil;
import dev.compactmods.machines.neoforge.config.ServerConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class SpawnSubcommand {

    public static ArgumentBuilder<CommandSourceStack, ?> make() {
        final var spawnRoot = Commands.literal("spawn");

        final var resetSpawn = Commands.literal("reset")
                .requires(cs -> cs.hasPermission(ServerConfig.changeRoomSpawn()))
                .then(Commands.argument("room", StringArgumentType.string())
                        .executes(SpawnSubcommand::resetRoomSpawn));

        spawnRoot.then(resetSpawn);

        return spawnRoot;
    }

    private static int resetRoomSpawn(CommandContext<CommandSourceStack> ctx) {
        final var src = ctx.getSource();
        final var roomCode = StringArgumentType.getString(ctx, "room");

        try {
            final var roomProvider = Rooms.spawnManager(roomCode);

            // FIXME roomProvider.setDefaultSpawn();
            src.sendSuccess(() -> TranslationUtil.command(CMCommands.SPAWN_CHANGED_SUCCESSFULLY, "%s".formatted(roomCode)), true);
            return 0;
        } catch (NonexistentRoomException e) {
            src.sendFailure(TranslationUtil.command(CMCommands.ROOM_NOT_FOUND, roomCode));
            return -1;
        }
    }
}
