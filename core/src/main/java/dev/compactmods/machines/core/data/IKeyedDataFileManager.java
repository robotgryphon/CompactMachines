package dev.compactmods.machines.core.data;

import java.util.Optional;

public interface IKeyedDataFileManager<Key, T extends CMDataFile & CodecHolder<T>> {
   T data(Key key);

   Optional<T> optionalData(Key key);

   void save();

}

