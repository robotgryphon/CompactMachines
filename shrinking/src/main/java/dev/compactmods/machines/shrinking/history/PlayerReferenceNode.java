package dev.compactmods.machines.shrinking.history;

import dev.compactmods.feather.node.Node;

import java.util.UUID;

record PlayerReferenceNode(UUID id, UUID data) implements Node<UUID> {
    public UUID playerID() {
        return data;
    }
}
