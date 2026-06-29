package dev.compactmods.machines.core.data;

import com.mojang.serialization.Codec;

public interface CMDataFile<T> {

   default String getDataVersion() {
      return "1.0.0";
   }

   Codec<T> codec();
}
