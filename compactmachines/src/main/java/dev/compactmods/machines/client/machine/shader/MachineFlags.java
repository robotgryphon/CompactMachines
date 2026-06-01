package dev.compactmods.machines.client.machine.shader;

import dev.compactmods.machines.core.CompactMachinesCore;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Static registry of {@link MachineFlag flags} that the {@code pride_stripes}
 * shader can render. Keep it small for now — the interesting evolution is to
 * load these from datapacks and merge them in at resource reload.
 *
 * <p>Each entry here is the source of truth that
 * {@link MachineFlagRenderTypes} consumes at
 * {@code RegisterRenderPipelinesEvent} time to build one RenderPipeline per
 * flag (palette baked in via shader defines).</p>
 */
public final class MachineFlags {

    private MachineFlags() {}

    /** Baker 6-stripe pride flag (1978 original, top to bottom). */
    public static final MachineFlag BAKER_PRIDE = new MachineFlag(
            CompactMachinesCore.identifier("flag/baker_pride"),
            List.of(
                    0xE40303, // red
                    0xFF8C00, // orange
                    0xFFED00, // yellow
                    0x008026, // green
                    0x24408E, // blue
                    0x732982  // violet
            ));

    public static final MachineFlag PURPLE = new MachineFlag(
            CompactMachinesCore.identifier("purple"),
            List.of(CommonColors.DARK_PURPLE));

    private static final Map<Identifier, MachineFlag> BY_ID = new LinkedHashMap<>();
    static {
        register(BAKER_PRIDE);
        register(PURPLE);
    }

    /** Add a flag to the registry. Datapack loaders will call this once the
     *  resource-driven path lands; for now it's only used by the static block. */
    public static void register(MachineFlag flag) {
        BY_ID.put(flag.id(), flag);
    }

    public static List<MachineFlag> all() {
        return List.copyOf(BY_ID.values());
    }

    public static Optional<MachineFlag> byId(Identifier id) {
        return Optional.ofNullable(BY_ID.get(id));
    }
}
