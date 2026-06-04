package dev.compactmods.machines.client.machine.shader;

import com.mojang.serialization.Codec;
import dev.compactmods.machines.api.CompactMachines;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public interface MachineShader {

    Codec<MachineShader> DISPATCH_CODEC = Codec.lazyInitialized(() -> {
        final var reg = BuiltInRegistries.REGISTRY
                .getOptional(CompactMachines.identifier("machine_shaders"))
                .map(r -> (Registry<MachineShaderType<?>>) r);

        //noinspection unchecked
        return (Codec<MachineShader>) reg
                .map(Registry::byNameCodec)
                .map(c -> c.dispatchStable(MachineShader::type, MachineShaderType::codec))
                .orElseThrow(() -> new RuntimeException("Machine shader registry not registered yet; calling too early?"));
    });

    MachineShaderType<?> type();
}
