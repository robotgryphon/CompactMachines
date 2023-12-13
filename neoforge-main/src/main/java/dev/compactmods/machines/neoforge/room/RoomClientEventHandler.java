package dev.compactmods.machines.neoforge.room;

import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.neoforge.room.ui.overlay.RoomMetadataDebugOverlay;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.client.event.RegisterGuiOverlaysEvent;
import net.neoforged.client.gui.overlay.VanillaGuiOverlay;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class RoomClientEventHandler {

    @SubscribeEvent
    public static void onOverlayRegistration(final RegisterGuiOverlaysEvent overlays) {
        overlays.registerAbove(VanillaGuiOverlay.DEBUG_TEXT.id(), "room_meta_debug", new RoomMetadataDebugOverlay());
    }
}
