package dev.compactmods.machines.api.room.generation;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record RoomDimensions(int width, int depth, int height) {
    private static final Codec<RoomDimensions> FULL_CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.intRange(3, 45).fieldOf("width").forGetter(RoomDimensions::width),
            Codec.intRange(3, 45).fieldOf("depth").forGetter(RoomDimensions::depth),
            Codec.intRange(3, 45).fieldOf("height").forGetter(RoomDimensions::height)
    ).apply(inst, RoomDimensions::new));

    public static final Codec<RoomDimensions> CODEC = Codec.of(FULL_CODEC, Decoder.INSTANCE);

    public static final StreamCodec<ByteBuf, RoomDimensions> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

    public RoomDimensions(int cubicSize) {
        this(Math.min(cubicSize, 45), Math.min(cubicSize, 45), Math.min(cubicSize, 45));
    }

    public static RoomDimensions cubic(int cubic) {
        return new RoomDimensions(cubic);
    }

    @Override
    public String toString() {
        return "%s x %s x %s".formatted(width, depth, height);
    }

    private static class Decoder implements com.mojang.serialization.Decoder<RoomDimensions> {

        public static final Decoder INSTANCE = new Decoder();

        @Override
        public <T> DataResult<Pair<RoomDimensions, T>> decode(DynamicOps<T> dynamicOps, T t) {
            final var asNum = dynamicOps.withParser(Codec.intRange(3, 45)).apply(t);
            if (asNum.isSuccess())
                return asNum.result()
                        .map(singleSize -> DataResult.success(Pair.of(RoomDimensions.cubic(singleSize), t))).orElseThrow();

            final var asObj = dynamicOps.withParser(FULL_CODEC).apply(t);
            if (asObj.isSuccess())
                return asObj.result()
                        .map(dims -> DataResult.success(Pair.of(dims, t))).orElseThrow();

            return DataResult.error(() -> "Dimensions must either be a single integer between 3-45, or specify width/depth/height.");
        }
    }
}
