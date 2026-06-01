package dev.compactmods.machines;

import com.google.common.base.Predicates;
import dev.compactmods.machines.api.CompactMachines;
import dev.compactmods.machines.core.CompactMachinesCore;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.Optional;
import java.util.function.Supplier;

public interface CMDataAttachments {

    DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, CompactMachinesCore.MOD_ID);

    Supplier<AttachmentType<GlobalPos>> OPEN_MACHINE_POS = ATTACHMENT_TYPES.register("open_machine", () -> AttachmentType
            .builder(() -> GlobalPos.of(Level.OVERWORLD, BlockPos.ZERO))
            .serialize(GlobalPos.MAP_CODEC, Predicates.alwaysFalse())
            .build());

    /**
     * Selects which renderer drives a Compact Machine's glass faces.
     */
    Supplier<AttachmentType<Identifier>> MACHINE_SHADER = ATTACHMENT_TYPES.register("machine_shader",
            () -> AttachmentType.builder(() -> CompactMachines.identifier("none"))
                    .serialize(Identifier.CODEC.fieldOf("shader_id"), id -> !(id.getNamespace().equals(CompactMachines.MOD_ID) && id.getPath().equals("none")))
                    .build());

    static void prepare() {
    }

}
