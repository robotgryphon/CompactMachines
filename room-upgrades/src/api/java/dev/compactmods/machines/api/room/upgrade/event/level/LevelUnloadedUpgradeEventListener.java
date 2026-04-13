package dev.compactmods.machines.api.room.upgrade.event.level;

import dev.compactmods.machines.api.room.RoomInstance;
import dev.compactmods.machines.api.room.upgrade.event.RoomUpgradeComponentEvent;

@FunctionalInterface
public interface LevelUnloadedUpgradeEventListener extends RoomUpgradeComponentEvent {

    /**
     * Called when a level is unloaded.
     */
    @Override
    void handle(RoomInstance instance);
}
