package dev.compactmods.machines.neoforge.machine.block;

import dev.compactmods.machines.LoggingUtil;
import dev.compactmods.machines.api.shrinking.PSDTags;
import dev.compactmods.machines.machine.item.ICompactMachineItem;
import dev.compactmods.machines.neoforge.machine.Machines;
import dev.compactmods.machines.neoforge.machine.entity.BoundCompactMachineBlockEntity;
import dev.compactmods.machines.neoforge.machine.item.BoundCompactMachineItem;
import dev.compactmods.machines.neoforge.machine.item.UnboundCompactMachineItem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BoundCompactMachineBlock extends CompactMachineBlock implements EntityBlock {
    public BoundCompactMachineBlock(Properties props) {
        super(props);
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) {
        if (level.getBlockEntity(pos) instanceof BoundCompactMachineBlockEntity be) {
            return BoundCompactMachineItem.createForRoom(be.connectedRoom(), be.getColor());
        }

        LoggingUtil.modLog().warn("Warning: tried to pick block on a machine that does not have an associated block entity.");
        return UnboundCompactMachineItem.unbound();
    }

    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        return MachineBlockUtil.destroyProgress(state, player, level, pos);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (!level.isClientSide) {
            level.getBlockEntity(pos, Machines.MACHINE_ENTITY.get()).ifPresent(tile -> {
                // force client redraw
                final int color = ICompactMachineItem.getMachineColor(stack);
                tile.setColor(color);

                BoundCompactMachineItem.getRoom(stack).ifPresent(tile::setConnectedRoom);
            });
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BoundCompactMachineBlockEntity(pos, state);
    }

    @NotNull
    @Override
    public InteractionResult use(@NotNull BlockState state, Level level, @NotNull BlockPos pos, Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hitResult) {
        MinecraftServer server = level.getServer();
        ItemStack mainItem = player.getMainHandItem();
        if (mainItem.is(PSDTags.ITEM) && player instanceof ServerPlayer sp) {
            return MachineBlockUtil.tryRoomTeleport(level, pos, sp);
        }

        // All other items, open preview screen
        if(!level.isClientSide) {
            level.getBlockEntity(pos, Machines.MACHINE_ENTITY.get()).ifPresent(machine -> {
                MachineBlockUtil.roomPreviewScreen(pos, (ServerPlayer) player, server, machine);
            });
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
