package dev.compactmods.machines.core.data;

import com.mojang.serialization.Codec;
import net.minecraft.server.MinecraftServer;

import java.nio.file.Path;

public interface CMDataFile<T> {

   default String getDataVersion() {
      return "1.0.0";
   }

   Path getDataLocation(MinecraftServer server);

   Codec<T> codec();
}
