package net.sinedkadis.terracompositio.datagen;


import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;
import net.minecraftforge.registries.ForgeRegistries;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.datagen.builders.MatterInfusionRecipeBuilder;
import net.sinedkadis.terracompositio.datagen.builders.TechnetiumFiringRecipeBuilder;
import net.sinedkadis.terracompositio.registries.ModBlocks;
import net.sinedkadis.terracompositio.registries.ModItems;
import net.sinedkadis.terracompositio.registries.ModTags;
import org.jetbrains.annotations.NotNull;


import java.util.List;
import java.util.Objects;
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
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.OUTPUT_BUS.get())
                .pattern(" R ")
                .pattern("RHR")
                .pattern(" R ")
                .define('R', ModItems.COPPER_ROD.get())
                .define('H', Items.IRON_INGOT)
                .unlockedBy(getHasName(Items.IRON_INGOT), has(Items.IRON_INGOT))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.INPUT_BUS.get())
                .pattern(" R ")
                .pattern("RHR")
                .pattern(" R ")
                .define('R', Items.LAPIS_LAZULI)
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
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.RAW_TECHNETIUM.get(), 9)
                .requires(ModBlocks.TECHNETIUM_RAW_ORE_BLOCK.get())
                .unlockedBy(getHasName(ModBlocks.TECHNETIUM_RAW_ORE_BLOCK.get()), has(ModBlocks.TECHNETIUM_RAW_ORE_BLOCK.get()))
                .save(pWriter);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModBlocks.TECHNETIUM_RAW_ORE_BLOCK.get(), 1)
                .requires(ModItems.RAW_TECHNETIUM.get(),9)
                .unlockedBy(getHasName(ModItems.RAW_TECHNETIUM.get()), has(ModItems.RAW_TECHNETIUM.get()))
                .save(pWriter);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, ModItems.SHIELDED_BUNDLE.get(), 1)
                .requires(Items.BUNDLE)
                .requires(ModItems.INFUSED_IRON_INGOT.get())
                .unlockedBy(getHasName(ModItems.INFUSED_IRON_INGOT.get()), has(Items.BUNDLE))
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
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.CONSTRUCTION_DESORBER.get())
                .pattern("ILI")
                .pattern("III")
                .define('I', Items.IRON_INGOT)
                .define('L',ModTags.Items.FLOW_CEDAR_LOGS)
                .unlockedBy(getHasName(ModBlocks.FLOW_CEDAR_LOG.get()), has(Items.IRON_INGOT))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.CULTIVATION_DESORBER.get())
                .pattern("ILI")
                .pattern("III")
                .define('I', Items.COPPER_INGOT)
                .define('L',ModTags.Items.FLOW_CEDAR_LOGS)
                .unlockedBy(getHasName(ModBlocks.FLOW_CEDAR_LOG.get()), has(Items.COPPER_INGOT))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.TIME_PASSAGE_DESORBER.get())
                .pattern("ILI")
                .pattern("III")
                .define('I', Items.GOLD_INGOT)
                .define('L',ModTags.Items.FLOW_CEDAR_LOGS)
                .unlockedBy(getHasName(ModBlocks.FLOW_CEDAR_LOG.get()), has(Items.GOLD_INGOT))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, Items.BUNDLE)
                .pattern("S")
                .pattern("L")
                .define('S', Items.STRING)
                .define('L',Items.LEATHER)
                .unlockedBy(getHasName(Items.STRING), has(Items.LEATHER))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.WRENCH_AXE.get())
                .pattern("II")
                .pattern("IS")
                .pattern(" S")
                .define('I', ModItems.INFUSED_IRON_INGOT.get())
                .define('S', Items.STICK)
                .unlockedBy(getHasName(ModItems.INFUSED_IRON_INGOT.get()), has(Items.STICK))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.WRENCH_AXE.get())
                .pattern("II")
                .pattern("SI")
                .pattern("S ")
                .define('I', ModItems.INFUSED_IRON_INGOT.get())
                .define('S', Items.STICK)
                .unlockedBy(getHasName(ModItems.INFUSED_IRON_INGOT.get()), has(Items.STICK))
                .save(pWriter, Objects.requireNonNull(ResourceLocation.tryBuild(Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(ModItems.WRENCH_AXE.get())).getNamespace(),
                        Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(ModItems.WRENCH_AXE.get())).getPath() + "_mirrored")));




        MatterInfusionRecipeBuilder.create(
                ModItems.RAW_TECHNETIUM.get(),4,
                        ModItems.LOW_ENRICHED_TECHNETIUM.get(),1,
                        Items.COAL,100,200,30)
                .save(pWriter);
        MatterInfusionRecipeBuilder.create(
                        ModItems.LOW_ENRICHED_TECHNETIUM.get(),4,
                        ModItems.MEDIUM_ENRICHED_TECHNETIUM.get(),1,
                        Items.REDSTONE,1000,1000,30)
                .save(pWriter);
        MatterInfusionRecipeBuilder.create(
                        ModItems.MEDIUM_ENRICHED_TECHNETIUM.get(),4,
                        ModItems.HIGH_ENRICHED_TECHNETIUM.get(),1,
                        Items.DIAMOND,10000,2000,30)
                .save(pWriter);

        oreSmelting(pWriter,
                List.of(ModItems.RAW_TECHNETIUM.get(),
                        ModBlocks.TECHNETIUM_ORE.get(),
                        ModBlocks.TECHNETIUM_DEEPSLATE_ORE.get(),
                        ModItems.LOW_ENRICHED_TECHNETIUM.get(),
                        ModItems.MEDIUM_ENRICHED_TECHNETIUM.get(),
                        ModItems.HIGH_ENRICHED_TECHNETIUM.get()),
                RecipeCategory.MISC,
                ModItems.TECHNETIUM_INGOT.get(),
                0.25f,
                200,
                "technetium");
        oreBlasting(pWriter,
                List.of(ModItems.RAW_TECHNETIUM.get(),
                        ModBlocks.TECHNETIUM_ORE.get(),
                        ModBlocks.TECHNETIUM_DEEPSLATE_ORE.get(),
                        ModItems.LOW_ENRICHED_TECHNETIUM.get(),
                        ModItems.MEDIUM_ENRICHED_TECHNETIUM.get(),
                        ModItems.HIGH_ENRICHED_TECHNETIUM.get()),
                RecipeCategory.MISC,
                ModItems.TECHNETIUM_INGOT.get(),
                0.25f,
                100,
                "technetium");

        TechnetiumFiringRecipeBuilder.create(
                ModItems.LOW_ENRICHED_TECHNETIUM.get(),
                200
                )
                .unlockedBy(getHasName(ModItems.LOW_ENRICHED_TECHNETIUM.get()), has(ModItems.LOW_ENRICHED_TECHNETIUM.get()))
                .save(pWriter, Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(ModItems.LOW_ENRICHED_TECHNETIUM.get())).withPrefix("recipes/technetium_firing_recipe/"));
        TechnetiumFiringRecipeBuilder.create(
                        ModItems.MEDIUM_ENRICHED_TECHNETIUM.get(),
                        2000)
                .unlockedBy(getHasName(ModItems.MEDIUM_ENRICHED_TECHNETIUM.get()), has(ModItems.MEDIUM_ENRICHED_TECHNETIUM.get()))
                .save(pWriter, Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(ModItems.MEDIUM_ENRICHED_TECHNETIUM.get())).withPrefix("recipes/technetium_firing_recipe/"));
        TechnetiumFiringRecipeBuilder.create(
                        ModItems.HIGH_ENRICHED_TECHNETIUM.get(),
                        20000)
                .unlockedBy(getHasName(ModItems.HIGH_ENRICHED_TECHNETIUM.get()), has(ModItems.HIGH_ENRICHED_TECHNETIUM.get()))
                .save(pWriter, Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(ModItems.HIGH_ENRICHED_TECHNETIUM.get())).withPrefix("recipes/technetium_firing_recipe/"));
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
