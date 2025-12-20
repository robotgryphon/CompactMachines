package dev.compactmods.machines.api.machine;

import dev.compactmods.machines.api.CompactMachines;
import dev.compactmods.machines.api.util.KeyHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public interface MachineConstants {

    Identifier BOUND_MACHINE_BLOCK_ID = CompactMachines.identifier( "machine");
    Identifier BOUND_MACHINE_ENTITY = CompactMachines.identifier( "machine");
    ResourceKey<Block> BOUND_MACHINE_BLOCK_RESKEY = KeyHelper.blockResKey("machine");
    ResourceKey<Item> BOUND_MACHINE_ITEM_RESKEY = KeyHelper.itemResKey("machine");


    Identifier UNBOUND_MACHINE_BLOCK_ID = CompactMachines.identifier( "new_machine");
    Identifier UNBOUND_MACHINE_ITEM_ID = CompactMachines.identifier( "new_machine");
    Identifier UNBOUND_MACHINE_ENTITY = CompactMachines.identifier( "new_machine");

    ResourceKey<Block> UNBOUND_MACHINE_BLOCK_RESKEY = KeyHelper.blockResKey("new_machine");
    ResourceKey<Item> UNBOUND_MACHINE_ITEM_RESKEY = KeyHelper.itemResKey("new_machine");

    /**
     * Marks a block as an unbound Compact Machine; applied only to machines that are not yet bound to a room.
     */
    TagKey<Block> UNBOUND_MACHINE_BLOCK = KeyHelper.blockTag("new_machine");

    /**
     * Marks a block as a bound Compact Machine; applied only to machines that are bound to a room.
     */
    TagKey<Block> BOUND_MACHINE_BLOCK = KeyHelper.blockTag("bound_machine");

    TagKey<Block> MACHINE_BLOCK = KeyHelper.blockTag("machine");
    TagKey<Item> MACHINE_ITEM = KeyHelper.itemTagKey("machine");

    TagKey<Item> BOUND_MACHINE_ITEM = KeyHelper.itemTagKey("bound_machine");
    TagKey<Item> NEW_MACHINE_ITEM = KeyHelper.itemTagKey("new_machine");


}
