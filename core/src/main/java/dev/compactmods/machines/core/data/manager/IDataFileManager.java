package dev.compactmods.machines.core.data.manager;

import dev.compactmods.machines.core.data.CMDataFile;

public interface IDataFileManager<T extends CMDataFile> {
   T data();

   void save();
}
