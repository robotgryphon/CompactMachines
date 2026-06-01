package dev.compactmods.machines.machine.block;

import dev.compactmods.machines.CMDataAttachments;
import dev.compactmods.machines.CMDataComponents;
import dev.compactmods.machines.api.room.capability.RoomCapabilities;
import dev.compactmods.machines.core.CompactMachinesCore;
import dev.compactmods.machines.core.machine.MachineColor;
import dev.compactmods.machines.core.machine.block.IBoundCompactMachineBlockEntity;
import dev.compactmods.machines.core.machine.block.ICompactMachineBlockEntity;
import dev.compactmods.machines.machine.Machines;
import dev.compactmods.machines.machine.ui.MachineUIMenu;
import dev.compactmods.machines.network.machine.MachineColorSyncPacket;
import dev.compactmods.machines.server.CompactMachinesServer;
import dev.compactmods.machines.shrinking.PersonalShrinkingDevice;
import dev.compactmods.machines.shrinking.Shrinking;
import dev.compactmods.machines.shrinking.api.ShrinkingDeviceConfiguration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
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
import org.jspecify.annotations.NonNull;

import javax.annotation.ParametersAreNonnullByDefault;

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
            CompactMachinesCore.modLog().warn("Warning: tried to pick block on a bound machine that does not have a room bound.", ex);
            return Machines.Items.MACHINE.toStack();
        }
    }

    @NotNull
    protected static InteractionResult tryDyingMachine(ServerLevel level, @NotNull BlockPos pos, Player player, ItemStack mainItem) {
        final var dye = mainItem.get(DataComponents.DYE);
        final var blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof ICompactMachineBlockEntity cmbe) {
            final var newColor = MachineColor.fromDyeColor(dye);
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

    @NonNull
    @Override
    @ParametersAreNonnullByDefault
    protected InteractionResult useItemOn(ItemStack mainItem, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {

        if (level.isClientSide() || !(player instanceof ServerPlayer serverPlayer) || !(level instanceof ServerLevel sl))
            return InteractionResult.SUCCESS;

        final var server = sl.getServer();
        final var roomRegistry = server.getCapability(RoomCapabilities.REGISTRY);

        if (mainItem.isEmpty())
            return InteractionResult.TRY_WITH_EMPTY_HAND;

        if (mainItem.has(DataComponents.DYE)) {
            return tryDyingMachine(sl, pos, player, mainItem);
        }

        final var tile = level.getBlockEntity(pos, Machines.BlockEntities.MACHINE.get())
                .orElse(null);

        if (tile == null)
            return InteractionResult.FAIL;

        if (mainItem.has(CMDataComponents.BOUND_ROOM_CODE)) {
            boolean yay = tile.setCore(serverPlayer, mainItem);
            return yay ? InteractionResult.SUCCESS : InteractionResult.FAIL;
        }

        // Try to teleport player into room
        if (mainItem.has(Shrinking.DataComponents.SHRINKING_CONFIG)) {
            var config = mainItem.getOrDefault(Shrinking.DataComponents.SHRINKING_CONFIG, ShrinkingDeviceConfiguration.DEFAULT_CONFIG);

            tile.connectedRoom().flatMap(roomRegistry::get).ifPresent(room -> {
                final var shrinkHandler = serverPlayer.getCapability(Shrinking.SHRINK, room);
                if (shrinkHandler == null) {
                    CompactMachinesCore.modLog().error("Error: Could not fetch or create a shrinking handler for room roomCode [{}], player [{}].",
                            room.code(), player.getUUID());

                    return;
                }

                shrinkHandler.tryEnter(tile.getLevelPosition(), config).thenAccept(result -> {
                    if (result.successful()) {
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
        if (level.isClientSide() || player instanceof FakePlayer) {
            return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
        }

        if (player.isShiftKeyDown() && player.getMainHandItem().isEmpty()) {
            InteractionResult.Success successServer = tryRemoveCore(level, pos, player);
            if (successServer != null) return successServer;
        }

        if (player instanceof ServerPlayer sp) {
            final var machinePos = GlobalPos.of(level.dimension(), pos);
            sp.setData(CMDataAttachments.OPEN_MACHINE_POS, machinePos);

            sp.openMenu(new SimpleMenuProvider(
                    (int i, Inventory _, Player p) -> new MachineUIMenu(i, p, machinePos),
                    Component.empty()
            ), buffer -> {
                buffer.writeGlobalPos(machinePos);
            });
//                                                PacketDistributor.sendToPlayer(sp,
//                                                        new OpenMachinePreviewScreenPacket(GlobalPos.of(level.dimension(), pos), roomCode)
//                                                );
        }

        return InteractionResult.SUCCESS;
    }

    private static InteractionResult.@org.jspecify.annotations.Nullable Success tryRemoveCore(Level level, BlockPos pos, Player player) {
        if (level.getBlockEntity(pos) instanceof IBoundCompactMachineBlockEntity bound) {
            final var core = bound.popCore(player);
            player.setItemSlot(EquipmentSlot.MAINHAND, core);
            return InteractionResult.SUCCESS_SERVER;
        }
        return null;
    }
}
