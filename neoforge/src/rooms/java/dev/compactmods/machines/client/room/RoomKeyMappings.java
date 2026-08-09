package dev.compactmods.machines.client.room;

import dev.compactmods.machines.core.CompactMachinesCore;
import net.minecraft.client.KeyMapping;

public interface RoomKeyMappings {
    KeyMapping.Category CATEGORY = new KeyMapping.Category(CompactMachinesCore.identifier("general"));
}
