package dev.compactmods.machines.core.capability;

import dev.compactmods.machines.core.CompactMachinesCore;
import dev.compactmods.machines.core.attachment.IForwardingAttachmentHolder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Util;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public abstract class CapabilityHelper {

    private static final Map<ServerCapability<?, ?>, ServerCapRegistration<?,?>> SERVER_CAPABILITY_SUPPLIER_MAP = new HashMap<>();

    public record ServerCapRegistration<SC extends ServerCapability<T, Void>, T>(SC instance,
                                                                                 Supplier<AttachmentType<SC>> attachmentTypeSupplier) {
    }

    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, CompactMachinesCore.MOD_ID);

    public static <SC extends ServerCapability<T, Void>, T> ServerCapRegistration<SC, T>
    registerServerCap(SC construct) {
        var original = construct.name();
        var sanitized = original.toString();
        if(original.getNamespace().equals(CompactMachinesCore.MOD_ID))
            sanitized = original.getPath().toString();
        else
            sanitized = original.toString().replace(':', '_');

        Supplier<AttachmentType<SC>> lookup = ATTACHMENT_TYPES.register(sanitized, () -> AttachmentType.builder(() -> construct).build());
        final var reg = new ServerCapRegistration<SC,T>(construct, lookup);
        SERVER_CAPABILITY_SUPPLIER_MAP.putIfAbsent(construct, reg);
        return reg;
    }

    @NotNull
    public static <T, SC extends ServerCapability<T,Void>> T server(MinecraftServer server, SC capability) {
        var caps = ServerDataAttachments.getInstance(server);
        var lookup = SERVER_CAPABILITY_SUPPLIER_MAP.get(capability);
        if (lookup != null) {
            var a = caps.getData(lookup.attachmentTypeSupplier);
            if (capability.typeClass().equals(a.typeClass()))
                return capability.typeClass().cast(a);
        }
        
        throw new RuntimeException(":(");
    }

    public static void registerAliases(IEventBus modBus) {
        ATTACHMENT_TYPES.register(modBus);
    }
}
