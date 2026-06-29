package dev.compactmods.machines.core.data.manager;

import com.mojang.serialization.Codec;
import dev.compactmods.machines.core.data.CMDataFile;
import dev.compactmods.machines.core.data.DataFileUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.common.IOUtilities;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * A codec-backed file that stores several instances of typed data, indexed by a key.
 *
 * @param <Key> The key used for instance lookups.
 * @param <T>
 */
public class CMKeyedDataFileManager<Key, T extends CMDataFile<T>> implements IKeyedDataFileManager<Key, T> {

    protected final MinecraftServer server;
    private final HashMap<Key, T> cache;

    private final Path rootDirectory;
    private final Supplier<Codec<T>> valueCodec;

    public CMKeyedDataFileManager(MinecraftServer server, Supplier<Codec<T>> codec,
                                  Path rootDirectory) {
        this.server = server;
        this.cache = new HashMap<>();
        this.valueCodec = codec;
        this.rootDirectory = rootDirectory;
    }

    /// Used to get the filename for a given item, if `key.toString()` is not sufficient.
    ///
    /// @param key
    /// @return
    public String getFileKey(Key key) {
        return key.toString();
    }

    @Override
    @Nullable
    public T data(Key key) {
        return cache.computeIfAbsent(key, k -> {
            final var file = getDataFile(k);
            return !file.exists() ? null : DataFileUtil.loadFileWithCodec(file, valueCodec.get());
        });
    }

    @Override
    public Optional<T> optionalData(Key key) {
        return hasData(key) ? Optional.ofNullable(data(key)) : Optional.empty();
    }

    @Override
    public void setData(Key key, @NonNull T data) {
        cache.put(key, data);
        save(key, data);
    }

    public void save() {
        cache.forEach(this::save);
    }

    private void save(Key key, T data) {
        var fullData = new CompoundTag();
        fullData.putString("version", data.getDataVersion());
        fullData.store("data", data.codec(), data);

        try {
            IOUtilities.writeNbtCompressed(fullData, rootDirectory.resolve(getFileKey(key) + ".dat"));
        } catch (IOException e) {
            var logger = LogManager.getLogger();
            logger.error("Failed to write data: {}", e.getMessage(), e);
        }
    }

    public Stream<String> existingFiles() {
        try {
            return FileUtils
                    .streamFiles(rootDirectory.toFile(), false)
                    .map(File::getName);
        } catch (IOException e) {
            return Stream.empty();
        }
    }

    private File getDataFile(Key key) {
        DataFileUtil.ensureDirExists(rootDirectory);
        return rootDirectory.resolve(getFileKey(key) + ".dat").toFile();
    }

    private boolean hasFile(Key key) {
        return getDataFile(key).exists();
    }

    public boolean hasData(Key key) {
        return cache.containsKey(key) || hasFile(key);
    }
}
