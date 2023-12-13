package dev.compactmods.machines.test;

import dev.compactmods.machines.api.core.Constants;
import net.neoforged.event.RegisterGameTestsEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class GameTestEventHandler {

    @SubscribeEvent
    public static void registerCrossmodGametests(final RegisterGameTestsEvent gametests) {
        final var mods = ModList.get();

        // if(mods.isLoaded("mekanism")) gametests.register(Mekanism.class);
    }
}
