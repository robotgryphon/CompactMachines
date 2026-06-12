package dev.compactmods.machines.core.data.attachments;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.machines.core.data.CMDataFile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import net.neoforged.neoforge.attachment.AttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentHolder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Function;

/// An implementation of [CMDataFile] that implements [IAttachmentHolder].
///
/// Effectively a combination of the following systems:
/// - [net.neoforged.neoforge.attachment.LevelAttachmentsSavedData]
/// - [IAttachmentHolder]
/// - [CMDataFile]
public abstract class AttachmentBasedDataFile<T extends AttachmentHolder, TAdditionalData> extends AttachmentHolder implements CMDataFile<T>, IAttachmentHolder {

    protected MinecraftServer server;
    protected final Codec<T> codec;
    private final MapCodec<TAdditionalData> additionalDataCodec;
    private final Function<AttachmentDataFileFactoryInput<TAdditionalData>, T> factory;

    // Told you we need a better codec/attachment system
    private final Method deserializeAttachments = ObfuscationReflectionHelper.findMethod(AttachmentHolder.class, "deserializeAttachments", ValueInput.class);

    protected AttachmentBasedDataFile(MinecraftServer server, MapCodec<TAdditionalData> additionalDataCodec, Function<AttachmentDataFileFactoryInput<TAdditionalData>, T> factory) {
        this.server = server;
        this.additionalDataCodec = additionalDataCodec;
        this.factory = factory;
        this.codec = makeCodec(server);
    }

    protected abstract TAdditionalData dataSupplier(T instance);

    public Codec<T> makeCodec(MinecraftServer server) {
        return RecordCodecBuilder.create(i -> i.group(
                CompoundTag.CODEC.fieldOf("attachments").forGetter(inst -> {
                    var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, server.registryAccess());
                    inst.serializeAttachments(output);
                    return output.buildResult();
                }),

                additionalDataCodec.forGetter(this::dataSupplier)
        ).apply(i, (ct, data) -> {
            final var template = factory.apply(new AttachmentDataFileFactoryInput<>(server, ct, data));
            try {
                deserializeAttachments.invoke(template, TagValueInput.create(ProblemReporter.DISCARDING, server.registryAccess(), ct));
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
            return template;
        }));
    }
}
