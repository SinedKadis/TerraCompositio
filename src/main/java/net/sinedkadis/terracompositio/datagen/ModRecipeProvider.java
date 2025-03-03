package net.sinedkadis.terracompositio.datagen;


import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.block.ModBlocks;
import net.sinedkadis.terracompositio.item.ModItems;
import net.sinedkadis.terracompositio.util.ModTags;
import org.jetbrains.annotations.NotNull;


import java.util.List;
import java.util.function.Consumer;

public class ModRecipeProvider extends RecipeProvider implements IConditionBuilder {
    /*private static final List<ItemLike> SAPPHIRE_SMELTABLES = List.of(ModItems.RAW_SAPPHIRE.get(),
            ModBlocks.SAPPHIRE_ORE.get(), ModBlocks.DEEPSLATE_SAPPHIRE_ORE.get(), ModBlocks.NETHER_SAPPHIRE_ORE.get(),
            ModBlocks.END_STONE_SAPPHIRE_ORE.get());*/

    public ModRecipeProvider(PackOutput pOutput) {
        super(pOutput);
    }

    @Override
    protected void buildRecipes(@NotNull Consumer<FinishedRecipe> pWriter) {
        //oreSmelting(pWriter, SAPPHIRE_SMELTABLES, RecipeCategory.MISC, ModItems.SAPPHIRE.get(), 0.25f, 200, "sapphire");
        //oreBlasting(pWriter, SAPPHIRE_SMELTABLES, RecipeCategory.MISC, ModItems.SAPPHIRE.get(), 0.25f, 100, "sapphire");

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.FLOW_CEDAR_STAIRS.get())
                .pattern("S  ")
                .pattern("SS ")
                .pattern("SSS")
                .define('S', ModBlocks.FLOW_CEDAR_PLANKS.get())
                .unlockedBy(getHasName(ModBlocks.FLOW_CEDAR_PLANKS.get()), has(ModBlocks.FLOW_CEDAR_PLANKS.get()))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.FLOW_CEDAR_SLAB.get(),2)
                .pattern("SSS")
                .define('S', ModBlocks.FLOW_CEDAR_PLANKS.get())
                .unlockedBy(getHasName(ModBlocks.FLOW_CEDAR_PLANKS.get()), has(ModBlocks.FLOW_CEDAR_PLANKS.get()))
                .save(pWriter);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.FLOW_CEDAR_PRESSURE_PLATE.get())
                .pattern("SS")
                .define('S', ModBlocks.FLOW_CEDAR_PLANKS.get())
                .unlockedBy(getHasName(ModBlocks.FLOW_CEDAR_PLANKS.get()), has(ModBlocks.FLOW_CEDAR_PLANKS.get()))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.FLOW_CEDAR_FENCE.get())
                .pattern("SFS")
                .pattern("SFS")
                .define('S', ModBlocks.FLOW_CEDAR_PLANKS.get())
                .define('F', Items.STICK)
                .unlockedBy(getHasName(ModBlocks.FLOW_CEDAR_PLANKS.get()), has(ModBlocks.FLOW_CEDAR_PLANKS.get()))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.FLOW_CEDAR_FENCE_GATE.get())
                .pattern("FSF")
                .pattern("FSF")
                .define('S', ModBlocks.FLOW_CEDAR_PLANKS.get())
                .define('F', Items.STICK)
                .unlockedBy(getHasName(ModBlocks.FLOW_CEDAR_PLANKS.get()), has(ModBlocks.FLOW_CEDAR_PLANKS.get()))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.FLOW_CEDAR_DOOR.get(),3)
                .pattern("SS")
                .pattern("SS")
                .pattern("SS")
                .define('S', ModBlocks.FLOW_CEDAR_PLANKS.get())
                .unlockedBy(getHasName(ModBlocks.FLOW_CEDAR_PLANKS.get()), has(ModBlocks.FLOW_CEDAR_PLANKS.get()))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.FLOW_CEDAR_TRAPDOOR.get(),2)
                .pattern("SSS")
                .pattern("SSS")
                .define('S', ModBlocks.FLOW_CEDAR_PLANKS.get())
                .unlockedBy(getHasName(ModBlocks.FLOW_CEDAR_PLANKS.get()), has(ModBlocks.FLOW_CEDAR_PLANKS.get()))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModBlocks.WEDGE.get())
                .pattern("S S")
                .pattern("SSS")
                .pattern(" S ")
                .define('S', Items.IRON_NUGGET)
                .unlockedBy(getHasName(Items.IRON_BARS), has(Items.IRON_BARS))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.FLOW_INFUSER_KIT.get())
                .pattern(" N ")
                .pattern("SNS")
                .pattern(" N ")
                .define('S', Items.STICK)
                .define('N',ModItems.COPPER_NUGGET.get())
                .unlockedBy(getHasName(Items.STICK), has(ModItems.COPPER_NUGGET.get()))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModBlocks.MATTER_INFUSER_PORT.get())
                .pattern(" R ")
                .pattern("RFR")
                .pattern(" R ")
                .define('R', ModItems.COPPER_ROD.get())
                .define('F',Items.ITEM_FRAME)
                .unlockedBy(getHasName(Items.ITEM_FRAME), has(Items.ITEM_FRAME))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.INPUT_BUS.get())
                .pattern(" R ")
                .pattern("RHR")
                .pattern(" R ")
                .define('R', ModItems.COPPER_ROD.get())
                .define('H', Items.IRON_INGOT)
                .unlockedBy(getHasName(Items.IRON_INGOT), has(Items.IRON_INGOT))
                .save(pWriter);


        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, ModBlocks.FLOW_CEDAR_PLANKS.get(), 4)
                .requires(ModTags.Items.FLOW_CEDAR_LOGS)
                .unlockedBy(getHasName(ModBlocks.FLOW_CEDAR_LOG.get()), has(ModBlocks.FLOW_CEDAR_LOG.get()))
                .save(pWriter);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, ModBlocks.FLOW_CEDAR_BUTTON.get(), 4)
                .requires(ModBlocks.FLOW_CEDAR_PLANKS.get())
                .unlockedBy(getHasName(ModBlocks.FLOW_CEDAR_PLANKS.get()), has(ModBlocks.FLOW_CEDAR_PLANKS.get()))
                .save(pWriter);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.COPPER_NUGGET.get(), 9)
                .requires(Items.COPPER_INGOT)
                .unlockedBy(getHasName(Items.COPPER_INGOT), has(Items.COPPER_INGOT))
                .save(pWriter);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.FLOW_CONTAINING_RAW_ORE.get(), 9)
                .requires(ModBlocks.FLOW_CONTAINING_RAW_ORE_BLOCK.get())
                .unlockedBy(getHasName(ModBlocks.FLOW_CONTAINING_RAW_ORE_BLOCK.get()), has(ModBlocks.FLOW_CONTAINING_RAW_ORE_BLOCK.get()))
                .save(pWriter);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModBlocks.FLOW_CONTAINING_RAW_ORE_BLOCK.get(), 1)
                .requires(ModItems.FLOW_CONTAINING_RAW_ORE.get(),9)
                .unlockedBy(getHasName(ModItems.FLOW_CONTAINING_RAW_ORE.get()), has(ModItems.FLOW_CONTAINING_RAW_ORE.get()))
                .save(pWriter);



        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.FLOW_CEDAR_BOOTS.get())
                .pattern("WLW")
                .pattern("W W")
                .define('W', ModTags.Items.FLOW_CEDAR_LOGS)
                .define('L', Items.LEATHER)
                .unlockedBy(getHasName(ModBlocks.FLOW_CEDAR_LOG.get()), has(ModBlocks.FLOW_CEDAR_LOG.get()))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.FLOW_CEDAR_LEGGINGS.get())
                .pattern("WWW")
                .pattern("WLW")
                .pattern("W W")
                .define('W', ModTags.Items.FLOW_CEDAR_LOGS)
                .define('L', Items.LEATHER)
                .unlockedBy(getHasName(ModBlocks.FLOW_CEDAR_LOG.get()), has(ModBlocks.FLOW_CEDAR_LOG.get()))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.FLOW_CEDAR_CHESTPLATE.get())
                .pattern("WLW")
                .pattern("WWW")
                .pattern("WWW")
                .define('W', ModTags.Items.FLOW_CEDAR_LOGS)
                .define('L', Items.LEATHER)
                .unlockedBy(getHasName(ModBlocks.FLOW_CEDAR_LOG.get()), has(ModBlocks.FLOW_CEDAR_LOG.get()))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.FLOW_CEDAR_HELMET.get())
                .pattern("WWW")
                .pattern("WLW")
                .define('W', ModTags.Items.FLOW_CEDAR_LOGS)
                .define('L', Items.LEATHER)
                .unlockedBy(getHasName(ModBlocks.FLOW_CEDAR_LOG.get()), has(ModBlocks.FLOW_CEDAR_LOG.get()))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.INFUSED_IRON_ROD.get())
                .pattern("R")
                .pattern("R")
                .define('R', ModItems.INFUSED_IRON_INGOT.get())
                .unlockedBy(getHasName(ModItems.INFUSED_IRON_INGOT.get()), has(ModItems.INFUSED_IRON_INGOT.get()))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.GOLD_ROD.get())
                .pattern("R")
                .pattern("R")
                .define('R', Items.GOLD_INGOT)
                .unlockedBy(getHasName(Items.GOLD_INGOT), has(Items.GOLD_INGOT))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.COPPER_ROD.get())
                .pattern("R")
                .pattern("R")
                .define('R', Items.COPPER_INGOT)
                .unlockedBy(getHasName(Items.COPPER_INGOT), has(Items.COPPER_INGOT))
                .save(pWriter);
        /*ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, ModBlocks.NONFLOW_PLANKS.get(), 4)
                .requires(ModBlocks.NONFLOW_WOOD.get())
                .unlockedBy(getHasName(ModBlocks.NONFLOW_WOOD.get()), has(ModBlocks.NONFLOW_WOOD.get()))
                .save(pWriter);*/
    }

    protected static void oreSmelting(@NotNull Consumer<FinishedRecipe> pFinishedRecipeConsumer, List<ItemLike> pIngredients, @NotNull RecipeCategory pCategory, @NotNull ItemLike pResult, float pExperience, int pCookingTIme, @NotNull String pGroup) {
        oreCooking(pFinishedRecipeConsumer, RecipeSerializer.SMELTING_RECIPE, pIngredients, pCategory, pResult, pExperience, pCookingTIme, pGroup, "_from_smelting");
    }

    protected static void oreBlasting(@NotNull Consumer<FinishedRecipe> pFinishedRecipeConsumer, List<ItemLike> pIngredients, @NotNull RecipeCategory pCategory, @NotNull ItemLike pResult, float pExperience, int pCookingTime, @NotNull String pGroup) {
        oreCooking(pFinishedRecipeConsumer, RecipeSerializer.BLASTING_RECIPE, pIngredients, pCategory, pResult, pExperience, pCookingTime, pGroup, "_from_blasting");
    }

    protected static void oreCooking(@NotNull Consumer<FinishedRecipe> pFinishedRecipeConsumer, @NotNull RecipeSerializer<? extends AbstractCookingRecipe> pCookingSerializer, List<ItemLike> pIngredients, @NotNull RecipeCategory pCategory, @NotNull ItemLike pResult, float pExperience, int pCookingTime, @NotNull String pGroup, String pRecipeName) {
        for(ItemLike itemlike : pIngredients) {
            SimpleCookingRecipeBuilder.generic(Ingredient.of(itemlike), pCategory, pResult,
                    pExperience, pCookingTime, pCookingSerializer)
                    .group(pGroup).unlockedBy(getHasName(itemlike), has(itemlike))
                    .save(pFinishedRecipeConsumer,  TerraCompositio.MOD_ID + ":" + getItemName(pResult) + pRecipeName + "_" + getItemName(itemlike));
        }
    }
}
