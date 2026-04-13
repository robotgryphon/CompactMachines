package dev.compactmods.machines.datagen.basic_room_templates;

import dev.compactmods.machines.api.CompactMachines;
import dev.compactmods.machines.api.room.template.RoomTemplate;
import dev.compactmods.machines.datagen.base.RecipeGenerator;
import dev.compactmods.machines.machine.Machines;
import dev.compactmods.machines.room.Rooms;
import dev.compactmods.machines.shrinking.Shrinking;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.Tags;

import java.util.function.UnaryOperator;

public class BasicRoomTemplateRecipeGenerator extends RecipeGenerator {

    public BasicRoomTemplateRecipeGenerator(HolderLookup.Provider holders, RecipeOutput output) {
        super(holders, output);
    }

    @Override
    protected void buildRecipes() {
        addMachineRecipe(CompactMachines.identifier("tiny"), Tags.Items.INGOTS_COPPER);
        addMachineRecipe(CompactMachines.identifier("small"), Tags.Items.INGOTS_IRON);
        addMachineRecipe(CompactMachines.identifier("normal"), Tags.Items.INGOTS_GOLD);
        addMachineRecipe(CompactMachines.identifier("large"), Tags.Items.GEMS_DIAMOND);
        addMachineRecipe(CompactMachines.identifier("giant"), Tags.Items.OBSIDIANS);
        addMachineRecipe(CompactMachines.identifier("colossal"), Tags.Items.INGOTS_NETHERITE);

        addMachineRecipe(CompactMachines.identifier("soaryn"), Tags.Items.NETHER_STARS);
        addMachineRecipe(CompactMachines.identifier("farming"), Items.DIAMOND_HOE);
    }

    private void addMachineRecipe(Identifier id, TagKey<Item> catalyst) {
        final var templateRef = this.registries.lookupOrThrow(RoomTemplate.REGISTRY_KEY)
                .getOrThrow(ResourceKey.create(RoomTemplate.REGISTRY_KEY, id));

        machineRecipeBuilder(this.output, templateRef, builder -> builder.define('P', catalyst));
    }

    private void addMachineRecipe(Identifier id, ItemLike catalyst) {
        final var templateRef = this.registries.lookupOrThrow(RoomTemplate.REGISTRY_KEY)
                .getOrThrow(ResourceKey.create(RoomTemplate.REGISTRY_KEY, id));

        machineRecipeBuilder(this.output, templateRef, builder -> builder.define('P', catalyst));
    }

    protected void machineRecipeBuilder(RecipeOutput consumer, Holder.Reference<RoomTemplate> templateRef, UnaryOperator<ShapedRecipeBuilder> configure) {
        final var builder = ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM),
                        RecipeCategory.MISC, Machines.Items.forNewRoom(templateRef).getItem())
                .pattern("WWW")
                .pattern("EPS")
                .pattern("WWW")
                .define('W', Rooms.Items.BREAKABLE_WALL)
                .define('E', Shrinking.ENLARGING_MODULE)
                .define('S', Shrinking.SHRINKING_MODULE);

        configure.apply(builder);

        builder.unlockedBy("has_recipe", has(Rooms.Items.BREAKABLE_WALL));
        builder.save(consumer);
    }
}
