package dev.compactmods.machines.datagen;

import dev.compactmods.machines.neoforge.room.Rooms;
import dev.compactmods.machines.neoforge.shrinking.Shrinking;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.Tags;

import java.util.concurrent.CompletableFuture;

public class RecipeGenerator extends RecipeProvider {
    public RecipeGenerator(PackOutput packOutput) {
        super(packOutput);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, Rooms.ITEM_BREAKABLE_WALL.get(), 8)
                .pattern("DDD")
                .pattern("D D")
                .pattern("DDD")
                .define('D', Items.POLISHED_DEEPSLATE)
                .unlockedBy("picked_up_deepslate", has(Tags.Items.COBBLESTONE_DEEPSLATE))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, Shrinking.PERSONAL_SHRINKING_DEVICE.get())
                .pattern(" P ")
                .pattern("EBE")
                .pattern(" I ")
                .define('P', Tags.Items.GLASS_PANES)
                .define('E', Items.ENDER_EYE)
                .define('B', Items.BOOK)
                .define('I', Tags.Items.INGOTS_IRON)
                .unlockedBy("picked_up_ender_eye", has(Items.ENDER_EYE))
                .save(recipeOutput);

//        TunnelRecipeBuilder.tunnel(BuiltInTunnels.ITEM_TUNNEL_DEF.getId(), 2)
//                .requires(Ingredient.of(Tags.Items.CHESTS))
//                .requires(Items.ENDER_PEARL)
//                .requires(Items.REDSTONE)
//                .requires(Items.OBSERVER)
//                .unlockedBy("observer", has(Items.OBSERVER))
//                .save(recipeOutput);
//
//        TunnelRecipeBuilder.tunnel(BuiltInTunnels.FLUID_TUNNEL_DEF.getId(), 2)
//                .requires(Items.BUCKET)
//                .requires(Items.ENDER_PEARL)
//                .requires(Items.REDSTONE)
//                .requires(Items.OBSERVER)
//                .unlockedBy("observer", has(Items.OBSERVER))
//                .save(recipeOutput);
//
//        TunnelRecipeBuilder.tunnel(BuiltInTunnels.FORGE_ENERGY.getId(), 2)
//                .requires(Items.GLOWSTONE_DUST)
//                .requires(Items.ENDER_PEARL)
//                .requires(Items.REDSTONE)
//                .requires(Items.OBSERVER)
//                .unlockedBy("observer", has(Items.OBSERVER))
//                .save(recipeOutput);

        // addMachineRecipes(recipeOutput);
    }

//    @SuppressWarnings("removal")
//    private void addMachineRecipes(RecipeOutput consumer) {
//        registerMachineRecipe(consumer, LegacySizedTemplates.EMPTY_TINY, Tags.Items.STORAGE_BLOCKS_COPPER);
//        registerMachineRecipe(consumer, LegacySizedTemplates.EMPTY_SMALL, Tags.Items.STORAGE_BLOCKS_IRON);
//        registerMachineRecipe(consumer, LegacySizedTemplates.EMPTY_NORMAL, Tags.Items.STORAGE_BLOCKS_GOLD);
//        registerMachineRecipe(consumer, LegacySizedTemplates.EMPTY_LARGE, Tags.Items.STORAGE_BLOCKS_DIAMOND);
//        registerMachineRecipe(consumer, LegacySizedTemplates.EMPTY_GIANT, Tags.Items.OBSIDIAN);
//        registerMachineRecipe(consumer, LegacySizedTemplates.EMPTY_COLOSSAL, Tags.Items.INGOTS_NETHERITE);
//
//        registerMachineRecipe(consumer, new ResourceLocation(Constants.MOD_ID, "absurd"),
//                new RoomTemplate(25, FastColor.ARGB32.color(255, 0, 166, 88)),
//                Tags.Items.NETHER_STARS);
//    }
//
//    @SuppressWarnings("removal")
//    @Deprecated(forRemoval = true, since = "5.2.0")
//    protected void registerMachineRecipe(Consumer<FinishedRecipe> consumer, LegacySizedTemplates template, TagKey<Item> center) {
//        registerMachineRecipe(consumer, template.id(), template.template(), center);
//    }
//
//    protected void registerMachineRecipe(Consumer<FinishedRecipe> consumer, ResourceLocation temId, RoomTemplate tem, TagKey<Item> center) {
//        Item wall = Walls.ITEM_BREAKABLE_WALL.get();
//        ShapedWithNbtRecipeBuilder recipe = ShapedWithNbtRecipeBuilder.shaped(Machines.UNBOUND_MACHINE_BLOCK_ITEM.get())
//                .pattern("WWW");
//
//        if (center != null)
//            recipe.pattern("WCW");
//        else
//            recipe.pattern("W W");
//
//        recipe.pattern("WWW").define('W', wall);
//        if (center != null)
//            recipe.define('C', center);
//
//        recipe.unlockedBy("has_recipe", has(wall));
//        recipe.addWriter(r -> {
//            final var nbt = new JsonObject();
//            nbt.addProperty(MachineNbt.NBT_TEMPLATE_ID, temId.toString());
//
//            final var blockTag = new JsonObject();
//            blockTag.addProperty("id", MachineIds.UNBOUND_MACHINE_ITEM_ID.toString());
//            blockTag.addProperty(MachineEntityNbt.NBT_TEMPLATE_ID, temId.toString());
//
//            nbt.add("BlockEntityTag", blockTag);
//            r.add("nbt", nbt);
//        });
//
//        final var recipeId = new ResourceLocation(Constants.MOD_ID, "new_machine_" + temId.getPath());
//        recipe.save(consumer, recipeId);
//    }
}
