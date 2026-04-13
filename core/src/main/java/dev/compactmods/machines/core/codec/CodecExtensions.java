package dev.compactmods.machines.core.codec;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

public abstract class CodecExtensions {

    public static <T extends Enum<T> & StringRepresentable> StreamCodec<ByteBuf, T> stringRepresentableStreamCodec(T[] values) {
        final var lookup = StringRepresentable.createNameLookup(values, StringRepresentable::getSerializedName);
        return ByteBufCodecs.STRING_UTF8.map(lookup, T::getSerializedName);
    }
}
