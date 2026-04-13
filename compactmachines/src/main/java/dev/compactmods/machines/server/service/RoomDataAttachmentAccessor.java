package dev.compactmods.machines.server.service;

import dev.compactmods.machines.api.room.data.IRoomDataAttachmentAccessor;
import dev.compactmods.machines.core.data.CMKeyedDataFileManager;
import dev.compactmods.machines.room.attachment.RoomDataAttachments;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.attachment.IAttachmentHolder;

import java.util.Optional;

public class RoomDataAttachmentAccessor implements IRoomDataAttachmentAccessor {

    private final CMKeyedDataFileManager<String, RoomDataAttachments> ROOM_DATA_ATTACHMENTS;

    public RoomDataAttachmentAccessor(MinecraftServer server) {
        ROOM_DATA_ATTACHMENTS = new CMKeyedDataFileManager<>(server, RoomDataAttachments::new);
    }

    @Override
    public Optional<? extends IAttachmentHolder> get(String roomCode) {
        return ROOM_DATA_ATTACHMENTS.optionalData(roomCode);
    }

    @Override
    public IAttachmentHolder getOrCreate(String roomCode) {
        return ROOM_DATA_ATTACHMENTS.data(roomCode);
    }

    @Override
    public void save() {
        ROOM_DATA_ATTACHMENTS.save();
    }
}
