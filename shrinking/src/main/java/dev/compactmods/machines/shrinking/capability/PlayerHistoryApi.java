package dev.compactmods.machines.shrinking.capability;

import dev.compactmods.machines.shrinking.api.capability.PlayerEntryPointHistoryManager;
import dev.compactmods.machines.shrinking.api.capability.IPlayerHistoryApi;
import dev.compactmods.machines.core.data.CMSingletonDataFileManager;
import dev.compactmods.machines.shrinking.history.ServerPlayerEntryPointHistoryManager;
import net.minecraft.server.MinecraftServer;import java.util.Collections;

public class PlayerHistoryApi implements IPlayerHistoryApi {

    private final CMSingletonDataFileManager<ServerPlayerEntryPointHistoryManager> PLAYER_HISTORY_DATA;

    public PlayerHistoryApi(MinecraftServer server) {
        PLAYER_HISTORY_DATA = new CMSingletonDataFileManager<>(server, "player_entrypoint_history", new ServerPlayerEntryPointHistoryManager(5, Collections.emptyMap()));
        PLAYER_HISTORY_DATA.load();
    }

    @Override
    public PlayerEntryPointHistoryManager entryPoints() {
        return PLAYER_HISTORY_DATA.data();
    }

    @Override
    public void save() {
        PLAYER_HISTORY_DATA.save();
    }
}
