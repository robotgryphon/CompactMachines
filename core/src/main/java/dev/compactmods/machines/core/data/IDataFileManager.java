package dev.compactmods.machines.core.data;

public interface IDataFileManager<T extends CMDataFile & CodecHolder<T>> {
   T data();

   void save();
}
