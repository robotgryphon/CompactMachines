package dev.compactmods.machines.room;

import dev.compactmods.machines.api.room.generation.NewRoomBuilder;
import dev.compactmods.machines.api.room.generation.RoomGenerationDetails;
import dev.compactmods.machines.api.room.generation.RoomGenerator;
import dev.compactmods.machines.api.room.RoomInstance;
import dev.compactmods.machines.api.room.generation.RoomStructureInfo.RoomStructurePlacement;
import dev.compactmods.machines.api.room.registration.RoomRegistry;
import dev.compactmods.machines.api.room.spatial.RoomBoundaries;
import dev.compactmods.machines.api.room.spawn.IRoomSpawnManagers;
import dev.compactmods.machines.api.room.template.RoomTemplate;
import dev.compactmods.machines.core.WallConstants;
import dev.compactmods.machines.core.util.BlockSpaceUtil;
import dev.compactmods.spatial.aabb.AABBAligner;
import dev.compactmods.spatial.aabb.AABBHelper;
import dev.compactmods.spatial.vector.VectorUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import org.joml.Vector3d;

public class ServerRoomGenerator implements RoomGenerator {

    private final ServerLevel level;
    private final RoomRegistry registry;
    private final IRoomSpawnManagers spawnManagers;

    public ServerRoomGenerator(ServerLevel level, RoomRegistry registry, IRoomSpawnManagers spawnManagers) {
        this.level = level;
        this.registry = registry;
        this.spawnManagers = spawnManagers;
    }

    public RoomBoundaries getNextBoundaries(RoomTemplate template) {
        final var region = dev.compactmods.machines.core.util.MathUtil.getRegionPositionByIndex(registry.count());
        final var floor = dev.compactmods.machines.core.util.MathUtil.getCenterWithY(region, 0);

        var outerBounds = AABBAligner.floor(template.getZeroBoundaries().move(floor), 0);
        return new RoomBoundaries(outerBounds);
    }

    /**
     * Generates a wall or platform in a given direction.
     * Uses the solid wall block.
     *
     * @param outerBounds
     * @param wallDirection
     * @since 3.0.0
     */
    public void generateCompactWall(AABB outerBounds, Direction wallDirection, BlockState block) {
        AABB wallBounds = BlockSpaceUtil.getWallBounds(outerBounds, wallDirection);
        BlockSpaceUtil.blocksInside(wallBounds).forEach(wallBlock -> {
            level.setBlock(wallBlock, block, Block.UPDATE_ALL);
        });
    }

    /**
     * Generates a machine "internal" structure in a world via a machine size and a central point.
     *
     * @param outerBounds Outer dimensions of the room.
     */
    public void generateRoom(AABB outerBounds) {
        final var block = BuiltInRegistries.BLOCK.getValue(WallConstants.SOLID_WALL);
        if (block != null) {
            final var solidWall = block.defaultBlockState();
            generateRoom(outerBounds, solidWall);
        }
    }

    /**
     * Generates a machine structure in a world via machine boundaries and a wall block.
     *
     * @param outerBounds Outer dimensions of the room.
     * @param block       Block to use for walls.
     */
    public void generateRoom(AABB outerBounds, BlockState block) {

        // Generate the walls
        for (final var dir : Direction.values())
            generateCompactWall(outerBounds, dir, block);

        // Clear out the inside of the room
        AABB machineInternal = outerBounds.deflate(1);
        BlockSpaceUtil.blocksInside(machineInternal)
                .forEach(p -> level.setBlock(p, Blocks.AIR.defaultBlockState(), 7));
    }

    public void populateStructure(Identifier template, AABB roomInnerBounds, RoomStructurePlacement placement) {
        level.getStructureManager().get(template).ifPresent(tem -> {

            Vector3d templateSize = VectorUtils.convert3d(tem.getSize());

            if (!AABBHelper.fitsInside(roomInnerBounds, templateSize)) {
                // skip: structure too large to place in room
                return;
            }

            var placementSettings = new StructurePlaceSettings()
                    .setRotation(Rotation.NONE)
                    .setMirror(Mirror.NONE);

            AABB placementBounds = null;
            final var aligner = AABBAligner.create(roomInnerBounds, AABBHelper.zeroOriginSized(templateSize));
            switch (placement) {
                case RoomStructurePlacement.CENTERED -> placementBounds = aligner
                        .center(roomInnerBounds.getCenter())
                        .align();

                case RoomStructurePlacement.CENTERED_CEILING -> placementBounds = aligner
                        .boundedDirection(Direction.UP)
                        .align();

                case RoomStructurePlacement.CENTERED_FLOOR -> placementBounds = aligner
                        .boundedDirection(Direction.DOWN)
                        .align();
            }

            if (placementBounds != null) {
                final var pos = AABBHelper.minCorner(placementBounds);
                BlockPos placeAt = BlockPos.containing(pos.x(), pos.y(), pos.z());
                tem.placeInWorld(level, placeAt, placeAt, placementSettings, level.getRandom(), Block.UPDATE_ALL);
            }
        });
    }

    @Override
    public NewRoomBuilder createNew() {
        return new ServerNewRoomBuilder();
    }

    @Override
    public RoomInstance generate(String newCode, RoomGenerationDetails details) {
        if (!details.template().isBound())
            return null;

        // Empty Room (Box)
        final var template = details.template().value();
        final var newRoomBoundaries = getNextBoundaries(template);
        generateRoom(newRoomBoundaries.outerBounds());

        if (!template.structures().isEmpty()) {
            for (var struct : template.structures()) {
                populateStructure(struct.template(), newRoomBoundaries.innerBounds(), struct.placement());
            }
        }

        template.optionalFloor().ifPresent(floorState -> {
            // Generate the floor
            AABB floorBounds = BlockSpaceUtil.getWallBounds(newRoomBoundaries.innerBounds(), Direction.DOWN);
            BlockSpaceUtil.blocksInside(floorBounds).forEach(floorBlockPos -> {
                level.setBlock(floorBlockPos, floorState, Block.UPDATE_ALL);
            });

            // Bump default spawn up 1 block to account for floor
            final var spawnManager = spawnManagers.get(newCode);
            var fixedSpawn = newRoomBoundaries
                    .defaultSpawn()
                    .add(0, 1, 0);

            spawnManager.setDefaultSpawn(fixedSpawn, Vec2.ZERO);
        });

        // Assign room code and return instance

        return new ServerRoomInstance(level.getServer(), level.dimension(),
                newCode, newRoomBoundaries);
    }
}
