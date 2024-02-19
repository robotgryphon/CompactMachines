package dev.compactmods.machines.test.data;

import dev.compactmods.machines.api.Constants;
import dev.compactmods.machines.api.room.history.RoomEntryPoint;
import dev.compactmods.machines.player.PlayerEntryPointHistory;
import dev.compactmods.machines.player.RoomEntryResult;
import dev.compactmods.machines.room.RoomCodeGenerator;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@PrefixGameTestTemplate(false)
@GameTestHolder(Constants.MOD_ID)
public class PlayerHistoryTrackerTests {

    private static final String BATCH = "PLAYER_HISTORY_TRACKING";

    @GameTest(template = "empty_1x1", batch = BATCH)
    public static void failsPlayerGoingTooFar(final GameTestHelper test) {
        final var history = new PlayerEntryPointHistory(1, c -> true);

        final var player = test.makeMockSurvivalPlayer();
        history.enterRoom(player, RoomCodeGenerator.generateRoomId(), RoomEntryPoint.nonexistent());

        final var tooFar = history.enterRoom(player, RoomCodeGenerator.generateRoomId(), RoomEntryPoint.nonexistent());

        test.assertTrue(tooFar == RoomEntryResult.FAILED_TOO_FAR_DOWN, "Room entry should have failed.");

        test.succeed();
    }

    @GameTest(template = "empty_1x1", batch = BATCH)
    public static void canGetPlayerHistory(final GameTestHelper test) {
        final var history = new PlayerEntryPointHistory(5, c -> true);

        final var player = test.makeMockSurvivalPlayer();

        for(int i = 0; i < 5; i++)
            history.enterRoom(player, RoomCodeGenerator.generateRoomId(), RoomEntryPoint.nonexistent());

        var hist = history.history(player, 3);
        test.assertTrue(hist.size() == 3, "Expected 3 entries in history.");
        hist.pop();

        test.succeed();
    }
}
