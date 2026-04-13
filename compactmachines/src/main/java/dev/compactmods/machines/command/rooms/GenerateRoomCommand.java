package dev.compactmods.machines.command.rooms;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.compactmods.machines.api.room.capability.RoomCapabilities;
import dev.compactmods.machines.core.CompactMachinesCore;
import dev.compactmods.machines.api.dimension.MissingDimensionException;
import dev.compactmods.machines.api.room.template.RoomTemplateHelper;
import dev.compactmods.machines.command.argument.Suggestors;
import dev.compactmods.machines.core.capability.CapabilityHelper;
import dev.compactmods.machines.room.RoomCodeGenerator;
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
            final var generator = CapabilityHelper.server(ctx.getSource().getServer(), RoomCapabilities.GENERATOR);

            final var generationDetails = generator.createNew()
                    .owner(player.getUUID())
                    .template(template)
                    .build();

            final var newCode = RoomCodeGenerator.generateRoomId();
            generator.generate(newCode, generationDetails);

            Component success = Component.literal(newCode)
                    .withStyle(s -> s.withClickEvent(new ClickEvent.SuggestCommand("/CompactMachinesCore give existing " + newCode))
                            .withHoverEvent(new HoverEvent.ShowText(Component.literal("Click to give a new core")))
                            .withUnderlined(true));

            src.sendSuccess(() -> success, true);

        } catch (MissingDimensionException e) {
            CompactMachinesCore.modLog().error(e);
            src.sendFailure(Component.literal("Failed."));
        }

        return 0;
    }

}
