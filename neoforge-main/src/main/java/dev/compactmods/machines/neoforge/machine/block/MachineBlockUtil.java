package dev.compactmods.machines.neoforge.machine.block;

import dev.compactmods.machines.api.core.Messages;
import dev.compactmods.machines.i18n.TranslationUtil;
import dev.compactmods.machines.machine.EnumMachinePlayersBreakHandling;
import dev.compactmods.machines.neoforge.config.ServerConfig;
import dev.compactmods.machines.neoforge.machine.entity.BoundCompactMachineBlockEntity;
import dev.compactmods.machines.neoforge.room.RoomHelper;
import dev.compactmods.machines.neoforge.room.ui.MachineRoomMenu;
import dev.compactmods.machines.util.PlayerUtil;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.NameTagItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.UUID;

public class MachineBlockUtil {

    @Nonnull
    static InteractionResult tryRoomTeleport(Level level, BlockPos pos, ServerPlayer player) {
        // Try teleport to compact machine dimension
        if (level.getBlockEntity(pos) instanceof BoundCompactMachineBlockEntity tile) {
            RoomHelper.teleportPlayerIntoMachine(level, player, tile.getLevelPosition(), tile.connectedRoom());
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    /**
     * Gets destroy progress, without any lookups on owner, internal players, etcetera.
     *
     * @param state
     * @param player
     * @param worldIn
     * @param pos
     * @return
     */
    public static float destroyProgressUnchecked(BlockState state, Player player, BlockGetter worldIn, BlockPos pos) {
        int baseSpeedForge = CommonHooks.isCorrectToolForDrops(state, player) ? 30 : 100;
        return player.getDigSpeed(state, pos) / baseSpeedForge;
    }

    public static float destroyProgress(BlockState state, Player player, BlockGetter worldIn, BlockPos pos) {
        float normalHardness = destroyProgressUnchecked(state, player, worldIn, pos);

        BoundCompactMachineBlockEntity tile = (BoundCompactMachineBlockEntity) worldIn.getBlockEntity(pos);
        if (tile == null)
            return normalHardness;

        boolean hasPlayers = tile.hasPlayersInside();

        // If there are players inside, check config for break handling
        if (hasPlayers) {
            EnumMachinePlayersBreakHandling hand = ServerConfig.MACHINE_PLAYER_BREAK_HANDLING.get();
            switch (hand) {
                case UNBREAKABLE:
                    return 0;

                case OWNER:
                    Optional<UUID> ownerUUID = tile.getOwnerUUID();
                    return ownerUUID
                            .map(uuid -> player.getUUID() == uuid ? normalHardness : 0)
                            .orElse(normalHardness);

                case ANYONE:
                    return normalHardness;
            }
        }

        // No players inside - let anyone break it
        return normalHardness;
    }

    public static void roomPreviewScreen(BlockPos pos, ServerPlayer player, MinecraftServer server, BoundCompactMachineBlockEntity machine) {
        final var roomCode = machine.connectedRoom();

        NetworkHooks.openScreen(player, MachineRoomMenu.makeProvider(server, roomCode, machine.getLevelPosition()), (buf) -> {
            buf.writeBlockPos(pos);
            buf.writeJsonWithCodec(GlobalPos.CODEC, machine.getLevelPosition());
            buf.writeUtf(roomCode);

            // FIXME Renamable rooms
//            roomName.ifPresentOrElse(name -> {
//                buf.writeBoolean(true);
//                buf.writeUtf(name);
//            }, () -> {
                buf.writeBoolean(false);
                buf.writeUtf("");
//            });
        });
    }

    @Nullable
    public static InteractionResult tryApplyNametag(Level level, BlockPos pos, Player player) {
        ItemStack mainItem = player.getMainHandItem();
        if (mainItem.getItem() instanceof NameTagItem && mainItem.hasCustomHoverName()) {
            if (level.getBlockEntity(pos) instanceof BoundCompactMachineBlockEntity tile) {
                final var ownerProfile = tile.getOwnerUUID().flatMap(id -> PlayerUtil.getProfileByUUID(level, id));
                boolean isOwner = ownerProfile.map(p -> p.getId().equals(player.getUUID())).orElse(false);
                boolean isOp = player.hasPermissions(Commands.LEVEL_MODERATORS);

                if (ownerProfile.isEmpty()) {
                    return InteractionResult.FAIL;
                }

                if (!isOp || !isOwner)
                    return InteractionResult.FAIL;
                else {
                    ownerProfile.ifPresent(owner -> {
                        player.displayClientMessage(TranslationUtil.message(Messages.CANNOT_RENAME_NOT_OWNER,
                                owner.getName()), true);
                    });
                }

                final var newName = mainItem.getHoverName().getString(120);
                // FIXME Renamable rooms Rooms.updateName(level.getServer(), tile.connectedRoom(), newName);
            }
        }
        return null;
    }
}
