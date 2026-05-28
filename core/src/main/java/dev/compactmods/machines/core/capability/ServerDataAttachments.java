package dev.compactmods.machines.core.capability;

import dev.compactmods.machines.core.attachment.MinecraftServerAttachments;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.attachment.IAttachmentHolder;

import java.util.Map;

public class ServerDataAttachments {

    private static final Map<MinecraftServer, IAttachmentHolder> INSTANCES = new Reference2ObjectOpenHashMap<>();

    public static IAttachmentHolder getInstance(MinecraftServer server) {
        return INSTANCES.computeIfAbsent(server, (s) -> new MinecraftServerAttachments());
    }

    public static void cleanup(MinecraftServer server) {
        INSTANCES.remove(server);
    }
}

