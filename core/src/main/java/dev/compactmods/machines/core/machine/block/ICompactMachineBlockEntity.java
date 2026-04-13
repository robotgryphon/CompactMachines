package dev.compactmods.machines.core.machine.block;

import dev.compactmods.machines.core.machine.MachineColor;

public interface ICompactMachineBlockEntity {
    MachineColor getMachineColor();

    void setMachineColor(MachineColor newColor);
}
