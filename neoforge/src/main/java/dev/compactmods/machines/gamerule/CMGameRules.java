package dev.compactmods.machines.gamerule;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.serialization.Codec;
import dev.compactmods.machines.CMRegistries;
import dev.compactmods.machines.core.CompactMachinesCore;
import dev.compactmods.machines.core.GameRulesHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import net.minecraft.world.level.gamerules.GameRuleType;
import net.minecraft.world.level.gamerules.GameRuleTypeVisitor;
import net.minecraft.world.level.gamerules.GameRules;
import net.neoforged.neoforge.registries.DeferredHolder;

public class CMGameRules {

    public static final Identifier ALLOW_SURVIVAL_OUT_OF_BOUNDS_KEY = CompactMachinesCore.identifier("allow_survival_oob");
    public static DeferredHolder<GameRule<?>, GameRule<Boolean>> ALLOW_SURVIVAL_OUT_OF_BOUNDS = CMRegistries.GAME_RULES
            .register(ALLOW_SURVIVAL_OUT_OF_BOUNDS_KEY.getPath(), () -> GameRulesHelper.makeBooleanRule(false));

    public static final Identifier ALLOW_CREATIVE_OUT_OF_BOUNDS_KEY = CompactMachinesCore.identifier("allow_creative_oob");
    public static DeferredHolder<GameRule<?>, GameRule<Boolean>> ALLOW_CREATIVE_OUT_OF_BOUNDS = CMRegistries.GAME_RULES
            .register(ALLOW_CREATIVE_OUT_OF_BOUNDS_KEY.getPath(), () -> GameRulesHelper.makeBooleanRule(true));

    public static final Identifier ALLOW_SPECTATORS_OUT_OF_BOUNDS_KEY = CompactMachinesCore.identifier("allow_spectator_oob");
    public static DeferredHolder<GameRule<?>, GameRule<Boolean>> ALLOW_SPECTATORS_OUT_OF_BOUNDS = CMRegistries.GAME_RULES
            .register(ALLOW_SPECTATORS_OUT_OF_BOUNDS_KEY.getPath(), () -> GameRulesHelper.makeBooleanRule(false));

    public static final Identifier DAMAGE_OOB_PLAYERS_KEY = CompactMachinesCore.identifier("damage_oob");
    public static DeferredHolder<GameRule<?>, GameRule<Boolean>> DAMAGE_OOB_PLAYERS = CMRegistries.GAME_RULES
            .register(DAMAGE_OOB_PLAYERS_KEY.getPath(), () -> GameRulesHelper.makeBooleanRule(false));



    public static void prepare() {}
}
