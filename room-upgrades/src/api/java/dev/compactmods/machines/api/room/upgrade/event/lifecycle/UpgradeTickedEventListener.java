package dev.compactmods.machines.api.room.upgrade.event.lifecycle;

import dev.compactmods.machines.api.room.RoomInstance;
import dev.compactmods.machines.api.room.upgrade.event.RoomUpgradeComponentEvent;

@FunctionalInterface
public interface UpgradeTickedEventListener extends RoomUpgradeComponentEvent {

    @Override
    void handle(RoomInstance instance);
}
