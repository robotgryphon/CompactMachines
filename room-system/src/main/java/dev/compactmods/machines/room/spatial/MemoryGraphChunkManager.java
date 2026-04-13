package dev.compactmods.machines.room.spatial;

import dev.compactmods.machines.api.room.RoomInstance;
import dev.compactmods.machines.api.room.capability.RoomCapabilities;
import dev.compactmods.machines.api.room.registration.RoomRegistry;
import dev.compactmods.machines.api.room.spatial.RoomBoundaries;
import dev.compactmods.machines.api.room.spatial.RoomChunkManager;
import dev.compactmods.machines.api.room.spatial.IRoomChunks;
import dev.compactmods.feather.MemoryGraph;
import dev.compactmods.feather.edge.GraphEdge;
import dev.compactmods.machines.room.ServerRoomRegistry;
import dev.compactmods.machines.room.graph.GraphNodes;
import dev.compactmods.machines.room.graph.edge.RoomChunkEdge;
import dev.compactmods.machines.room.graph.node.RoomChunkNode;
import dev.compactmods.machines.room.graph.node.RoomReferenceNode;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.ChunkPos;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class MemoryGraphChunkManager implements RoomChunkManager {

    private final MemoryGraph graph;
    private final Map<ChunkPos, RoomChunkNode> chunks;
    private final RoomRegistry registry;

    public MemoryGraphChunkManager(MinecraftServer server) {
        this.graph = new MemoryGraph();
        this.chunks = new HashMap<>();

        RoomRegistry r = RoomCapabilities.REGISTRY.getCapability(server);
        if(r == null) r = new ServerRoomRegistry(server);
        this.registry = r;

        registry.allRooms()
                .forEach(inst -> calculateChunks(inst.code(), inst.boundaries()));
    }

    @Override
    public void calculateChunks(String roomCode, RoomBoundaries boundaries) {
        final var outer = boundaries.outerBounds();
        final var allInside = dev.compactmods.machines.core.util.MathUtil.getChunksFromAABB(outer).collect(Collectors.toSet());

        final var ref = new RoomReferenceNode(roomCode);
        graph.addNode(ref);

        for (var c : allInside) {
            final var chunk = new RoomChunkNode(UUID.randomUUID(), new RoomChunkNode.Data(c));
            graph.addNode(chunk);
            graph.connectNodes(ref, chunk, new RoomChunkEdge(ref, chunk));
            chunks.put(c, chunk);
        }
    }

    @Override
    public Optional<RoomInstance> findRoomByChunk(ChunkPos chunk) {
        if (!chunks.containsKey(chunk)) return Optional.empty();
        final var chunkNode = chunks.get(chunk);

        return graph.inboundEdges(chunkNode, RoomReferenceNode.class)
                .map(GraphEdge::source)
                .map(WeakReference::get)
                .filter(Objects::nonNull)
                .map(RoomReferenceNode::code)
                .findFirst()
                .flatMap(registry::get);
    }

    @Override
    public IRoomChunks get(String room) {
        final var regNode = graph.nodes(RoomReferenceNode.class)
                .filter(rn -> rn.code().equals(room))
                .findFirst();

        return regNode.map(node -> {
            final var chunks = graph.outboundEdges(GraphNodes.ROOM_CHUNKS, node)
                    .map(GraphEdge::target)
                    .map(WeakReference::get)
                    .filter(Objects::nonNull)
                    .peek(chunkNode -> this.chunks.putIfAbsent(chunkNode.data().chunk(), chunkNode))
                    .map(c -> c.data().chunk())
                    .collect(Collectors.toSet());

            return new RoomChunks(chunks);
        }).orElseThrow();
    }
}
