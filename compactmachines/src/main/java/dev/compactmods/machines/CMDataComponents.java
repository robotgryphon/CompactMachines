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

    String KEY_ROOM_TEMPLATE = "room_template";
    String KEY_ROOM_CODE = "room_code";
    String KEY_MACHINE_COLOR = "machine_color";

    /**
     * Only on bound room items - given by a crafting process or when a bound machine block is broken
     */

    DeferredHolder<DataComponentType<?>, DataComponentType<String>> BOUND_ROOM_CODE = CMRegistries.DATA_COMPONENTS
            .registerComponentType(KEY_ROOM_CODE, (builder) -> builder
                    .persistent(Codec.STRING)
                    .networkSynchronized(ByteBufCodecs.STRING_UTF8));


    DeferredHolder<DataComponentType<?>, DataComponentType<MachineColor>> MACHINE_COLOR = CMRegistries.DATA_COMPONENTS
            .registerComponentType(KEY_MACHINE_COLOR, (builder) -> builder
                    .persistent(MachineColor.CODEC)
                    .networkSynchronized(MachineColor.STREAM_CODEC));

    /**
     * Only on new room items - IUnboundMachineItem
     */
    DeferredHolder<DataComponentType<?>, DataComponentType<Identifier>> ROOM_TEMPLATE_ID = CMRegistries.DATA_COMPONENTS
            .registerComponentType(KEY_ROOM_TEMPLATE, (builder) -> builder
                    .persistent(Identifier.CODEC)
                    .networkSynchronized(Identifier.STREAM_CODEC));

    DeferredHolder<DataComponentType<?>, DataComponentType<UUID>> UPGRADE_INSTANCE_ID = CMRegistries.DATA_COMPONENTS
            .registerComponentType("upgrade_id", (builder) -> builder
                    .persistent(UUIDUtil.CODEC)
                    .networkSynchronized(UUIDUtil.STREAM_CODEC));

    static void prepare() {}
}
