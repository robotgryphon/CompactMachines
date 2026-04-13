package dev.compactmods.machines.datagen.basic_room_templates.lang;

import dev.compactmods.machines.datagen.base.lang.BaseLangGenerator;
import net.minecraft.data.PackOutput;

public class RoomTemplatesEnglishLangGenerator extends BaseLangGenerator {
    public RoomTemplatesEnglishLangGenerator(PackOutput packOutput) {
        super(packOutput, "en_us");
    }

    @Override
    protected void addTranslations() {
        super.addTranslations();
        blocksAndItems();
    }

    private void blocksAndItems() {
        final var machineTranslation = getMachineTranslation();
        add("machine.compactmachines.tiny", "%s (%s)".formatted(machineTranslation, "Tiny"));
        add("machine.compactmachines.small", "%s (%s)".formatted(machineTranslation, "Small"));
        add("machine.compactmachines.normal", "%s (%s)".formatted(machineTranslation, "Normal"));
        add("machine.compactmachines.large", "%s (%s)".formatted(machineTranslation, "Large"));
        add("machine.compactmachines.giant", "%s (%s)".formatted(machineTranslation, "Giant"));
        add("machine.compactmachines.colossal", "%s (%s)".formatted(machineTranslation, "Colossal"));
    }
}
