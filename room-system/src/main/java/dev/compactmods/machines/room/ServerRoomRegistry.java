package dev.compactmods.machines.room;

import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.api.room.RoomInstance;
import dev.compactmods.machines.api.room.generation.NewRoomBuilder;
import dev.compactmods.machines.api.room.registration.RoomRegistry;
import dev.compactmods.machines.api.room.template.RoomTemplate;
import dev.compactmods.machines.core.data.CMSingletonDataFileManager;
import dev.compactmods.machines.room.graph.node.RoomRegistrationNode;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class ServerRoomRegistry implements RoomRegistry, AutoCloseable {

    private final MinecraftServer server;
    private final CMSingletonDataFileManager<RoomRegistrarData> ROOM_REGISTRAR_DATA;
    private final Map<String, RoomInstance> instanceCache;

    public ServerRoomRegistry(MinecraftServer server) {
        this.instanceCache = new Object2ObjectArrayMap<>();
        this.server = server;
        ROOM_REGISTRAR_DATA = new CMSingletonDataFileManager<>(server, "room_registrations", new RoomRegistrarData());
        ROOM_REGISTRAR_DATA.load();
    }

    @Override
    public NewRoomBuilder builder() {
        return new ServerNewRoomBuilder();
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

//    @Override
//    public RoomInstance createNew(RoomTemplate template, UUID owner, Consumer<NewRoomBuilder> override) {
//        final Consumer<NewRoomBuilder> preOverride = builder -> builder.defaultMachineColor(template.defaultMachineColor())
//                .owner(owner)
//                .boundaries(getNextBoundaries(template));
//
//        // Make builder, set template defaults, then allow overrides
//        final var b = new ServerNewRoomBuilder();
//
//        preOverride.andThen(override).accept(b);
//
//        final var inst = b.build(server);
//
//        var node = new RoomRegistrationNode(UUID.randomUUID(), new RoomRegistrationNode.Data(inst));
//
//        ROOM_REGISTRAR_DATA.data().put(node);
//
//        CompactMachines.chunkManager().calculateChunks(inst.code(), node);
//
//        instanceCache.put(inst.code(), inst);
//        return inst;
//    }

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
