package dev.compactmods.machines.client.machine.shader;

import dev.compactmods.machines.CMDataAttachments;
import dev.compactmods.machines.client.config.ClientConfig;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.time.Month;
import java.util.Optional;

/**
 * Centralised "which shader should this machine render with?" decision.
 *
 * <p>Order:
 * <ol>
 *   <li>An explicit {@link CMDataAttachments#MACHINE_SHADER} on the holder.</li>
 *   <li>Date-driven default (Pride during June, if {@link ClientConfig#ENABLE_PRIDE_IN_JUNE} is on).</li>
 *   <li>Empty — caller renders the regular tinted-glass model.</li>
 * </ol>
 */
public final class MachineShaderResolver {

    private MachineShaderResolver() {}

    public static Optional<Identifier> resolve(@Nullable IAttachmentHolder holder) {
        if (holder != null) {
            Optional<Identifier> explicit = holder.getExistingData(CMDataAttachments.MACHINE_SHADER.get());
            if (explicit.isPresent()) return explicit;
        }
        return Optional.empty();
    }

    /** Pure-function variant for places that have no attachment holder yet (item icons, JEI). */
    public static Optional<FlagShader> dateDefault() {
        if (ClientConfig.ENABLE_PRIDE_IN_JUNE.get() && LocalDate.now().getMonth() == Month.JUNE) {
            return Optional.of(MachineFlags.WHITE);
        }
        return Optional.empty();
    }
}
