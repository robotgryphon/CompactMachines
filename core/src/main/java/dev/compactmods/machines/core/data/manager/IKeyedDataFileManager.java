package dev.compactmods.machines.core.data.manager;

import dev.compactmods.machines.core.data.CMDataFile;

import java.util.Optional;

public interface IKeyedDataFileManager<Key, T extends CMDataFile<T>> {
   T data(Key key);

   Optional<T> optionalData(Key key);

   void save();

}

