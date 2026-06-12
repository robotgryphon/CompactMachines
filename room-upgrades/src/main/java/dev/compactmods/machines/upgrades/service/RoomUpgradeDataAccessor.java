package dev.compactmods.machines.upgrades.service;

import dev.compactmods.machines.upgrades.api.data.IRoomUpgradeDataAttachmentAccessor;
import dev.compactmods.machines.core.data.manager.CMKeyedDataFileManager;
import dev.compactmods.machines.upgrades.RoomUpgradeDataAttachments;
import dev.compactmods.machines.upgrades.RoomUpgradeIdentifier;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.attachment.IAttachmentHolder;

import java.util.Optional;
import java.util.UUID;

public class RoomUpgradeDataAccessor implements IRoomUpgradeDataAttachmentAccessor, AutoCloseable {
    private final CMKeyedDataFileManager<RoomUpgradeIdentifier, RoomUpgradeDataAttachments> DATA_ATTACHMENTS;

    public RoomUpgradeDataAccessor(MinecraftServer server) {
        DATA_ATTACHMENTS = new CMKeyedDataFileManager<>(server, RoomUpgradeDataAttachments::new) {
            @Override
            public String getFileKey(RoomUpgradeIdentifier key) {
                return key.instanceId().toString();
            }
        };
    }

    @Override
    public Optional<? extends IAttachmentHolder> get(String roomCode, UUID instanceId) {
        return DATA_ATTACHMENTS.optionalData(new RoomUpgradeIdentifier(roomCode, instanceId));
    }

    @Override
    public IAttachmentHolder getOrCreate(String roomCode, UUID instanceId) {
        return DATA_ATTACHMENTS.data(new RoomUpgradeIdentifier(roomCode, instanceId));
    }

    @Override
    public void save() {
        DATA_ATTACHMENTS.save();
    }

    @Override
    public void close() {
        save();
    }
}
