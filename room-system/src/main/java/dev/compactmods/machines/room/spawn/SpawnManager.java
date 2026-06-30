package dev.compactmods.machines.room.spawn;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.UnboundedMapCodec;
import dev.compactmods.machines.api.room.RoomInstance;
import dev.compactmods.machines.api.room.spatial.RoomBoundaries;
import dev.compactmods.machines.api.room.spawn.IRoomSpawnManager;
import dev.compactmods.machines.api.room.spawn.IRoomSpawns;
import dev.compactmods.machines.api.room.spawn.RoomSpawn;
import dev.compactmods.machines.core.data.DataFileUtil;
import dev.compactmods.machines.core.data.Saveable;
import dev.compactmods.machines.core.data.manager.CodecFileManager;
import dev.compactmods.machines.room.data.CMRoomDataLocations;
import it.unimi.dsi.fastutil.objects.Object2ReferenceLinkedOpenHashMap;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class SpawnManager implements IRoomSpawnManager, Saveable {

    private static final UnboundedMapCodec<UUID, RoomSpawn> PLAYER_SPAWNS_CODEC = Codec.unboundedMap(UUIDUtil.STRING_CODEC, RoomSpawn.CODEC);
    public static final Codec<SpawnManager> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.STRING.fieldOf("roomCode").forGetter(x -> x.roomCode),
            PLAYER_SPAWNS_CODEC.fieldOf("player_spawns").forGetter(x -> x.playerSpawns),
            RoomSpawn.CODEC.fieldOf("default_spawn").forGetter(x -> x.defaultSpawn),
            RoomBoundaries.MAP_CODEC.fieldOf("room_bounds").forGetter(x -> x.roomBoundaries)
    ).apply(inst, SpawnManager::new));

    private MinecraftServer server;

    private final String roomCode;
    private final RoomBoundaries roomBoundaries;
    private RoomSpawn defaultSpawn;
    private final Map<UUID, RoomSpawn> playerSpawns;

    SpawnManager(MinecraftServer server, RoomInstance roomInstance) {
        this.server = server;
        this.roomCode = roomInstance.code();
        this.roomBoundaries = roomInstance.boundaries();
        this.defaultSpawn = new RoomSpawn(roomBoundaries.defaultSpawn(), Vec2.ZERO);
        this.playerSpawns = new Object2ReferenceLinkedOpenHashMap<>();
    }

    private SpawnManager(String roomCode, Map<UUID, RoomSpawn> playerSpawns, RoomSpawn defaultSpawn, RoomBoundaries roomBoundaries) {
        this.roomCode = roomCode;
        this.playerSpawns = new Object2ReferenceLinkedOpenHashMap<>(playerSpawns);
        this.defaultSpawn = defaultSpawn;
        this.roomBoundaries = roomBoundaries;
    }

    @Override
    public void resetPlayerSpawn(UUID player) {
        playerSpawns.remove(player);
    }

    @Override
    public void setDefaultSpawn(Vec3 position, Vec2 rotation) {
        defaultSpawn = new RoomSpawn(position, rotation);
    }

    @Override
    public IRoomSpawns spawns() {
        final var ps = new HashMap<UUID, RoomSpawn>();
        playerSpawns.forEach(ps::putIfAbsent);
        return new RoomSpawns(defaultSpawn, ps);
    }

    @Override
    public void setPlayerSpawn(UUID player, Vec3 location, Vec2 rotation) {
        if (!roomBoundaries.innerBounds().contains(location))
            return;

        playerSpawns.put(player, new RoomSpawn(location, rotation));
    }

    @Override
    public void save() {
        CodecFileManager.Single<SpawnManager> manager = CodecFileManager.single(CODEC)
                .at(CMRoomDataLocations.PLAYER_SPAWNS)
                .build(server);

        manager.save();
    }

    private record RoomSpawns(RoomSpawn defaultSpawn,
                              Map<UUID, RoomSpawn> playerSpawnsSnapshot) implements IRoomSpawns {

        @Override
        public Optional<RoomSpawn> forPlayer(UUID player) {
            return Optional.ofNullable(playerSpawnsSnapshot.get(player));
        }
    }
}
