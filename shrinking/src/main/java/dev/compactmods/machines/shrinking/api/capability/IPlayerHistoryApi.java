package dev.compactmods.machines.shrinking.api.capability;

import dev.compactmods.machines.core.data.Saveable;

public interface IPlayerHistoryApi extends Saveable {

   PlayerEntryPointHistoryManager entryPoints();
   
}
