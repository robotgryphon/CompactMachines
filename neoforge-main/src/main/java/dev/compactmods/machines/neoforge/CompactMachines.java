package dev.compactmods.machines.neoforge;

import com.google.common.collect.ImmutableSet;
import dev.compactmods.machines.api.ICompactMachinesAddon;
import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.command.Commands;
import dev.compactmods.machines.neoforge.client.ClientConfig;
import dev.compactmods.machines.neoforge.client.CreativeTabs;
import dev.compactmods.machines.neoforge.config.CommonConfig;
import dev.compactmods.machines.neoforge.config.ServerConfig;
import dev.compactmods.machines.neoforge.data.functions.LootFunctions;
import dev.compactmods.machines.neoforge.dimension.Dimension;
import dev.compactmods.machines.neoforge.machine.Machines;
import dev.compactmods.machines.neoforge.room.Rooms;
import dev.compactmods.machines.neoforge.shrinking.Shrinking;
import dev.compactmods.machines.neoforge.villager.Villagers;
import net.minecraft.util.FastColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.util.Set;

@Mod(Constants.MOD_ID)
public class CompactMachines {

    public static final Marker ADDON_LIFECYCLE = MarkerManager.getMarker("addons");

    public static final int BRAND_MACHINE_COLOR = FastColor.ARGB32.color(255, 248, 246, 76);

    private static Set<ICompactMachinesAddon> loadedAddons = ImmutableSet.of();

    @SuppressWarnings("unused")
    public CompactMachines(IEventBus modBus) {
        // Package initialization here, this kick-starts the rest of the DR code (classloading)
        Machines.prepare();
        Shrinking.prepare();
        Rooms.prepare();
        Dimension.prepare();
//  todo upgrade system      MachineRoomUpgrades.prepare();
        Commands.prepare();
        LootFunctions.prepare();

        Villagers.prepare();
        CreativeTabs.prepare();

        Registries.setup(modBus);

        // loadAddons(modBus);

        // Configuration
        ModLoadingContext mlCtx = ModLoadingContext.get();
        mlCtx.registerConfig(ModConfig.Type.CLIENT, ClientConfig.CONFIG);
        mlCtx.registerConfig(ModConfig.Type.COMMON, CommonConfig.CONFIG);
        mlCtx.registerConfig(ModConfig.Type.SERVER, ServerConfig.CONFIG);
    }

    /**
     * Sets up the deferred registration for usage in package/module setup.
     */
    private static void loadAddons(IEventBus modBus) {
//        CompactMachines.loadedAddons = ServiceLoader.load(ICompactMachinesAddon.class)
//                .stream()
//                .filter(p -> Arrays.stream(p.type().getAnnotationsByType(CompactMachinesAddon.class))
//                        .anyMatch(cma -> cma.major() == 2))
//                .map(allowedThisMajor -> {
//                    allowedThisMajor.get();
//                })
//                .collect(Collectors.toSet());

//        CompactMachines.loadedAddons = AnnotationScanner.scanModList(CompactMachinesAddon.class)
//                .map(ModFileScanData.AnnotationData::memberName)
//                .map(cmAddonClass -> {
//                    try {
//                        final var cl = Class.forName(cmAddonClass);
//                        final var cla = cl.asSubclass(ICompactMachinesAddon.class);
//                        return cla.getDeclaredConstructor().newInstance();
//                    } catch (Exception e) {
//                        return null;
//                    }
//                })
//                .filter(Objects::nonNull)
//                .collect(Collectors.toSet());
//
//        CompactMachines.loadedAddons.forEach(addon -> {
//            LoggingUtil.modLog().debug(ADDON_LIFECYCLE, "Sending registration hook to addon: {}", addon.getClass().getName());
//            addon.afterRegistration();
//        });
    }

    public static Set<ICompactMachinesAddon> getAddons() {
        return loadedAddons;
    }
}
