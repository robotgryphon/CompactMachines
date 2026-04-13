package dev.compactmods.machines.capabilities;

import dev.compactmods.machines.core.capability.ServerCapability;
import dev.compactmods.machines.core.capability.ServerCapabilityLookup;
import dev.compactmods.machines.server.CompactMachinesServer;

public class ServerCapabilityProviderImpl implements ServerCapabilityLookup {

    @Override
    public <T extends ServerCapability<T, Void>> T fromCurrentServer(ServerCapability<T, Void> capability) {
        return CompactMachinesServer.getCapability(capability);
    }

    @Override
    public <T extends ServerCapability<T, C>, C> T fromCurrentServer(ServerCapability<T, C> capability, C context) {
        return CompactMachinesServer.getCapability(capability, context);
    }
}
