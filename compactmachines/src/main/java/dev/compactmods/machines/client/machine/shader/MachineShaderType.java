package dev.compactmods.machines.client.machine.shader;

import com.mojang.serialization.MapCodec;

public record MachineShaderType<T extends MachineShader>(MapCodec<T> codec) {}
