package dev.compactmods.machines;

import com.mojang.serialization.Codec;
import dev.compactmods.machines.core.machine.MachineColor;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.UUID;

public interface CMDataComponents {

    DeferredHolder<DataComponentType<?>, DataComponentType<MachineColor>> MACHINE_COLOR = CMRegistries.DATA_COMPONENTS
            .registerComponentType("machine_color", (builder) -> builder
                    .persistent(MachineColor.CODEC)
                    .networkSynchronized(MachineColor.STREAM_CODEC));



    DeferredHolder<DataComponentType<?>, DataComponentType<UUID>> UPGRADE_INSTANCE_ID = CMRegistries.DATA_COMPONENTS
            .registerComponentType("upgrade_id", (builder) -> builder
                    .persistent(UUIDUtil.CODEC)
                    .networkSynchronized(UUIDUtil.STREAM_CODEC));

    static void prepare() {}
}
