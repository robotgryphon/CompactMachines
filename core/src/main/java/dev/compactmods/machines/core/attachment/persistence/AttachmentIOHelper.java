package dev.compactmods.machines.core.attachment.persistence;

import dev.compactmods.machines.core.attachment.CMAttachmentHolder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.neoforged.neoforge.common.IOUtilities;

import java.io.IOException;
import java.nio.file.Path;

public final class AttachmentIOHelper {

    public static void save(RegistryAccess registryAccess, CMAttachmentHolder attachments, Path file) {
        try {
            var dataOut = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, registryAccess);
            attachments.persistenceHandler()
                    .serialize(dataOut);

            final var tag = dataOut.buildResult();
            IOUtilities.writeNbtCompressed(tag, file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void load(RegistryAccess registryAccess, CMAttachmentHolder attachments, Path file) {
        try {
            final var data = NbtIo.readCompressed(file, NbtAccounter.defaultQuota());
            var dataIn = TagValueInput.create(ProblemReporter.DISCARDING, registryAccess, data);
            attachments.persistenceHandler()
                    .deserialize(dataIn);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}
