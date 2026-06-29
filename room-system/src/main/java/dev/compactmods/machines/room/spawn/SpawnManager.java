package dev.compactmods.machines.room.spawn;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.UnboundedMapCodec;
import dev.compactmods.machines.api.room.RoomInstance;
import dev.compactmods.machines.api.room.spatial.RoomBoundaries;
import dev.compactmods.machines.api.room.spawn.IRoomSpawnManager;
import dev.compactmods.machines.api.room.spawn.IRoomSpawns;
import dev.compactmods.machines.api.room.spawn.RoomSpawn;
import dev.compactmods.machines.core.data.CMDataFile;
import dev.compactmods.machines.room.data.CMRoomDataLocations;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class SpawnManager implements IRoomSpawnManager, CMDataFile<SpawnManager> {

    private final Logger LOGS = LogManager.getLogger();

    private static final UnboundedMapCodec<UUID, dev.compactmods.machines.api.room.spawn.RoomSpawn> PLAYER_SPAWNS_CODEC = Codec.unboundedMap(UUIDUtil.STRING_CODEC, dev.compactmods.machines.api.room.spawn.RoomSpawn.CODEC);
    public static final Codec<SpawnManager> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.STRING.fieldOf("roomCode").forGetter(x -> x.roomCode),
            PLAYER_SPAWNS_CODEC.fieldOf("player_spawns").forGetter(x -> x.playerSpawns),
            dev.compactmods.machines.api.room.spawn.RoomSpawn.CODEC.fieldOf("default_spawn").forGetter(x -> x.defaultSpawn),
            RoomBoundaries.MAP_CODEC.fieldOf("room_bounds").forGetter(x -> x.roomBoundaries)
    ).apply(inst, SpawnManager::new));

    private final String roomCode;

    private final RoomBoundaries roomBoundaries;

    private dev.compactmods.machines.api.room.spawn.RoomSpawn defaultSpawn;

    private final Map<UUID, dev.compactmods.machines.api.room.spawn.RoomSpawn> playerSpawns;

    public SpawnManager(RoomInstance instance) {
        this.roomCode = instance.code();
        this.playerSpawns = new HashMap<>();
        this.roomBoundaries = instance.boundaries();
        this.defaultSpawn = new RoomSpawn(instance.boundaries().defaultSpawn(), Vec2.ZERO);
    }

    private SpawnManager(String roomCode, Map<UUID, dev.compactmods.machines.api.room.spawn.RoomSpawn> playerSpawns, dev.compactmods.machines.api.room.spawn.RoomSpawn defaultSpawn, RoomBoundaries roomBoundaries) {
        this.roomCode = roomCode;
        this.playerSpawns = new HashMap<>(playerSpawns);
        this.defaultSpawn = defaultSpawn;
        this.roomBoundaries = roomBoundaries;
    }

    @Override
    public void resetPlayerSpawn(UUID player) {
        playerSpawns.remove(player);
    }

    @Override
    public void setDefaultSpawn(Vec3 position, Vec2 rotation) {
        defaultSpawn = new dev.compactmods.machines.api.room.spawn.RoomSpawn(position, rotation);
    }

    @Override
    public IRoomSpawns spawns() {
        final var ps = new HashMap<UUID, dev.compactmods.machines.api.room.spawn.RoomSpawn>();
        playerSpawns.forEach(ps::putIfAbsent);
        return new RoomSpawns(defaultSpawn, ps);
    }

    @Override
    public void setPlayerSpawn(UUID player, Vec3 location, Vec2 rotation) {
        if (!roomBoundaries.innerBounds().contains(location))
            return;

        playerSpawns.put(player, new dev.compactmods.machines.api.room.spawn.RoomSpawn(location, rotation));
    }

    private Path getDataLocation(MinecraftServer server) {
        return CMRoomDataLocations.PLAYER_SPAWNS.apply(server);
    }

    @Override
    public Codec<SpawnManager> codec() {
        return CODEC;
    }

    private record RoomSpawns(dev.compactmods.machines.api.room.spawn.RoomSpawn defaultSpawn,
                              Map<UUID, dev.compactmods.machines.api.room.spawn.RoomSpawn> playerSpawnsSnapshot) implements IRoomSpawns {

        @Override
        public Optional<dev.compactmods.machines.api.room.spawn.RoomSpawn> forPlayer(UUID player) {
            return Optional.ofNullable(playerSpawnsSnapshot.get(player));
        }
    }
}
