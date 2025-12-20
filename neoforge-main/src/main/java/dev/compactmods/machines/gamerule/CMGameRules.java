package dev.compactmods.machines.gamerule;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.serialization.Codec;
import dev.compactmods.machines.CMRegistries;
import dev.compactmods.machines.api.CompactMachines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import net.minecraft.world.level.gamerules.GameRuleType;
import net.minecraft.world.level.gamerules.GameRuleTypeVisitor;
import net.minecraft.world.level.gamerules.GameRules;
import net.neoforged.neoforge.registries.DeferredHolder;

public class CMGameRules {

    public static final Identifier ALLOW_SURVIVAL_OUT_OF_BOUNDS_KEY = CompactMachines.identifier("allow_survival_oob");
    public static DeferredHolder<GameRule<?>, GameRule<Boolean>> ALLOW_SURVIVAL_OUT_OF_BOUNDS = CMRegistries.GAME_RULES
            .register(ALLOW_SURVIVAL_OUT_OF_BOUNDS_KEY.getPath(), () -> makeBooleanRule(false));

    public static final Identifier ALLOW_CREATIVE_OUT_OF_BOUNDS_KEY = CompactMachines.identifier("allow_creative_oob");
    public static DeferredHolder<GameRule<?>, GameRule<Boolean>> ALLOW_CREATIVE_OUT_OF_BOUNDS = CMRegistries.GAME_RULES
            .register(ALLOW_CREATIVE_OUT_OF_BOUNDS_KEY.getPath(), () -> makeBooleanRule(true));

    public static final Identifier ALLOW_SPECTATORS_OUT_OF_BOUNDS_KEY = CompactMachines.identifier("allow_spectator_oob");
    public static DeferredHolder<GameRule<?>, GameRule<Boolean>> ALLOW_SPECTATORS_OUT_OF_BOUNDS = CMRegistries.GAME_RULES
            .register(ALLOW_SPECTATORS_OUT_OF_BOUNDS_KEY.getPath(), () -> makeBooleanRule(false));

    public static final Identifier DAMAGE_OOB_PLAYERS_KEY = CompactMachines.identifier("damage_oob");
    public static DeferredHolder<GameRule<?>, GameRule<Boolean>> DAMAGE_OOB_PLAYERS = CMRegistries.GAME_RULES
            .register(DAMAGE_OOB_PLAYERS_KEY.getPath(), () -> makeBooleanRule(false));

    /**
     * For hardcore-style packs. If a shrinking item is successfully used to LEAVE a room,
     * it will also be damaged. Off by default.
     */
    public static final Identifier DAMAGE_PSD_ITEMS_ON_ROOM_EXIT_KEY = CompactMachines.identifier("damage_psd_on_exit");
    public static DeferredHolder<GameRule<?>, GameRule<Boolean>> DAMAGE_PSD_ITEMS_ON_ROOM_EXIT = CMRegistries.GAME_RULES
            .register(DAMAGE_PSD_ITEMS_ON_ROOM_EXIT_KEY.getPath(), () -> makeBooleanRule(false));;

    private static GameRule<Boolean> makeBooleanRule(boolean defaultVal) {
        return new GameRule<>(GameRuleCategory.PLAYER, GameRuleType.BOOL, BoolArgumentType.bool(),
                GameRuleTypeVisitor::visitBoolean, Codec.BOOL, (b) -> b ? 1 : 0, defaultVal, FeatureFlags.DEFAULT_FLAGS);
    }

    public static void prepare() {}
}
