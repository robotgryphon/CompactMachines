package dev.compactmods.machines.neoforge.client;

import dev.compactmods.machines.api.core.Constants;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.ModConfigSpec;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientConfig {

    public static ModConfigSpec CONFIG;

    static {
        generateConfig();
    }

    private static void generateConfig() {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        CONFIG = builder.build();
    }
}
