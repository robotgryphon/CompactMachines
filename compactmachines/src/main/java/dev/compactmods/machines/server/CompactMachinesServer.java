package dev.compactmods.machines.server;

import dev.compactmods.machines.api.room.capability.RoomCapabilities;
import dev.compactmods.machines.core.CompactMachinesCore;
import dev.compactmods.machines.core.capability.CapabilityHelper;
import dev.compactmods.machines.core.capability.ServerCapability;
import dev.compactmods.machines.core.data.Saveable;
import dev.compactmods.machines.api.dimension.CompactDimension;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.world.chunk.RegisterTicketControllersEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Mod(value = CompactMachinesCore.MOD_ID)
public class CompactMachinesServer {

    private static @Nullable MinecraftServer CURRENT_SERVER;
    private static @Nullable IServerCapabilities CURRENT_SERVER_CAPS;

//    public static TicketController CHUNK_TICKET_CONTROLLER = new TicketController(CompactMachinesCore.identifier("chunkloader_upgrade"), RoomUpgradeHelper::verifyChunkloaderUpgrades);

    public CompactMachinesServer(IEventBus modBus) {

        var roomReg = CapabilityHelper.registerServerCap(RoomCapabilities.REGISTRY);

        NeoForge.EVENT_BUS.addListener(EventPriority.LOW, CompactMachinesServer::serverAboutToStart);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOW, CompactMachinesServer::serverStarting);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOW, CompactMachinesServer::serverStopping);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOW, CompactMachinesServer::levelSaved);

        modBus.addListener(CompactMachinesServer::registerTicketController);
        CapabilityHelper.registerAliases(modBus);
    }

    public static @NotNull IServerCapabilities caps(MinecraftServer server) {
        return CURRENT_SERVER_CAPS != null ? CURRENT_SERVER_CAPS : new ServerCapabilities(server);
    }

    private static void registerTicketController(RegisterTicketControllersEvent event) {
//        event.register(CHUNK_TICKET_CONTROLLER);
    }

    private static void serverAboutToStart(final ServerAboutToStartEvent evt) {
        CURRENT_SERVER = evt.getServer();
        CURRENT_SERVER_CAPS = new ServerCapabilities(CURRENT_SERVER);
    }

    private static void serverStarting(final ServerStartingEvent evt) {
        CURRENT_SERVER_CAPS = new ServerCapabilities(CURRENT_SERVER);
    }

    private static void saveAll() {
        if(CURRENT_SERVER_CAPS instanceof Saveable s)
            s.save();
    }

    private static void serverStopping(final ServerStoppingEvent ignored) {
        saveAll();
    }

    private static void levelSaved(final LevelEvent.Save level) {
        if (level.getLevel() instanceof Level l && CompactDimension.isLevelCompact(l)) {
            saveAll();
        }
    }

    public static <T> T getCapability(ServerCapability<T, Void> generator) {
        return generator.getCapability(CURRENT_SERVER);
    }

    public static <T, C> T getCapability(ServerCapability<T, C> generator, @Nullable C context) {
        return generator.getCapability(CURRENT_SERVER, context);
    }
}
