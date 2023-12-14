package dev.compactmods.machines.neoforge;

import dev.compactmods.machines.LoggingUtil;
import dev.compactmods.machines.api.CompactMachinesAddon;
import dev.compactmods.machines.api.ICompactMachinesAddon;
import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.command.Commands;
import dev.compactmods.machines.neoforge.client.ClientConfig;
import dev.compactmods.machines.neoforge.config.CommonConfig;
import dev.compactmods.machines.neoforge.config.ServerConfig;
import dev.compactmods.machines.neoforge.data.functions.LootFunctions;
import dev.compactmods.machines.neoforge.dimension.Dimension;
import dev.compactmods.machines.neoforge.machine.Machines;
import dev.compactmods.machines.neoforge.room.ui.RoomUserInterfaceRegistration;
import dev.compactmods.machines.neoforge.shrinking.Shrinking;
import dev.compactmods.machines.neoforge.util.AnnotationScanner;
import dev.compactmods.machines.neoforge.villager.Villagers;
import dev.compactmods.machines.neoforge.wall.Walls;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.neoforgespi.language.ModFileScanData;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Mod(Constants.MOD_ID)
public class CompactMachines {

    public static final Marker ADDON_LIFECYCLE = MarkerManager.getMarker("addons");

    private static Set<ICompactMachinesAddon> loadedAddons;

    public CompactMachines() {
        Registries.setup();
        preparePackages();
        doRegistration();

        // Configuration
        ModLoadingContext mlCtx = ModLoadingContext.get();
        mlCtx.registerConfig(ModConfig.Type.CLIENT, ClientConfig.CONFIG);
        mlCtx.registerConfig(ModConfig.Type.COMMON, CommonConfig.CONFIG);
        mlCtx.registerConfig(ModConfig.Type.SERVER, ServerConfig.CONFIG);
    }

    /**
     * Sets up the deferred registration for usage in package/module setup.
     */
    private static void doRegistration() {
        var bus = FMLJavaModLoadingContext.get().getModEventBus();

        Registries.BLOCKS.register(bus);
        Registries.ITEMS.register(bus);
        Registries.BLOCK_ENTITIES.register(bus);
        Registries.CONTAINERS.register(bus);
        Registries.ROOM_TEMPLATES_DR.register(bus);
        Registries.UPGRADES.register(bus);
        Registries.COMMAND_ARGUMENT_TYPES.register(bus);
        Registries.LOOT_FUNCS.register(bus);
        Registries.VILLAGERS.register(bus);
        // Villagers.TRADES.register(bus);
        Registries.POINTS_OF_INTEREST.register(bus);

//        CompactMachines.loadedAddons = ServiceLoader.load(ICompactMachinesAddon.class)
//                .stream()
//                .filter(p -> Arrays.stream(p.type().getAnnotationsByType(CompactMachinesAddon.class))
//                        .anyMatch(cma -> cma.major() == 2))
//                .map(allowedThisMajor -> {
//                    allowedThisMajor.get();
//                })
//                .collect(Collectors.toSet());

        CompactMachines.loadedAddons = AnnotationScanner.scanModList(CompactMachinesAddon.class)
                .map(ModFileScanData.AnnotationData::memberName)
                .map(cmAddonClass -> {
                    try {
                        final var cl = Class.forName(cmAddonClass);
                        final var cla = cl.asSubclass(ICompactMachinesAddon.class);
                        return cla.getDeclaredConstructor().newInstance();
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        CompactMachines.loadedAddons.forEach(addon -> {
            LoggingUtil.modLog().debug(ADDON_LIFECYCLE, "Sending registration hook to addon: {}", addon.getClass().getName());
            addon.afterRegistration();
        });
    }

    private static void preparePackages() {
        // Package initialization here, this kickstarts the rest of the DR code (classloading)
        Machines.prepare();
        Walls.prepare();
        Shrinking.prepare();

        RoomUserInterfaceRegistration.prepare();
        Dimension.prepare();
//  fixme      MachineRoomUpgrades.prepare();
        Commands.prepare();
        LootFunctions.prepare();

        Villagers.prepare();
    }

    public static Set<ICompactMachinesAddon> getAddons() {
        return loadedAddons;
    }
}
