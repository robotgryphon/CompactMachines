package dev.compactmods.machines.room.graph.node;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.machines.api.room.spatial.RoomBoundaries;
import dev.compactmods.feather.node.Node;
import net.minecraft.core.UUIDUtil;

import java.util.UUID;

/**
 * Hosts core information about a machine room, such as how large it is and its roomCode.
 */
public record RoomRegistrationNode(UUID id, Data data) implements Node<RoomRegistrationNode.Data> {

    public static final Codec<RoomRegistrationNode> CODEC = RecordCodecBuilder.create(i -> i.group(
            UUIDUtil.CODEC.fieldOf("id").forGetter(RoomRegistrationNode::id),
            Data.CODEC.fieldOf("data").forGetter(RoomRegistrationNode::data)
    ).apply(i, RoomRegistrationNode::new));

    public record Data(String code, RoomBoundaries boundaries) {
        public static final Codec<Data> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.STRING.fieldOf("roomCode").forGetter(Data::code),
                RoomBoundaries.MAP_CODEC.fieldOf("bounds").forGetter(Data::boundaries)
        ).apply(i, Data::new));
    }

    public String code() {
        return data.code;
    }
}
