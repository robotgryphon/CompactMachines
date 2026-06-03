package dev.compactmods.machines.client.machine.shader;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.machines.api.CompactMachines;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import java.util.List;

/// A coloured-stripe flag that the Compact Machine can wear via the
/// `pride_stripes` shader. [#colors] is in _top-to-bottom_
/// order so the list reads the same way the flag itself does, and they're stored
/// as 24-bit packed RGB (`0xRRGGBB`) &mdash; easy to construct from hex codes
/// and trivially codec-able once the registry moves to datapacks.
///
/// Limit: ten colours. Anything past that gets ignored because the shader
/// declares a fixed `vec3[10]` palette array.
///
/// @param colors stripe colours, top-to-bottom, packed `0xRRGGBB`.
public record FlagShader(List<Integer> colors) {

    public static final int MAX_STRIPES = 10;

    public static final ResourceKey<Registry<FlagShader>> REGISTRY_KEY = ResourceKey.createRegistryKey(CompactMachines.identifier("flag_shaders"));

    public int size() {
        return Math.min(colors.size(), MAX_STRIPES);
    }

    public float r(int idx) { return ((colors.get(idx) >> 16) & 0xFF) / 255f; }
    public float g(int idx) { return ((colors.get(idx) >>  8) & 0xFF) / 255f; }
    public float b(int idx) { return ((colors.get(idx))       & 0xFF) / 255f; }

    /** Codec for future datapack loading; not wired into a real registry yet. */
    public static final Codec<FlagShader> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.INT.listOf().fieldOf("colors").forGetter(FlagShader::colors)
    ).apply(inst, FlagShader::new));
}
