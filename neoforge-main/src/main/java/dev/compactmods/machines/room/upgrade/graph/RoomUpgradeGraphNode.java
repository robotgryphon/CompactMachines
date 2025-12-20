package dev.compactmods.machines.room.upgrade.graph;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.feather.node.Node;
import net.minecraft.resources.Identifier;

import java.util.UUID;

public record RoomUpgradeGraphNode(UUID id, Data data) implements Node<RoomUpgradeGraphNode.Data> {

    public record Data(Identifier key) {
        public static final Codec<Data> CODEC = RecordCodecBuilder.create(i -> i.group(
                Identifier.CODEC.fieldOf("upgrade").forGetter(Data::key)
        ).apply(i, Data::new));
    }
}
