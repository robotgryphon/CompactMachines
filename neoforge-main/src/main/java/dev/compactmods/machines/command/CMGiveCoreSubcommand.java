package dev.compactmods.machines.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.compactmods.machines.api.CompactMachines;
import dev.compactmods.machines.LoggingUtil;
import dev.compactmods.machines.i18n.CommandTranslations;
import dev.compactmods.machines.i18n.RoomTranslations;
import dev.compactmods.machines.api.room.template.RoomTemplateHelper;
import dev.compactmods.machines.command.argument.Suggestors;
import dev.compactmods.machines.machine.Machines;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.Logger;

public class CMGiveCoreSubcommand {

    private static final Logger LOGGER = LoggingUtil.modLog();

    public static LiteralArgumentBuilder<CommandSourceStack> make() {
        final var subRoot = Commands.literal("give")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS));


        // /cm give new [template]
        subRoot.then(Commands.literal("new")
                .then(Commands.argument("template", IdentifierArgument.id())
                        .suggests(Suggestors.ROOM_TEMPLATES)
                        .executes(CMGiveCoreSubcommand::giveNewMachineExecutor)));

        // /cm give existing [room-code]
        subRoot.then(Commands.literal("existing")
                .then(Commands.argument("room", StringArgumentType.string())
                        .suggests(Suggestors.ROOM_CODES)
                        .executes(CMGiveCoreSubcommand::giveExistingRoomExecutor)));

        // /cm give [player]
        var giveSpecificPlayer = Commands.argument("player", EntityArgument.player());

        // /cm give [player] new [template]
        giveSpecificPlayer.then(Commands.literal("new")
                        .then(Commands.argument("template", IdentifierArgument.id())
                            .suggests(Suggestors.ROOM_TEMPLATES)
                            .executes(CMGiveCoreSubcommand::giveNewMachineSpecificPlayer)));

        // /cm give [player] existing [room-code]
        giveSpecificPlayer.then(Commands.literal("existing")
                .then(Commands.argument("room", StringArgumentType.string())
                        .suggests(Suggestors.ROOM_CODES)
                        .executes(CMGiveCoreSubcommand::giveExistingRoomSpecificPlayer)));

        subRoot.then(giveSpecificPlayer);




        return subRoot;
    }


    private static int giveNewMachineExecutor(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        final var src = ctx.getSource();
        final var player = src.getPlayerOrException();
        final var templateId = IdentifierArgument.getId(ctx, "template");

        createAndGiveNewCore(src, templateId, player);

        return 0;
    }
    
    private static int giveNewMachineSpecificPlayer(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        final var src = ctx.getSource();
        final var player = EntityArgument.getPlayer(ctx, "player");
        final var templateId = IdentifierArgument.getId(ctx, "template");

        createAndGiveNewCore(src, templateId, player);

        return 0;
    }

    private static int giveExistingRoomExecutor(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        final var src = ctx.getSource();
        final var player = src.getPlayerOrException();
        final var roomCode = StringArgumentType.getString(ctx, "room");

        createAndGiveExistingRoom(roomCode, player, src);

        return 0;
    }

    private static int giveExistingRoomSpecificPlayer(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        final var src = ctx.getSource();
        final var player = EntityArgument.getPlayer(ctx, "player");
        final var roomCode = StringArgumentType.getString(ctx, "room");

        createAndGiveExistingRoom(roomCode, player, src);

        return 0;
    }

    private static void createAndGiveNewCore(CommandSourceStack src, Identifier templateId, ServerPlayer player) {

        final var template = RoomTemplateHelper.getTemplateHolder(src.getServer().registryAccess(), templateId);
        if(template.isBound()) {
            final var item = Machines.Items.forNewRoom(template);
            if (!player.addItem(item)) {
                src.sendFailure(CommandTranslations.CANNOT_GIVE_MACHINE.get());
            } else {
                src.sendSuccess(() -> CommandTranslations.MACHINE_GIVEN.apply(player), true);
            }
        } else {
            src.sendFailure(CommandTranslations.CANNOT_GIVE_MACHINE.get());
        }
    }

    private static void createAndGiveExistingRoom(String roomCode, ServerPlayer player, CommandSourceStack src) {
        CompactMachines.room(roomCode).ifPresentOrElse(room -> {
            ItemStack newItem = Machines.Items.boundToRoom(room.code());
            if (!player.addItem(newItem)) {
                src.sendFailure(CommandTranslations.CANNOT_GIVE_MACHINE.get());
            } else {
                src.sendSuccess(() -> CommandTranslations.MACHINE_GIVEN.apply(player), true);
            }
        }, () -> {
            LOGGER.error("Error giving player a new machine block: room not found.");
            src.sendFailure(RoomTranslations.UNKNOWN_ROOM_BY_CODE.apply(roomCode));
        });
    }
}

