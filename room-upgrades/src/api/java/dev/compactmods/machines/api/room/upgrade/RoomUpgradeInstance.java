package dev.compactmods.machines.api.room.upgrade;

import dev.compactmods.machines.api.room.RoomInstance;

import java.util.UUID;
import java.util.stream.Stream;

public record RoomUpgradeInstance(RoomInstance roomInstance, UUID upgradeID) {

    public Stream<RoomUpgradeComponent> components() {
        // TODO
        return Stream.empty();
    }
}