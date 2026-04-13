package dev.compactmods.machines.core.data;

import com.mojang.serialization.Codec;

public interface CodecHolder<T> {

   Codec<T> codec();
}
