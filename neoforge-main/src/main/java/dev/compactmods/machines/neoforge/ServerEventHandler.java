package dev.compactmods.machines.neoforge;

import dev.compactmods.compactmachines.api.room.IRoomRegistrar;
import dev.compactmods.compactmachines.api.room.RoomApi;
import dev.compactmods.compactmachines.api.room.owner.IRoomOwners;
import dev.compactmods.compactmachines.api.room.spatial.IRoomChunkManager;
import dev.compactmods.compactmachines.api.room.spawn.IRoomSpawnManagers;
import dev.compactmods.machines.LoggingUtil;
import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.room.RoomApiInstance;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;

// @Mod.EventBusSubscriber(modid = Constants.MOD_ID)
public class ServerEventHandler {

    @SubscribeEvent
    public static void onServerAboutToStart(final ServerAboutToStartEvent server) {
        final var modLog = LoggingUtil.modLog();

        modLog.debug("Setting up room API instances.");
        final IRoomRegistrar registrar = null;
        final IRoomOwners owners = null;
        final IRoomSpawnManagers spawnManager = null;
        final IRoomChunkManager chunkManager = null;

        //noinspection UnstableApiUsage
        RoomApi.INSTANCE = new RoomApiInstance(registrar, owners, spawnManager, chunkManager);
        modLog.debug("Completed setting up room API instances.");

        modLog.debug("Starting addon scan and injection for server startup.");
//        CompactMachines.getAddons().forEach(addon -> {
//            final Supplier<IRoomOwnerLookup> ownerLookup = ForgeCompactRoomProvider::instance;
//            final Supplier<IRoomSpawnLookup> spawnLookup = ForgeCompactRoomProvider::instance;
//
//            final var injectableFields = AnnotationScanner.scanFields(addon, InjectField.class)
//                    .filter(field -> field.canAccess(addon))
//                    .collect(Collectors.toSet());
//
//            if(injectableFields.isEmpty()) return;
//
//            modLog.debug("Injecting lookup data into addon {} ...", addon.getClass());
//            AnnotationScanner.injectFields(addon, ownerLookup, injectableFields);
//            AnnotationScanner.injectFields(addon, spawnLookup, injectableFields);
//        });
    }

    @SubscribeEvent
    public static void onWorldLoaded(final LevelEvent.Load evt) {
        if (evt.getLevel() instanceof ServerLevel compactDim && compactDim.dimension().equals(CompactDimension.LEVEL_KEY)) {
        // FIXME Room upgrade initialization
//            final var levelUpgrades = RoomUpgradeManager.get(compactDim);
//            final var roomInfo = CompactRoomProvider.instance(compactDim);
//
//            levelUpgrades.implementing(ILevelLoadedUpgradeListener.class).forEach(inst -> {
//                final var upg = inst.upgrade();
//                roomInfo.forRoom(inst.room()).ifPresent(ri -> upg.onLevelLoaded(compactDim, ri));
//            });
        }
    }
}