package dev.compactmods.machines.core.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.common.IOUtilities;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

/**
 * A codec-backed file that stores a single data instance.
 *
 * @param <T>
 */
public class CMSingletonDataFileManager<T extends CMDataFile & CodecHolder<T>> implements IDataFileManager<T> {

    private final Logger logger = LogManager.getLogger();
    protected final MinecraftServer server;
    private final String dataKey;
    private T instance;

    public CMSingletonDataFileManager(MinecraftServer server, String dataKey, T instance) {
        this.server = server;
        this.dataKey = dataKey;
        this.instance = instance;
    }

    public void load() {
        final var file = instance.getDataLocation(server)
                .resolve(dataKey + ".dat")
                .toFile();

        if (file.exists()) {
            this.instance = DataFileUtil.loadFileWithCodec(file, instance.codec());
        }
    }

    public T data() {
        return this.instance;
    }

    private void ensureFileReady() {
        var dir = instance.getDataLocation(server);
        DataFileUtil.ensureDirExists(dir);
    }

    public void save() {
        if (instance != null) {
            ensureFileReady();

            var fullData = new CompoundTag();
            fullData.putString("version", instance.getDataVersion());
            fullData.store("data", instance.codec(), instance);

            try {
                IOUtilities.writeNbtCompressed(fullData, instance.getDataLocation(server).resolve(dataKey + ".dat"));
            } catch (IOException e) {
                logger.error("Failed to write data: " + e.getMessage(), e);
            }
        }
    }
}
