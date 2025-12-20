package dev.compactmods.machines.i18n;

import dev.compactmods.machines.api.CompactMachines;
import net.minecraft.ChatFormatting;
import net.minecraft.util.Util;
import net.minecraft.network.chat.Component;

import java.util.function.Supplier;

public interface Translations {

    Supplier<Component> HINT_HOLD_SHIFT = () -> Component.translatableWithFallback(IDs.HINT_HOLD_SHIFT, "Hold shift for details.")
            .withStyle(ChatFormatting.DARK_GRAY)
            .withStyle(ChatFormatting.ITALIC);

    Supplier<Component> UNBREAKABLE_BLOCK = () -> Component.translatableWithFallback(IDs.UNBREAKABLE_BLOCK, "Warning! Unbreakable for non-creative players!")
            .withStyle(ChatFormatting.DARK_RED);

    Supplier<Component> TELEPORT_OUT_OF_BOUNDS = () -> Component
            .translatableWithFallback(IDs.TELEPORT_OUT_OF_BOUNDS, "An otherworldly force prevents your teleportation.")
            .withStyle(ChatFormatting.DARK_RED)
            .withStyle(ChatFormatting.ITALIC);

    interface IDs {
        String TELEPORT_OUT_OF_BOUNDS = Util.makeDescriptionId("messages", CompactMachines.identifier("teleport_oob"));
        String HOW_DID_YOU_GET_HERE = Util.makeDescriptionId("messages", CompactMachines.identifier("how_did_you_get_here"));
        String HINT_HOLD_SHIFT = Util.makeDescriptionId("messages", CompactMachines.identifier("hint.hold_shift"));
        String UNBREAKABLE_BLOCK = Util.makeDescriptionId("messages", CompactMachines.identifier("solid_wall"));
    }
}
