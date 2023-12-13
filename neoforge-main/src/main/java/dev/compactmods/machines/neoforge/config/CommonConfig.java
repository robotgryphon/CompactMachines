package dev.compactmods.machines.neoforge.config;

import dev.compactmods.machines.neoforge.CompactMachines;
import dev.compactmods.machines.api.core.Constants;
import net.neoforged.common.ForgeConfigSpec;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CommonConfig {

    public static ForgeConfigSpec CONFIG;

    public static ForgeConfigSpec.BooleanValue ENABLE_VANILLA_RECIPES;



    static {
        generateConfig();
    }

    private static void generateConfig() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder
                .comment("Recipes and Integrations")
                .push("recipes");

        ENABLE_VANILLA_RECIPES = builder
                .comment("Enable vanilla-style recipes.")
                .define("vanillaRecipes", true);

        builder.pop();

        CONFIG = builder.build();
    }

    @SubscribeEvent
    public static void onLoaded(ModConfigEvent.Loading loading) {
        CompactMachines.LOGGER.debug("Loading common configuration...");
    }
}
