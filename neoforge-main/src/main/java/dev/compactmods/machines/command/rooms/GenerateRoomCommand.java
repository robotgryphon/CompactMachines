package dev.compactmods.machines.command.rooms;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.compactmods.machines.LoggingUtil;
import dev.compactmods.machines.api.CompactMachines;
import dev.compactmods.machines.api.dimension.MissingDimensionException;
import dev.compactmods.machines.api.room.template.RoomTemplateHelper;
import dev.compactmods.machines.command.argument.Suggestors;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;

public class GenerateRoomCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> make() {
        // /cm rooms
        final LiteralArgumentBuilder<CommandSourceStack> subRoot = LiteralArgumentBuilder.literal("generate");

        // TODO: /cm rooms create [size]

        // generate [template]
        subRoot.then(Commands.argument("template", IdentifierArgument.id())
                        .suggests(Suggestors.ROOM_TEMPLATES)
                        .executes(GenerateRoomCommand::exec));

        return subRoot;
    }

    private static int exec(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        final var src = ctx.getSource();
        final var player = src.getPlayerOrException();
        final var templateId = IdentifierArgument.getId(ctx, "template");

        final var template = RoomTemplateHelper.getTemplate(src.registryAccess(), templateId);

        try {
            final var instance = CompactMachines.newRoom(src.getServer(), template, player.getUUID());
            Component success = Component.literal(instance.code())
                    .withStyle(s -> s.withClickEvent(new ClickEvent.SuggestCommand("/compactmachines give existing " + instance.code()))
                            .withHoverEvent(new HoverEvent.ShowText(Component.literal("Click to give a new core")))
                            .withUnderlined(true));

            src.sendSuccess(() -> success, true);

        } catch (MissingDimensionException e) {
            LoggingUtil.modLog().error(e);
            src.sendFailure(Component.literal("Failed."));
        }

        return 0;
    }

}
