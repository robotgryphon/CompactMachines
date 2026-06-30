package dev.compactmods.machines;

import dev.compactmods.machines.command.Commands;
import dev.compactmods.machines.compat.InterModCompat;
import dev.compactmods.machines.core.CompactMachinesCore;
import dev.compactmods.machines.dimension.Dimension;
import dev.compactmods.machines.feature.CMFeaturePacks;
import dev.compactmods.machines.gamerule.CMGameRules;
import dev.compactmods.machines.machine.Machines;
import dev.compactmods.machines.network.CMNetworks;
import dev.compactmods.machines.room.RoomSystem;
import dev.compactmods.machines.shrinking.PlayerEventHandler;
import dev.compactmods.machines.shrinking.Shrinking;
import dev.compactmods.machines.upgrades.RoomUpgrades;
import dev.compactmods.machines.room.block.ProtectedBlockEventHandler;
import dev.compactmods.machines.villager.Villagers;
import net.minecraft.util.ARGB;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

@Mod(CompactMachinesCore.MOD_ID)
public class CompactMachinesCommon {

    public static final int BRAND_MACHINE_COLOR = ARGB.color(255, 248, 246, 76);

    @SuppressWarnings("unused")
    public CompactMachinesCommon(IEventBus modBus) {
        Machines.prepare();
        Dimension.prepare();
        Commands.prepare();
        CMGameRules.prepare();

        Villagers.prepare();

        CMDataComponents.prepare();
        CMDataAttachments.prepare();

        registerEvents(modBus);

        CMRegistries.setup(modBus);

        RoomSystem.init(modBus);
        RoomUpgrades.init(modBus);
        Shrinking.init(modBus);
    }

    private static void registerEvents(IEventBus modBus) {
        Villagers.registerEvents();

        modBus.addListener(CMFeaturePacks::addFeaturePacks);
        modBus.addListener(CMNetworks::onPacketRegistration);
        modBus.addListener(InterModCompat::enqueueCompatMessages);

        NeoForge.EVENT_BUS.addListener(Commands::onCommandsRegister);
        NeoForge.EVENT_BUS.addListener(ProtectedBlockEventHandler::leftClickBlock);
    }
}
