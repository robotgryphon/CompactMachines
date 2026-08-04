package dev.compactmods.machines.upgrades.example;

import com.google.common.base.Predicates;
import dev.compactmods.machines.api.room.RoomInstance;
import dev.compactmods.machines.upgrades.api.RoomUpgradeComponent;
import dev.compactmods.machines.upgrades.api.RoomUpgradeComponentType;
import dev.compactmods.machines.upgrades.api.event.lifecycle.TickingRoomUpgradeComponent;
import dev.compactmods.machines.upgrades.RoomUpgrades;
import dev.compactmods.machines.upgrades.storage.LocatedResourceStorage;
import dev.compactmods.machines.upgrades.storage.ResourceTypes;
import dev.compactmods.machines.upgrades.storage.StorageCapabilities;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.CommonColors;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class TreeCutterUpgradeComponent implements RoomUpgradeComponent, TickingRoomUpgradeComponent {

    public static void prepare() {
    }

    @Override
    public void addToTooltip(Item.TooltipContext tooltipContext, Consumer<Component> consumer, TooltipFlag tooltipFlag, DataComponentGetter dataComponentGetter) {
        final var c = Component.literal("Tree Cutter")
                .withColor(CommonColors.LIGHT_GRAY);

        consumer.accept(c);
    }

    @Override
    public RoomUpgradeComponentType<TreeCutterUpgradeComponent> getType() {
        return RoomUpgrades.TREECUTTER.get();
    }

    public void tick(RoomInstance instance) {
        final var level = instance.level();
        if(level.isClientSide() || !(level instanceof ServerLevel serverLevel))
            return;

        final var everythingLoaded = instance.boundaries()
                .innerChunkPositions()
                .allMatch(cp -> level.shouldTickBlocksAt(cp.pack()));

        if (!everythingLoaded)
            return;

        final var innerBounds = instance.boundaries().innerBounds();

//        final var upgradeItem = instance.upgradeItem();
//        var energyHandler = ItemAccess.forStack(upgradeItem).getCapability(Capabilities.Energy.ITEM);

        int maxAllowed = 5;
//        if (upgradeItem.isDamageableItem()) {
//            behavior = SuccessfulActionBehavior.DamageItem;
//            var durabilityLeft = upgradeItem.getMaxDamage() - upgradeItem.getDamageValue();
//            maxAllowed = Math.clamp(durabilityLeft, 0, 5);
//        }

//        if (energyHandler != null) {
//            behavior = SuccessfulActionBehavior.DrainEnergy;
//            maxAllowed = Math.clamp(energyHandler.getAmountAsLong() / 10, 0, 10);
//        }

        final var treeBlocks = BlockPos.betweenClosedStream(innerBounds)
                .map(pos -> {
                    final var state = level.getBlockState(pos);
                    return Pair.of(pos.immutable(), state);
                })
                .filter(pair -> {
                    BlockState state = pair.right();
                    if (state.is(BlockTags.LOGS)) return true;
                    if (state.is(BlockTags.LEAVES)) {
                        if (state.hasProperty(LeavesBlock.PERSISTENT)) return !state.getValue(LeavesBlock.PERSISTENT);
                        return true;
                    }

                    return false;
                })
                .limit(maxAllowed)
                .collect(Collectors.toUnmodifiableSet());

        final var numLogs = treeBlocks.size();

        if (!treeBlocks.isEmpty()) {
            final var storageCache = instance.getCapability(StorageCapabilities.ROOM_STORAGE);
            if (storageCache == null)
                return;

            final var inventories = storageCache.resources(instance, ResourceTypes.ITEM_BLOCK.get()).toList();

            // If we have no valid inventories, do nothing
            if (inventories.isEmpty())
                return;

            for (Pair<BlockPos, BlockState> pos : treeBlocks) {
                breakSingleBlock(pos, serverLevel, inventories);
            }
        }
    }

    private static void breakSingleBlock(Pair<BlockPos, BlockState> pos, ServerLevel level, List<LocatedResourceStorage<ItemResource>> inventories) {
        final var blockEntity = level.getBlockEntity(pos.left());

        final var drops = Block.getDrops(pos.right(), level, pos.left(), blockEntity);
        ResourceHandler<ItemResource> memory = new ItemStacksResourceHandler(NonNullList.copyOf(drops));

        try (final var blockTx = Transaction.openRoot()) {
            if (!drops.isEmpty()) {
                int totalDrops = drops.size();
                int totalMoved = 0;
                for (final var cornerInv : inventories) {
                    totalMoved += ResourceHandlerUtil.move(memory, cornerInv.handler(), Predicates.alwaysTrue(), Integer.MAX_VALUE, blockTx);
                }

                // We only commit if the entire drop set was moved
                if (ResourceHandlerUtil.isEmpty(memory) && (totalMoved == totalDrops)) {
                    level.destroyBlock(pos.left(), false);
                    blockTx.commit();
                }
            }
        }
    }

}
