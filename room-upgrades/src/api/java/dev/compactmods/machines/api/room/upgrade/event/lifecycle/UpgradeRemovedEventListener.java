package dev.compactmods.machines.api.room.upgrade.event.lifecycle;

import dev.compactmods.machines.api.room.RoomInstance;
import dev.compactmods.machines.api.room.upgrade.event.RoomUpgradeComponentEvent;

@FunctionalInterface
public interface UpgradeRemovedEventListener extends RoomUpgradeComponentEvent {

    /**
     * Called when an update is removed from a room.
     */
    @Override
    void handle(RoomInstance instance);
}
