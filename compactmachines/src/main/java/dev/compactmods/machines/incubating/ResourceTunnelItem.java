// FIXME
// package dev.compactmods.machines.incubating;
//
//import dev.compactmods.machines.core.CompactMachinesCore;
//import dev.compactmods.machines.api.dimension.CompactDimension;
//import dev.compactmods.machines.server.CompactMachinesServer;
//import net.minecraft.network.chat.Component;
//import net.minecraft.server.level.ServerPlayer;
//import net.minecraft.world.InteractionResult;
//import net.minecraft.world.item.Item;
//import net.minecraft.world.item.context.UseOnContext;
//import net.minecraft.world.level.ChunkPos;
//import net.neoforged.neoforge.transfer.resource.Resource;
//
//public class ResourceTunnelItem<TResource extends Resource> extends Item {
//
//    public ResourceTunnelItem(Properties properties) {
//        super(properties);
//    }
//
//    @Override
//    public InteractionResult useOn(UseOnContext context) {
//        final var level = context.getLevel();
//        if(level.isClientSide() || !CompactDimension.isLevelCompact(level))
//            return InteractionResult.PASS;
//
//        final var maybeUser = context.getPlayer();
//        final var pos = context.getClickedPos();
//        final var targetDir = context.getClickedFace().getOpposite();
//
//        // Get machines bound to room
//        if(maybeUser instanceof ServerPlayer player) {
//            final var server = level.getServer();
//
//            final var room = CompactMachinesServer.caps(server)
//                    .chunkManager()
//                    .findRoomByChunk(ChunkPos.containing(pos))
//                    .flatMap(CompactMachinesCore::room);
//
//            room.ifPresentOrElse(instance -> {
//                    player.sendOverlayMessage(Component.literal(instance.roomCode()));
//                }
//        }, () -> {
//
//        });
//    }
//
//        return InteractionResult.PASS;
//    }
//}
