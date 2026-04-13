package dev.compactmods.machines.shrinking.api.capability;

import dev.compactmods.machines.shrinking.api.ShrinkingDeviceConfiguration;
import dev.compactmods.machines.shrinking.api.history.RoomEntryPoint;
import dev.compactmods.machines.shrinking.api.history.RoomEntryResult;
import dev.compactmods.machines.shrinking.api.history.RoomExitResult;
import net.minecraft.core.GlobalPos;

import java.util.concurrent.CompletableFuture;

public interface PlayerShrinkingHandler {

    CompletableFuture<RoomEntryResult> tryEnter(RoomEntryPoint entryPoint);

    CompletableFuture<RoomEntryResult> tryEnter(GlobalPos machinePosition, ShrinkingDeviceConfiguration config);

    CompletableFuture<RoomExitResult> tryExit();

}
