package dev.compactmods.machines.test.worldgen;

import dev.compactmods.compactmachines.api.room.CompactRoomGenerator;
import dev.compactmods.compactmachines.api.room.RoomTemplate;
import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.test.TestBatches;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@PrefixGameTestTemplate(false)
@GameTestHolder(Constants.MOD_ID)
public class RoomGenerationTests {

    @GameTest(template = "empty_15x15", batch = TestBatches.ROOM_GENERATION)
    public static void checkRoomGeneratorColossal(final GameTestHelper test) {
        final var roomDims = new Vec3i(13, 13, 13);
        final var roomCenter = Vec3.atCenterOf(test.absolutePos(new BlockPos(7, 2, 7)));

        CompactRoomGenerator.generateRoom(test.getLevel(), roomDims, roomCenter);

        test.setBlock(new BlockPos(7, 9, 7), Blocks.GOLD_BLOCK.defaultBlockState());
        CompactRoomGenerator.fillWithTemplate(test.getLevel(),
                new ResourceLocation(Constants.MOD_ID, "template_max"),
                roomDims, roomCenter);

        test.succeed();
    }

    @GameTest(template = "empty_15x15", batch = TestBatches.ROOM_GENERATION)
    public static void checkRoomGeneratorNormal(final GameTestHelper test) {
        final var roomDims = new Vec3i(9, 9, 9);
        final var roomCenter = Vec3.atCenterOf(test.absolutePos(new BlockPos(7, 2, 7)));

        CompactRoomGenerator.generateRoom(test.getLevel(), roomDims, roomCenter);

        test.setBlock(new BlockPos(7, 5, 7), Blocks.GOLD_BLOCK.defaultBlockState());
        CompactRoomGenerator.fillWithTemplate(test.getLevel(),
                RoomTemplate.NO_TEMPLATE,
                roomDims, roomCenter);

        test.succeed();
    }

    @GameTest(template = "empty_15x15", batch = TestBatches.ROOM_GENERATION)
    public static void checkRoomGeneratorSmall(final GameTestHelper test) {
        final var roomDims = new Vec3i(5, 5, 5);
        final var roomCenter = Vec3.atCenterOf(test.absolutePos(new BlockPos(7, 2, 7)));

        CompactRoomGenerator.generateRoom(test.getLevel(), roomDims, roomCenter);

        test.setBlock(new BlockPos(7, 4, 7), Blocks.GOLD_BLOCK.defaultBlockState());
        CompactRoomGenerator.fillWithTemplate(test.getLevel(),
                RoomTemplate.NO_TEMPLATE,
                roomDims, roomCenter);

        test.succeed();
    }

    @GameTest(template = "empty_15x15", batch = TestBatches.ROOM_GENERATION)
    public static void checkRoomGeneratorWeirdShape(final GameTestHelper test) {
        final var roomDims = new Vec3i(11, 2, 7);
        final var roomCenter = Vec3.atCenterOf(test.absolutePos(new BlockPos(7, 2, 7)));

        CompactRoomGenerator.generateRoom(test.getLevel(), roomDims, roomCenter);

        // test.setBlock(new BlockPos(7, 3, 7), Blocks.GOLD_BLOCK.defaultBlockState());

        test.succeed();
    }
}
