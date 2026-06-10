package dev.compactmods.machines.upgrades.api.event.lifecycle;

import dev.compactmods.machines.api.room.RoomInstance;
import dev.compactmods.machines.upgrades.api.event.RoomUpgradeComponentEvent;

@FunctionalInterface
public interface UpgradeTickedEventListener extends RoomUpgradeComponentEvent {

    @Override
    void handle(RoomInstance instance);
}
