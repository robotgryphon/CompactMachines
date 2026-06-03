package dev.compactmods.machines.client.machine.shader;

import dev.compactmods.machines.CMRegistries;
import dev.compactmods.machines.core.CompactMachinesCore;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Static registry of {@link FlagShader flags} that the {@code pride_stripes}
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

    public static final FlagShader WHITE = new FlagShader(List.of(CommonColors.WHITE));

    private static final Map<Identifier, FlagShader> BY_ID = new LinkedHashMap<>();
    static {
//        register(BAKER_PRIDE);
        register(WHITE);
    }

    /** Add a flag to the registry. Datapack loaders will call this once the
     *  resource-driven path lands; for now it's only used by the static block. */
    public static void register(FlagShader flag) {
//        BY_ID.put(flag.id(), flag);
    }

    public static List<FlagShader> all() {
        return List.copyOf(BY_ID.values());
    }

    public static Optional<FlagShader> byId(Identifier id) {
        return Optional.ofNullable(BY_ID.get(id));
    }
}
