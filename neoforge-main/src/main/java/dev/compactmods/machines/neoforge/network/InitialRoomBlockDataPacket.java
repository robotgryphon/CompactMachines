package dev.compactmods.machines.neoforge.network;

import dev.compactmods.machines.neoforge.room.client.ClientRoomPacketHandler;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.neoforged.neoforge.network.NetworkEvent;

public record InitialRoomBlockDataPacket(StructureTemplate blocks) {

    public static InitialRoomBlockDataPacket fromNetwork(FriendlyByteBuf buf) {
        final var nbt = buf.readNbt();

        final var struct = new StructureTemplate();
        struct.load(BuiltInRegistries.BLOCK.asLookup(), nbt);

        return new InitialRoomBlockDataPacket(struct);
    }

    public void toNetwork(FriendlyByteBuf buf) {
        final var tag = blocks.save(new CompoundTag());
        buf.writeNbt(tag);
    }

    public boolean handle(NetworkEvent.Context ctx) {
        ctx.enqueueWork(() -> ClientRoomPacketHandler.handleBlockData(this.blocks));
        return true;
    }
}
