package dev.compactmods.machines.datagen.room;

import dev.compactmods.compactmachines.api.room.RoomTemplate;
import dev.compactmods.compactmachines.api.room.Rooms;
import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.machine.LegacySizedTemplates;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.Vec3i;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.HashMap;
import java.util.Set;

public class RoomTemplates {

    @SuppressWarnings("removal")
    public static void make(GatherDataEvent event) {

        final var templates = new HashMap<ResourceLocation, RoomTemplate>();
        templates.put(new ResourceLocation(Constants.MOD_ID, "tiny"), LegacySizedTemplates.EMPTY_TINY.template());
        templates.put(new ResourceLocation(Constants.MOD_ID, "small"), LegacySizedTemplates.EMPTY_SMALL.template());
        templates.put(new ResourceLocation(Constants.MOD_ID, "normal"), LegacySizedTemplates.EMPTY_NORMAL.template());
        templates.put(new ResourceLocation(Constants.MOD_ID, "large"), LegacySizedTemplates.EMPTY_LARGE.template());
        templates.put(new ResourceLocation(Constants.MOD_ID, "giant"), LegacySizedTemplates.EMPTY_GIANT.template());
        templates.put(new ResourceLocation(Constants.MOD_ID, "colossal"), LegacySizedTemplates.EMPTY_COLOSSAL.template());

        templates.put(new ResourceLocation(Constants.MOD_ID, "absurd"), new RoomTemplate(new Vec3i(25, 25, 25),
                FastColor.ARGB32.color(255, 0, 166, 88),
                RoomTemplate.NO_TEMPLATE));

        final var gen = event.getGenerator();
        gen.addProvider(event.includeServer(), (DataProvider.Factory<DatapackBuiltinEntriesProvider>) output ->
                new DatapackBuiltinEntriesProvider(output,
                        event.getLookupProvider(),
                        new RegistrySetBuilder()
                                .add(Rooms.TEMPLATE_REG_KEY, ctx -> {
                                    for (var template : templates.entrySet()) {
                                        ctx.register(ResourceKey.create(Rooms.TEMPLATE_REG_KEY, template.getKey()), template.getValue());
                                    }
                                }),
                        Set.of(Constants.MOD_ID)));
    }
}
