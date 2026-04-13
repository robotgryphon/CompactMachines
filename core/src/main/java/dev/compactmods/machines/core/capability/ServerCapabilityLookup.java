package dev.compactmods.machines.core.capability;

public interface ServerCapabilityLookup {

    <T extends ServerCapability<T, Void>> T fromCurrentServer(ServerCapability<T, Void> capability);

    <T extends ServerCapability<T, C>, C> T fromCurrentServer(ServerCapability<T, C> capability, C context);
}
