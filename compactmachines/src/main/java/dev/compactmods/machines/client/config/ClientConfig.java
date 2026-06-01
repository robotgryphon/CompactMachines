package dev.compactmods.machines.client.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ClientConfig {

    public static ModConfigSpec CONFIG;

    public static ModConfigSpec.BooleanValue ENABLE_ROOM_PREVIEWS;

    /**
     * When {@code true}, Compact Machines that haven't been pinned to a
     * specific shader (via the {@code machine_shader} data attachment) will
     * automatically render with the Pride rainbow shader during June.
     */
    public static ModConfigSpec.BooleanValue ENABLE_PRIDE_IN_JUNE;

    static {
        generateConfig();
    }

    private static void generateConfig() {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        ENABLE_ROOM_PREVIEWS = builder
                .comment("Enable room preview when opening a bound machine UI")
                .define("enableRoomPreviews", true);

        ENABLE_PRIDE_IN_JUNE = builder
                .comment("Render Compact Machines with the rainbow Pride shader during June if no explicit shader is set on the block.")
                .define("enablePrideInJune", true);

        CONFIG = builder.build();
    }
}
