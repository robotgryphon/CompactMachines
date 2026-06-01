package dev.compactmods.machines.upgrades.event;

import dev.compactmods.machines.api.room.capability.RoomCapabilities;
import dev.compactmods.machines.api.room.registry.RoomRegistry;
import dev.compactmods.machines.api.room.upgrade.RoomUpgradeComponent;
import dev.compactmods.machines.api.room.upgrade.RoomUpgradeInstance;
import dev.compactmods.machines.api.room.upgrade.capability.RoomUpgradeCapabilities;
import dev.compactmods.machines.api.room.upgrade.event.NeoForgeEventHandler;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.Event;

public class NeoForgeServerEventProcessor<TEvt extends Event> {

    private final MinecraftServer server;
    private final Class<TEvt> eventClass;
    private final RoomRegistry registrar;

    public NeoForgeServerEventProcessor(MinecraftServer server, Class<TEvt> eClass) {
        this.server = server;
        this.eventClass = eClass;
        this.registrar = RoomCapabilities.REGISTRY.getCapability(server);
    }

    public void process(TEvt event) {
        registrar.allRooms()
                .map(inst -> inst.getCapability(RoomUpgradeCapabilities.UPGRADES))
                .forEach(inst -> inst.all()
                        .forEach(upg -> processSingleUpgradeInstance(upg, event)));
    }

    private NeoForgeEventHandler<TEvt> cast(RoomUpgradeComponent handler) {
        if (handler instanceof NeoForgeEventHandler<?> eh && eh.eventType().equals(eventClass))
            //noinspection unchecked
            return (NeoForgeEventHandler<TEvt>) eh;

        throw new RuntimeException("Cannot cast event to NeoForgeEventHandler");
    }

    private void processSingleUpgradeInstance(RoomUpgradeInstance roomUpgradeInstance, TEvt evt) {
        roomUpgradeInstance.components()
                .filter(component -> component instanceof NeoForgeEventHandler<?> eh && eh.eventType().equals(eventClass))
                .map(this::cast)
                .forEach(component -> component.handle(roomUpgradeInstance, evt));
    }
}
