package dev.compactmods.machines.room.graph.node;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.feather.node.Node;
import net.minecraft.core.UUIDUtil;

import java.util.UUID;

public record RoomOwnerNode(UUID id, Data data) implements Node<RoomOwnerNode.Data> {

    public record Data(UUID owner) {
        public static final Codec<Data> CODEC = RecordCodecBuilder.create((i) -> i.group(
                UUIDUtil.CODEC.fieldOf("owner").forGetter(Data::owner)
        ).apply(i, Data::new));
    }
}
