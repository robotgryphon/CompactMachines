package dev.compactmods.machines.core.data;

import net.minecraft.server.MinecraftServer;

import java.nio.file.Path;

public interface CMDataFile {

   default String getDataVersion() {
      return "1.0.0";
   }

   Path getDataLocation(MinecraftServer server);
}
