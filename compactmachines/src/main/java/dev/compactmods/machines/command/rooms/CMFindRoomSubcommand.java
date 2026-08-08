package dev.compactmods.machines.command.rooms;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.compactmods.machines.api.room.capability.RoomCapabilities;
import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.api.machine.MachineConstants;
import dev.compactmods.machines.machine.i18n.MachineTranslations;
import dev.compactmods.machines.room.RoomTranslations;
import dev.compactmods.machines.machine.block.CompactMachineBlockEntity;
import dev.compactmods.machines.room.Rooms;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.ColumnPosArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.NotNull;

public class CMFindRoomSubcommand {
    static @NotNull LiteralArgumentBuilder<CommandSourceStack> create() {
        // /cm rooms find ...
        final var find = Commands.literal("find");

        // /cm rooms find chunk
        find.then(Commands.literal("chunk").then(
                // /cm rooms find chunk [pos]
                Commands.argument("chunk", ColumnPosArgument.columnPos())
                        .executes(CMFindRoomSubcommand::fetchByChunkPos)
        ));

        // /cm rooms find connected_to
        find.then(Commands.literal("connected_to").then(
                // /cm rooms find connected_to [pos]
                Commands.argument("pos", BlockPosArgument.blockPos())
                        .executes(CMFindRoomSubcommand::fetchByMachineBlock)
        ));

        // /cm rooms find player
        find.then(Commands.literal("player").then(
                // /cm rooms find player [@p]
                Commands.argument("player", EntityArgument.player())
                        .executes(CMFindRoomSubcommand::findByContainingPlayer)
        ));

        find.then(Commands.literal("owner").then(
                Commands.argument("owner", EntityArgument.player())
                        .executes(CMFindRoomSubcommand::findByOwner)
        ));

        return find;
    }

    private static int fetchByChunkPos(CommandContext<CommandSourceStack> ctx) {
        final var server = ctx.getSource().getServer();
        final var chunkManager = server.getCapability(RoomCapabilities.CHUNK_MANAGER);

        final var chunkPos = ColumnPosArgument.getColumnPos(ctx, "chunk");

        final var m = chunkManager
                .findRoomByChunk(chunkPos.toChunkPos())
                // FIXME Translations
                .map(code -> Component.translatableWithFallback("commands.cm.room_by_chunk", "Room at chunk %s has ID: %s", chunkPos.toString(), code))
                .orElse(Component.literal("Room not found at chunk: " + chunkPos));

        ctx.getSource().sendSuccess(() -> m, false);

        return 0;
    }

    private static int fetchByMachineBlock(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        final var block = BlockPosArgument.getLoadedBlockPos(ctx, "pos");
        final var level = ctx.getSource().getLevel();

        if (!level.getBlockState(block).is(MachineConstants.MACHINE_BLOCK)) {
            ctx.getSource().sendFailure(MachineTranslations.NOT_A_MACHINE_BLOCK.apply(block));
            return -1;
        }

        final var server = ctx.getSource().getServer();
        final var registry = server.getCapability(RoomCapabilities.REGISTRY);
        if (level.getBlockEntity(block) instanceof CompactMachineBlockEntity be) {
            be.connectedRoom()
                    .flatMap(registry::get)
                    .ifPresent(roomInfo -> {
                        ctx.getSource().sendSuccess(() -> RoomTranslations.MACHINE_ROOM_INFO.apply(block, roomInfo), false);
                    });
        } else {
            // FIXME Translations
            ctx.getSource().sendFailure(Component.literal("Does not appear to be a bound machine block."));
        }

        return 0;
    }

    private static int findByContainingPlayer(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        final var source = ctx.getSource();

        final var player = EntityArgument.getPlayer(ctx, "player");

        if (!player.level().dimension().equals(CompactDimension.LEVEL_KEY)) {
            source.sendFailure(RoomTranslations.PLAYER_NOT_IN_COMPACT_DIM.apply(player));
            return -1;
        }

        final var server = ctx.getSource().getServer();
        final var chunkManager = server.getCapability(RoomCapabilities.CHUNK_MANAGER);

        final var maybeRoom = chunkManager.findRoomByChunk(player.chunkPosition());

        maybeRoom.ifPresentOrElse(room -> {
            final var roomPreamble = RoomTranslations.PLAYER_ROOM_INFO.apply(player, room.code());

            final var teleportSenderIntoRoom = Component.literal("Teleport to Room")
                    .withStyle(s -> s.withClickEvent(new ClickEvent.SuggestCommand("/compactmachines tp " + room.code()))
                            .withHoverEvent(new HoverEvent.ShowText(Component.literal("Click to teleport into the room")))
                            .withUnderlined(true)
                            .withColor(DyeColor.CYAN.getTextColor()));

            final var ejectPlayerFromRoom = Component.literal("Eject ")
                    .append(Component.literal(player.nameAndId().name()))
                    .append(Component.literal(" from Room"))
                    .withStyle(s -> s.withClickEvent(new ClickEvent.SuggestCommand("/compactmachines eject " + player.nameAndId().id()))
                            .withHoverEvent(new HoverEvent.ShowText(Component.literal("Click to eject " + player.nameAndId().name() + " back to their spawn")))
                            .withUnderlined(true)
                            .withColor(DyeColor.PINK.getTextColor()));

            ;
            source.sendSuccess(() -> CommonComponents.joinLines(roomPreamble,
                    CommonComponents.EMPTY,
                    Component.literal(" ")
                            .append(teleportSenderIntoRoom)
                            .append(Component.literal(" - "))
                            .append(ejectPlayerFromRoom)), false);

        }, () -> {
            final var notFound = RoomTranslations.UNKNOWN_ROOM_BY_PLAYER_CHUNK.apply(player);
            source.sendFailure(notFound);
        });

        return 0;
    }

    public static int findByOwner(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        final var owner = EntityArgument.getPlayer(ctx, "owner");
        final var source = ctx.getSource();

        final var server = ctx.getSource().getServer();
        final var registry = server.getCapability(RoomCapabilities.REGISTRY);
        final var owned = registry.allRooms()
                .filter(i -> i.getExistingData(Rooms.DataAttachments.ROOM_OWNER).map(id -> id.equals(owner)).orElse(false))
                .toList();

        // TODO Localization
        if (owned.isEmpty()) {
            source.sendSuccess(() -> Component.literal("No rooms found."), false);
        } else {
            owned.forEach(instance -> source.sendSuccess(() -> Component.literal("Room: " + instance.code()), false));
        }


        return 0;
    }
}
