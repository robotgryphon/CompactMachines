package dev.compactmods.machines;

import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentType;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.UUID;

public interface CMDataComponents {

    DeferredHolder<DataComponentType<?>, DataComponentType<UUID>> UPGRADE_INSTANCE_ID = CMRegistries.DATA_COMPONENTS
            .registerComponentType("upgrade_id", (builder) -> builder
                    .persistent(UUIDUtil.CODEC)
                    .networkSynchronized(UUIDUtil.STREAM_CODEC));

    static void prepare() {}
}
