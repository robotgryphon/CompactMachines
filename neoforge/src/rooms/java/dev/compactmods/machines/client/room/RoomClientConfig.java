package dev.compactmods.machines.client.room;

import net.neoforged.neoforge.common.ModConfigSpec;

/// Room-render client setting (the room-preview toggle). Owned by the mod's
/// client config spec but consumed here by room/preview code; populated by
/// {@code dev.compactmods.machines.client.config.ClientConfig} once the spec is
/// built, so the room feature doesn't depend upward on that class. Mirrors
/// {@code dev.compactmods.machines.machine.client.MachineClientConfig}.
public final class RoomClientConfig {
    private RoomClientConfig() {}

    public static ModConfigSpec.BooleanValue ENABLE_ROOM_PREVIEWS;
}
