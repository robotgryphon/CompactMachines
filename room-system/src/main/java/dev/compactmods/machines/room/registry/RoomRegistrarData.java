package dev.compactmods.machines.room.registry;

import com.mojang.serialization.Codec;
import dev.compactmods.machines.core.data.CMDataFile;
import dev.compactmods.machines.core.data.CodecHolder;
import dev.compactmods.machines.room.data.CMRoomDataLocations;
import dev.compactmods.machines.room.graph.node.RoomRegistrationNode;
import it.unimi.dsi.fastutil.objects.Object2ReferenceArrayMap;
import net.minecraft.server.MinecraftServer;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class RoomRegistrarData implements CodecHolder<RoomRegistrarData>, CMDataFile {

    public static final Codec<RoomRegistrarData> CODEC = RoomRegistrationNode.CODEC.listOf()
            .fieldOf("rooms")
            .xmap(RoomRegistrarData::new, (RoomRegistrarData x) -> List.copyOf(x.registrationNodes.values()))
            .codec();

    private final Map<String, RoomRegistrationNode> registrationNodes;

    public RoomRegistrarData() {
        this.registrationNodes = new Object2ReferenceArrayMap<>();
    }

    private RoomRegistrarData(List<RoomRegistrationNode> regNodes) {
        this();
        regNodes.forEach(this::registerDirty);
    }

    public boolean isRegistered(String room) {
        return registrationNodes.containsKey(room);
    }

    public Optional<RoomRegistrationNode> get(String room) {
        final var regNode = registrationNodes.get(room);
        return Optional.ofNullable(regNode);
    }

    public void put(RoomRegistrationNode node) {
        this.registrationNodes.put(node.code(), node);
    }

    public long count() {
        return registrationNodes.size();
    }


    public Stream<String> allRoomCodes() {
        return registrationNodes.keySet().stream();
    }

    private void registerDirty(RoomRegistrationNode node) {
        registrationNodes.putIfAbsent(node.code(), node);
    }

    public Path getDataLocation(MinecraftServer server) {
        return CMRoomDataLocations.DATA_ROOT.apply(server);
    }

    @Override
    public Codec<RoomRegistrarData> codec() {
        return CODEC;
    }

    public Stream<RoomRegistrationNode> allRoomData() {
        return registrationNodes.values().stream();
    }
}
