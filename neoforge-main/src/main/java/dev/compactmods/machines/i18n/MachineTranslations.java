package dev.compactmods.machines.i18n;

import dev.compactmods.machines.api.CompactMachines;
import net.minecraft.ChatFormatting;
import net.minecraft.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import java.util.function.Function;

public interface MachineTranslations {

    Function<BlockPos, Component> NOT_A_MACHINE_BLOCK = (pos) -> Component.empty();

    interface IDs {
        String OWNER = Util.makeDescriptionId("machine", CompactMachines.identifier("machine.owner"));
        String SIZE = Util.makeDescriptionId("machine", CompactMachines.identifier("machine.size"));
        String BOUND_TO = Util.makeDescriptionId("machine", CompactMachines.identifier("machine.bound_to"));
        String NEW_MACHINE = Util.makeDescriptionId("machine", CompactMachines.identifier("new_machine"));
    }
}
