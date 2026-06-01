package dev.compactmods.machines.room.registry;

import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.api.room.RoomInstance;
import dev.compactmods.machines.api.room.registry.RoomRegistry;
import dev.compactmods.machines.core.data.CMSingletonDataFileManager;
import dev.compactmods.machines.room.Rooms;
import dev.compactmods.machines.room.graph.node.RoomRegistrationNode;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class ServerRoomRegistry implements RoomRegistry, AutoCloseable {

    private final MinecraftServer server;
    private final CMSingletonDataFileManager<RoomRegistrarData> ROOM_REGISTRAR_DATA;
    private final Map<String, RoomInstance> instanceCache;

    public ServerRoomRegistry(MinecraftServer server) {
        this.server = server;
        this.instanceCache = new Object2ObjectArrayMap<>();

        ROOM_REGISTRAR_DATA = server.getData(Rooms.DataAttachments.ROOM_REGISTRAR_DATA);
        ROOM_REGISTRAR_DATA.load();
    }

    @Override
    public boolean isRegistered(String room) {
        return ROOM_REGISTRAR_DATA.data().isRegistered(room);
    }

    @Override
    public Optional<RoomInstance> get(String room) {
        return ROOM_REGISTRAR_DATA.data()
                .get(room)
                .map(this::getOrMakeRoomInstance);
    }

    @Override
    public int count() {
        return Math.toIntExact(ROOM_REGISTRAR_DATA.data().count());
    }

    @Override
    public Stream<String> allRoomCodes() {
        if(ROOM_REGISTRAR_DATA == null) return Stream.empty();
        return ROOM_REGISTRAR_DATA.data().allRoomCodes();
    }

    @Override
    public Stream<RoomInstance> allRooms() {
        if(ROOM_REGISTRAR_DATA == null) return Stream.empty();
        return ROOM_REGISTRAR_DATA.data()
                .allRoomData()
                .map(this::getOrMakeRoomInstance);
    }

    @Override
    public void save() {
        if(ROOM_REGISTRAR_DATA == null) return;
        ROOM_REGISTRAR_DATA.save();
    }

    @NotNull
    private RoomInstance getOrMakeRoomInstance(RoomRegistrationNode regNode) {
        if (instanceCache.containsKey(regNode.code()))
            return instanceCache.get(regNode.code());

        final var inst = new ServerRoomInstance(server, CompactDimension.LEVEL_KEY, regNode.code(), regNode.data().boundaries());
        instanceCache.put(regNode.code(), inst);
        return inst;
    }

    @Override
    public void close() {
        save();
    }
}
