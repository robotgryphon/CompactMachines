package dev.compactmods.machines.feature;

import dev.compactmods.machines.api.CompactMachines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.neoforge.event.AddPackFindersEvent;

public class CMFeaturePacks {

    private static final PackSource OPTIONAL_ROOM_TEMPLATES = new RoomTemplatePackSource("basic_templates");

    public static void addFeaturePacks(final AddPackFindersEvent event) {
        addOptionalRoomTemplateDataPack(event, CompactMachines.identifier("basic_templates"), Component.literal("Compact Machines: Basic Room Templates"));
        addOptionalFeaturePack(event, CompactMachines.identifier("room_upgrades"), Component.literal("Compact Machines: Room Upgrades"));
    }

    private static void addOptionalFeaturePack(AddPackFindersEvent event, Identifier packName, Component displayName) {
        event.addPackFinders(
                CompactMachines.identifier("data/" + packName.getNamespace() + "/datapacks/" + packName.getPath()),
                PackType.SERVER_DATA,
                displayName,
                PackSource.FEATURE,
                false,
                Pack.Position.TOP
        );
    }

    private static void addOptionalRoomTemplateDataPack(AddPackFindersEvent event, Identifier packName, Component displayName) {
        event.addPackFinders(
                CompactMachines.identifier("data/" + packName.getNamespace() + "/datapacks/" + packName.getPath()),
                PackType.SERVER_DATA,
                displayName,
                OPTIONAL_ROOM_TEMPLATES,
                false,
                Pack.Position.TOP
        );
    }
}
