package dev.compactmods.machines.machine.block;

import dev.compactmods.machines.LoggingUtil;
import dev.compactmods.machines.api.CompactMachines;
import dev.compactmods.machines.api.attachment.CMDataAttachments;
import dev.compactmods.machines.api.component.CMDataComponents;
import dev.compactmods.machines.api.machine.MachineColor;
import dev.compactmods.machines.api.machine.block.ICompactMachineBlockEntity;
import dev.compactmods.machines.machine.Machines;
import dev.compactmods.machines.network.machine.MachineColorSyncPacket;
import dev.compactmods.machines.network.machine.OpenMachinePreviewScreenPacket;
import dev.compactmods.machines.room.RoomBlocks;
import dev.compactmods.machines.room.RoomHelper;
import dev.compactmods.machines.room.Rooms;
import dev.compactmods.machines.shrinking.PersonalShrinkingDevice;
import dev.compactmods.machines.shrinking.Shrinking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ExecutionException;

public class CompactMachineBlock extends Block implements EntityBlock {
    public CompactMachineBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData, Player player) {
        try {
            if (level.getBlockEntity(pos) instanceof CompactMachineBlockEntity be) {
                if (includeData && be.connectedRoom().isPresent()) {
                    final var room = be.connectedRoom().get();
                    final var stack = Machines.Items.boundToRoom(room);

                    be.getCustomName().ifPresent(cn -> {
                        stack.set(DataComponents.CUSTOM_NAME, cn);
                    });

                    return stack;
                } else {
                    return Machines.Items.MACHINE.toStack();
                }
            }

            return Machines.Items.MACHINE.toStack();
        } catch (Exception ex) {
            LoggingUtil.modLog().warn("Warning: tried to pick block on a bound machine that does not have a room bound.", ex);
            return Machines.Items.MACHINE.toStack();
        }
    }

    @NotNull
    protected static InteractionResult tryDyingMachine(ServerLevel level, @NotNull BlockPos pos, Player player, DyeItem dye, ItemStack mainItem) {
        // TODO Support IColorable once https://github.com/neoforged/NeoForge/pull/1094 is merged
        var color = dye.getDyeColor();
        final var blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof ICompactMachineBlockEntity cmbe) {
            final var newColor = MachineColor.fromDyeColor(color);
            cmbe.setMachineColor(newColor);

            PacketDistributor.sendToPlayersTrackingChunk(
                    level, ChunkPos.containing(pos), new MachineColorSyncPacket(GlobalPos.of(level.dimension(), pos), newColor));

            if (!player.isCreative())
                mainItem.shrink(1);

            return InteractionResult.CONSUME;
        }

        return InteractionResult.FAIL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CompactMachineBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack mainItem, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {

        if (level.isClientSide() || !(player instanceof ServerPlayer serverPlayer) || !(level instanceof ServerLevel sl))
            return InteractionResult.SUCCESS;

        if (mainItem.getItem() instanceof DyeItem dye) {
            return tryDyingMachine(sl, pos, player, dye, mainItem);
        }

        final var tile = level.getBlockEntity(pos, Machines.BlockEntities.MACHINE.get())
                .orElse(null);

        if (tile == null)
            return InteractionResult.FAIL;

        if(mainItem.has(CMDataComponents.BOUND_ROOM_CODE)) {
            boolean yay = tile.setCore(serverPlayer, mainItem);
            return yay ? InteractionResult.SUCCESS : InteractionResult.FAIL;
        }

        if (mainItem.has(Shrinking.DataComponents.SHRINKING_CONFIG)) {
            // Try to teleport player into room
            tile.connectedRoom().ifPresent(roomCode -> {
                RoomHelper.teleportPlayerIntoMachine(level, serverPlayer, tile.getLevelPosition(), roomCode).thenAccept(result -> {
                    if (result.successful()) {
                        var config = PersonalShrinkingDevice.config(mainItem);
                        PersonalShrinkingDevice.handleSuccessfulAtomicShift(mainItem, serverPlayer, config);
                    }
                });
            });

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        // All other items, open preview screen
        if (!level.isClientSide() && !(player instanceof FakePlayer)) {
            level.getBlockEntity(pos, Machines.BlockEntities.MACHINE.get())
                    .ifPresent(machine -> {

                        if(player.isShiftKeyDown() && player.getMainHandItem().isEmpty()) {
                            final var coreItem = machine.popCore(player);
                        } else {
                            machine.connectedRoom()
                                    .ifPresent(roomCode -> {
                                        CompactMachines.room(roomCode).ifPresent(inst -> {
                                            if (player instanceof ServerPlayer sp) {
                                                sp.setData(CMDataAttachments.OPEN_MACHINE_POS, GlobalPos.of(level.dimension(), pos));

                                                PacketDistributor.sendToPlayer(sp,
                                                        new OpenMachinePreviewScreenPacket(GlobalPos.of(level.dimension(), pos), roomCode)
                                                );
                                            }
                                        });
                                    });
                        }
                    });
        }

        return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
    }
}
