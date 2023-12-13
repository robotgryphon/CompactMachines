package dev.compactmods.machines.test;

import dev.compactmods.machines.neoforge.CompactMachines;
import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.util.DimensionUtil;
import net.minecraft.server.MinecraftServer;
import net.neoforged.event.server.ServerStartedEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID)
public class ServerEvents {

    @SubscribeEvent
    public static void onServerStarted(final ServerStartedEvent evt) {
        final MinecraftServer serv = evt.getServer();
        var compactLevel = serv.getLevel(CompactDimension.LEVEL_KEY);
        if (compactLevel == null) {
            CompactMachines.LOGGER.warn("Compact dimension not found; recreating it.");
            DimensionUtil.createAndRegisterWorldAndDimension(serv);
        }
    }
}
