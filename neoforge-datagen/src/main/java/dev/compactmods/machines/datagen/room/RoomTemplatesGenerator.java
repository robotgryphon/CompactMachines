package dev.compactmods.machines.datagen.room;

import dev.compactmods.compactmachines.api.room.RoomTemplate;
import dev.compactmods.compactmachines.api.room.Rooms;
import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.machine.LegacySizedTemplates;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.Vec3i;
import net.minecraft.data.PackOutput;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class RoomTemplatesGenerator extends DatapackBuiltinEntriesProvider {

    private static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Rooms.TEMPLATE_REG_KEY, RoomTemplatesGenerator::addRoomTemplates);

    public RoomTemplatesGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BUILDER, Set.of(Constants.MOD_ID));
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
