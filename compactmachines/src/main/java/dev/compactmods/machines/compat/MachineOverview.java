package dev.compactmods.machines.compat;

import dev.compactmods.machines.api.room.RoomInstance;

import java.util.UUID;

public record MachineOverview(UUID owner, RoomInstance connectedRoom) {
}
