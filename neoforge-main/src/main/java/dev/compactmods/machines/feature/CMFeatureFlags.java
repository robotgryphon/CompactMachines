package dev.compactmods.machines.feature;

import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;

import static dev.compactmods.machines.api.CompactMachines.identifier;

public class CMFeatureFlags {

    public static final FeatureFlag ROOM_UPDATES_FLAG = FeatureFlags.REGISTRY.getFlag(identifier("room_upgrades"));

    public static final FeatureFlagSet ROOM_UPGRADES = FeatureFlagSet.of(ROOM_UPDATES_FLAG);
}
