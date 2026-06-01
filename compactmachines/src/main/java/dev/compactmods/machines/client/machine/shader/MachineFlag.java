package dev.compactmods.machines.client.machine.shader;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

import java.util.List;

/**
 * A coloured-stripe flag that the Compact Machine can wear via the
 * {@code pride_stripes} shader. {@link #colors} is in <em>top-to-bottom</em>
 * order so the list reads the same way the flag itself does, and they're stored
 * as 24-bit packed RGB ({@code 0xRRGGBB}) — easy to construct from hex codes
 * and trivially codec-able once the registry moves to datapacks.
 *
 * <p>Limit: ten colours. Anything past that gets ignored because the shader
 * declares a fixed {@code vec3[10]} palette array.</p>
 *
 * @param id     unique identifier (also the source for the per-flag render
 *               pipeline's location).
 * @param colors stripe colours, top-to-bottom, packed {@code 0xRRGGBB}.
 */
public record MachineFlag(Identifier id, List<Integer> colors) {

    public static final int MAX_STRIPES = 10;

    public MachineFlag {
        if (colors.isEmpty()) {
            throw new IllegalArgumentException("MachineFlag '" + id + "' needs at least one colour");
        }
    }

    public int size() {
        return Math.min(colors.size(), MAX_STRIPES);
    }

    public float r(int idx) { return ((colors.get(idx) >> 16) & 0xFF) / 255f; }
    public float g(int idx) { return ((colors.get(idx) >>  8) & 0xFF) / 255f; }
    public float b(int idx) { return ((colors.get(idx))       & 0xFF) / 255f; }

    /** Codec for future datapack loading; not wired into a real registry yet. */
    public static final Codec<MachineFlag> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Identifier.CODEC.fieldOf("id").forGetter(MachineFlag::id),
            Codec.INT.listOf().fieldOf("colors").forGetter(MachineFlag::colors)
    ).apply(inst, MachineFlag::new));
}
