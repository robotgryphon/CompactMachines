package dev.compactmods.machines.api.room.capability;

import dev.compactmods.machines.api.CompactMachines;
import dev.compactmods.machines.api.room.upgrade.IRoomUpgradeAccessor;
import net.neoforged.neoforge.attachment.IAttachmentHolder;

import java.util.UUID;

public interface RoomCapabilities {

    CompactRoomCapability<IAttachmentHolder, Void> ROOM_DATA_ATTACHMENTS = CompactRoomCapability.createVoid(CompactMachines.identifier("data_attachments"), IAttachmentHolder.class);

    CompactRoomCapability<IAttachmentHolder, UUID> UPGRADE_DATA_ATTACHMENTS = CompactRoomCapability.create(CompactMachines.identifier("upgrade_data_attachments"), IAttachmentHolder.class, UUID.class);

    CompactRoomCapability<IRoomUpgradeAccessor, Void> UPGRADES = CompactRoomCapability.createVoid(CompactMachines.identifier("components"), IRoomUpgradeAccessor.class);
}
