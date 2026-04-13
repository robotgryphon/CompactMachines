package dev.compactmods.machines.util;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Math;

import java.util.stream.Stream;

public abstract class RandomSourceUtil {

    public static Stream<Vec3> randomVec3Stream(RandomSource source) {
        return Stream.generate(() -> randomVec3(source));
    }

    @NotNull
    public static Vec3 randomVec3(RandomSource source) {
        return new Vec3(source.nextDouble(), source.nextDouble(), source.nextDouble());
    }
}
