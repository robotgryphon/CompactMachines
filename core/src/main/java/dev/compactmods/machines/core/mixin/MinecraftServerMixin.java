package dev.compactmods.machines.core.mixin;

import dev.compactmods.machines.core.attachment.IForwardingAttachmentHolder;
import dev.compactmods.machines.core.attachment.MinecraftServerAttachments;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.attachment.AttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.function.Supplier;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin implements IForwardingAttachmentHolder {

    @Unique
    private final AttachmentHolder cm_attachments = new AttachmentHolder.AsField(new MinecraftServerAttachments());

    @Override
    public Supplier<IAttachmentHolder> getAttachmentHolder() {
        return () -> cm_attachments;
    }
}
