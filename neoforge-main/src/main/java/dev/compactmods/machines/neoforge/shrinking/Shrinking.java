package dev.compactmods.machines.neoforge.shrinking;

import dev.compactmods.machines.neoforge.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;

public class Shrinking {

    public static final DeferredItem<PersonalShrinkingDevice> PERSONAL_SHRINKING_DEVICE = Registries.ITEMS.register("personal_shrinking_device",
            () -> new PersonalShrinkingDevice(new Item.Properties().stacksTo(1)));

    public static void prepare() {

    }
}
