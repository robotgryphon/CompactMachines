package dev.compactmods.machines.shrinking.history;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.feather.node.Node;
import dev.compactmods.machines.shrinking.api.history.RoomEntryPoint;
import net.minecraft.core.UUIDUtil;

import java.util.UUID;

record PlayerEntryPointNode(UUID id, RoomEntryPoint data) implements Node<RoomEntryPoint> {
    public static final Codec<PlayerEntryPointNode> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            UUIDUtil.CODEC.fieldOf("id").forGetter(PlayerEntryPointNode::id),
            RoomEntryPoint.CODEC.fieldOf("data").forGetter(PlayerEntryPointNode::data)
    ).apply(inst, PlayerEntryPointNode::new));
}
