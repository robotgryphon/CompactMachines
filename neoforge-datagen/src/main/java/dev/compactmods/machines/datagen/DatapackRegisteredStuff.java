package dev.compactmods.machines.datagen;

import dev.compactmods.compactmachines.api.room.RoomTemplate;
import dev.compactmods.compactmachines.api.room.Rooms;
import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.machine.LegacySizedTemplates;
import dev.compactmods.machines.neoforge.dimension.Dimension;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class DatapackRegisteredStuff extends DatapackBuiltinEntriesProvider {
    private static final ResourceLocation COMPACT_BIOME = new ResourceLocation(Constants.MOD_ID, "machine");

    private static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.BIOME, DatapackRegisteredStuff::generateBiomes)
            .add(Registries.DIMENSION_TYPE, DatapackRegisteredStuff::generateDimensionTypes)
            .add(Registries.LEVEL_STEM, DatapackRegisteredStuff::generateDimensions)
            .add(Rooms.TEMPLATE_REG_KEY, DatapackRegisteredStuff::addRoomTemplates);


    DatapackRegisteredStuff(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> registries) {
        super(packOutput, registries, BUILDER, Set.of(Constants.MOD_ID));
    }

    private static void generateBiomes(BootstapContext<Biome> ctx) {
        var spawnBuilder = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.plainsSpawns(spawnBuilder);
        var spawns = spawnBuilder.build();

        final Biome compactBiome = new Biome.BiomeBuilder()
                .downfall(0)
                .generationSettings(BiomeGenerationSettings.EMPTY)
                .mobSpawnSettings(spawns)
                .hasPrecipitation(false)
                .temperature(0.8f)
                .temperatureAdjustment(Biome.TemperatureModifier.NONE)
                .specialEffects(new BiomeSpecialEffects.Builder()
                        .fogColor(12638463)
                        .waterColor(4159204)
                        .waterFogColor(329011)
                        .skyColor(0xFF000000)
                        .build())
                .build();

        ctx.register(ResourceKey.create(Registries.BIOME, COMPACT_BIOME), compactBiome);
    }

    private static void generateDimensionTypes(BootstapContext<DimensionType> ctx) {
        ctx.register(CompactDimension.DIM_TYPE_KEY, new DimensionTypeBuilder()
                .bedWorks(false)
                .respawnAnchorWorks(false)
                .fixedTime(18000L)
                .natural(false)
                .raids(false)
                .heightBounds(0, 256)
                .build());
    }

    private static void generateDimensions(BootstapContext<LevelStem> ctx) {
        final var biomes = ctx.lookup(Registries.BIOME);
        final var dimTypes = ctx.lookup(Registries.DIMENSION_TYPE);

        final var cmBiome = biomes.getOrThrow(ResourceKey.create(Registries.BIOME, COMPACT_BIOME));

        var flatSettings = new FlatLevelGeneratorSettings(Optional.empty(), cmBiome, Collections.emptyList());
        flatSettings.withBiomeAndLayers(
                List.of(new FlatLayerInfo(1, Dimension.BLOCK_MACHINE_VOID_AIR.get())),
                Optional.empty(),
                cmBiome
        );

        var stem = new LevelStem(dimTypes.getOrThrow(CompactDimension.DIM_TYPE_KEY), new FlatLevelSource(flatSettings));
        ctx.register(ResourceKey.create(Registries.LEVEL_STEM, CompactDimension.LEVEL_KEY.location()), stem);
    }

    @SuppressWarnings("removal")
    private static void addRoomTemplates(BootstapContext<RoomTemplate> ctx) {
        ctx.register(ResourceKey.create(Rooms.TEMPLATE_REG_KEY, LegacySizedTemplates.EMPTY_TINY.id()), LegacySizedTemplates.EMPTY_TINY.template());
        ctx.register(ResourceKey.create(Rooms.TEMPLATE_REG_KEY, new ResourceLocation(Constants.MOD_ID, "small")), LegacySizedTemplates.EMPTY_SMALL.template());
        ctx.register(ResourceKey.create(Rooms.TEMPLATE_REG_KEY, new ResourceLocation(Constants.MOD_ID, "normal")), LegacySizedTemplates.EMPTY_NORMAL.template());
        ctx.register(ResourceKey.create(Rooms.TEMPLATE_REG_KEY, new ResourceLocation(Constants.MOD_ID, "large")), LegacySizedTemplates.EMPTY_LARGE.template());
        ctx.register(ResourceKey.create(Rooms.TEMPLATE_REG_KEY, new ResourceLocation(Constants.MOD_ID, "giant")), LegacySizedTemplates.EMPTY_GIANT.template());
        ctx.register(ResourceKey.create(Rooms.TEMPLATE_REG_KEY, new ResourceLocation(Constants.MOD_ID, "colossal")), LegacySizedTemplates.EMPTY_COLOSSAL.template());
        ctx.register(ResourceKey.create(Rooms.TEMPLATE_REG_KEY, new ResourceLocation(Constants.MOD_ID, "absurd")), new RoomTemplate(new Vec3i(25, 25, 25),
                FastColor.ARGB32.color(255, 0, 166, 88),
                RoomTemplate.NO_TEMPLATE));
    }
}
