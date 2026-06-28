package net.sinedkadis.terracompositio.datagen;


import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.crafting.StrictNBTIngredient;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.datagen.builders.AltarTransformationRecipeBuilder;
import net.sinedkadis.terracompositio.datagen.builders.FlowInfusionRecipeBuilder;
import net.sinedkadis.terracompositio.datagen.builders.MatterInfusionRecipeBuilder;
import net.sinedkadis.terracompositio.datagen.builders.TechnetiumFiringRecipeBuilder;
import net.sinedkadis.terracompositio.recipe.ECFStorageUpgradeRecipe;
import net.sinedkadis.terracompositio.recipe.NoOpRecipeSerializer;
import net.sinedkadis.terracompositio.recipe.TagTransferShapedRecipe;
import net.sinedkadis.terracompositio.recipe.WrapperResult;
import net.sinedkadis.terracompositio.registries.TCBlocks;
import net.sinedkadis.terracompositio.registries.TCItems;
import net.sinedkadis.terracompositio.registries.TCTags;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import static net.minecraft.data.recipes.RecipeBuilder.getDefaultRecipeId;

public class TCRecipeProvider extends RecipeProvider implements IConditionBuilder {

    public TCRecipeProvider(PackOutput pOutput) {
        super(pOutput);
    }

    @Override
    protected void buildRecipes(@NotNull Consumer<FinishedRecipe> pWriter) {

        specialCraftingRecipe(pWriter, ECFStorageUpgradeRecipe.SERIALIZER);

        buildCedarBlocks(pWriter);
        buildMatterInfuserBlocks(pWriter);
        buildCopperMaterials(pWriter);
        buildTechnetiumMaterials(pWriter);
        buildTechnetiumArmor(pWriter);
        buildCedarArmor(pWriter);
        buildInfusedIronMaterials(pWriter);
        buildGoldMaterials(pWriter);
        buildDesorbers(pWriter);
        buildPathPointers(pWriter);
        buildFloatingRedstone(pWriter);
        buildCFJ(pWriter);


        buildTechnetiumOreProcessing(pWriter);
        buildMisc(pWriter);
        buildSpecial(pWriter);
        buildApples(pWriter);

        buildCompat(pWriter);


    }

    private static void buildCFJ(@NotNull Consumer<FinishedRecipe> pWriter) {
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, TCBlocks.FLOW_CEDAR_ALTAR.get())
                .pattern("LLL")
                .define('L', TCTags.Items.FLOW_CEDAR_LOGS)
                .unlockedBy(getHasName(TCBlocks.FLOW_CEDAR_LOG.get()), has(TCBlocks.FLOW_CEDAR_LOG.get()))
                .save(pWriter);


        ItemStack bookLevel1 = createCFJBook(1);
        ItemStack bookLevel2 = createCFJBook(2);
        ItemStack bookLevel3 = createCFJBook(3);
        ItemStack bookLevel4 = createCFJBook(4);
        ItemStack bookLevel5 = createCFJBook(5);


        AltarTransformationRecipeBuilder.create(
                        bookLevel1,
                        Ingredient.of(Items.BOOK)
                )
                .save(pWriter, "upgrade_book_to_day_1");
        AltarTransformationRecipeBuilder.create(
                        bookLevel2,
                        StrictNBTIngredient.of(
                                bookLevel1
                        ),
                        Ingredient.of(TCBlocks.FLOW_CEDAR_SAPLING.get().asItem().getDefaultInstance())
                )
                .save(pWriter, "upgrade_book_to_day_2");
        AltarTransformationRecipeBuilder.create(
                        bookLevel3,
                        StrictNBTIngredient.of(
                                bookLevel2
                        ),
                        Ingredient.of(TCItems.FLOW_BOTTLE.get().asItem().getDefaultInstance())
                )
                .save(pWriter, "upgrade_book_to_day_3");
        AltarTransformationRecipeBuilder.create(
                        bookLevel4,
                        StrictNBTIngredient.of(
                                bookLevel3
                        ),
                        Ingredient.of(TCItems.ECF_CHARGE.get().asItem().getDefaultInstance())
                )
                .save(pWriter, "upgrade_book_to_day_4");
        AltarTransformationRecipeBuilder.create(
                        bookLevel5,
                        StrictNBTIngredient.of(
                                bookLevel4
                        ),
                        Ingredient.of(TCItems.TECHNETIUM_INGOT.get().asItem().getDefaultInstance())
                )
                .save(pWriter, "upgrade_book_to_day_5");
    }

    private void buildApples(@NotNull Consumer<FinishedRecipe> pWriter) {
        FlowInfusionRecipeBuilder.create(
                TCItems.APPLE_OF_KNOWLEDGE.get().getDefaultInstance(),
                Ingredient.of(Items.APPLE),
                100,
                200
        ).save(pWriter, TerraCompositio.modLoc("flow_infusion/apple_of_knowledge"));
        FlowInfusionRecipeBuilder.create(
                TCItems.APPLE_OF_IGNORANCE.get().getDefaultInstance(),
                Ingredient.of(Items.GOLDEN_APPLE),
                100,
                200
        ).save(pWriter, TerraCompositio.modLoc("flow_infusion/apple_of_ignorance"));
    }

    private void buildCompat(@NotNull Consumer<FinishedRecipe> pWriter) {
        if (ModList.get().isLoaded("create")) {
            TerraCompositio.createCompat.getDataGen().buildRecipes(this, pWriter);
        }
    }

    private static void buildCedarBlocks(@NotNull Consumer<FinishedRecipe> pWriter) {
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, TCBlocks.FLOW_CEDAR_STAIRS.get())
                .pattern("S  ")
                .pattern("SS ")
                .pattern("SSS")
                .define('S', TCBlocks.FLOW_CEDAR_PLANKS.get())
                .unlockedBy(getHasName(TCBlocks.FLOW_CEDAR_PLANKS.get()), has(TCBlocks.FLOW_CEDAR_PLANKS.get()))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, TCBlocks.FLOW_CEDAR_SLAB.get(),2)
                .pattern("SSS")
                .define('S', TCBlocks.FLOW_CEDAR_PLANKS.get())
                .unlockedBy(getHasName(TCBlocks.FLOW_CEDAR_PLANKS.get()), has(TCBlocks.FLOW_CEDAR_PLANKS.get()))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, TCBlocks.FLOW_CEDAR_PRESSURE_PLATE.get())
                .pattern("SS")
                .define('S', TCBlocks.FLOW_CEDAR_PLANKS.get())
                .unlockedBy(getHasName(TCBlocks.FLOW_CEDAR_PLANKS.get()), has(TCBlocks.FLOW_CEDAR_PLANKS.get()))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, TCBlocks.FLOW_CEDAR_FENCE.get())
                .pattern("SFS")
                .pattern("SFS")
                .define('S', TCBlocks.FLOW_CEDAR_PLANKS.get())
                .define('F', Items.STICK)
                .unlockedBy(getHasName(TCBlocks.FLOW_CEDAR_PLANKS.get()), has(TCBlocks.FLOW_CEDAR_PLANKS.get()))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, TCBlocks.FLOW_CEDAR_FENCE_GATE.get())
                .pattern("FSF")
                .pattern("FSF")
                .define('S', TCBlocks.FLOW_CEDAR_PLANKS.get())
                .define('F', Items.STICK)
                .unlockedBy(getHasName(TCBlocks.FLOW_CEDAR_PLANKS.get()), has(TCBlocks.FLOW_CEDAR_PLANKS.get()))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, TCBlocks.FLOW_CEDAR_DOOR.get(),3)
                .pattern("SS")
                .pattern("SS")
                .pattern("SS")
                .define('S', TCBlocks.FLOW_CEDAR_PLANKS.get())
                .unlockedBy(getHasName(TCBlocks.FLOW_CEDAR_PLANKS.get()), has(TCBlocks.FLOW_CEDAR_PLANKS.get()))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, TCBlocks.FLOW_CEDAR_TRAPDOOR.get(),2)
                .pattern("SSS")
                .pattern("SSS")
                .define('S', TCBlocks.FLOW_CEDAR_PLANKS.get())
                .unlockedBy(getHasName(TCBlocks.FLOW_CEDAR_PLANKS.get()), has(TCBlocks.FLOW_CEDAR_PLANKS.get()))
                .save(pWriter);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, TCBlocks.FLOW_CEDAR_PLANKS.get(), 4)
                .requires(TCTags.Items.FLOW_CEDAR_LOGS)
                .unlockedBy(getHasName(TCBlocks.FLOW_CEDAR_LOG.get()), has(TCBlocks.FLOW_CEDAR_LOG.get()))
                .save(pWriter);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, TCBlocks.FLOW_CEDAR_BUTTON.get(), 1)
                .requires(TCBlocks.FLOW_CEDAR_PLANKS.get())
                .unlockedBy(getHasName(TCBlocks.FLOW_CEDAR_PLANKS.get()), has(TCBlocks.FLOW_CEDAR_PLANKS.get()))
                .save(pWriter);
    }

    private static void buildTechnetiumOreProcessing(@NotNull Consumer<FinishedRecipe> pWriter) {
        int base1 = 128;
        int consumeMultiplier = 5;
        int generationMultiplier = 4;

        int base2 = base1 * consumeMultiplier;
        int base3 = base2 * consumeMultiplier;

        MatterInfusionRecipeBuilder.create(
                TCItems.RAW_TECHNETIUM.get(),4,
                        TCItems.LOW_ENRICHED_TECHNETIUM.get(),1,
                        Items.COAL, base1, 200, 30)
                .save(pWriter);

        MatterInfusionRecipeBuilder.create(
                        TCItems.LOW_ENRICHED_TECHNETIUM.get(),4,
                        TCItems.MEDIUM_ENRICHED_TECHNETIUM.get(),1,
                        Items.REDSTONE, base2, 1000, 30)
                .save(pWriter);

        MatterInfusionRecipeBuilder.create(
                        TCItems.MEDIUM_ENRICHED_TECHNETIUM.get(),4,
                        TCItems.HIGH_ENRICHED_TECHNETIUM.get(),1,
                        Items.DIAMOND, base3, 2000, 30)
                .save(pWriter);

        TechnetiumFiringRecipeBuilder.create(
                        TCItems.LOW_ENRICHED_TECHNETIUM.get(),
                        base1 * generationMultiplier
                )
                .save(pWriter, Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(TCItems.LOW_ENRICHED_TECHNETIUM.get())).withPrefix("firing/"));
        TechnetiumFiringRecipeBuilder.create(
                        TCItems.MEDIUM_ENRICHED_TECHNETIUM.get(),
                        base2 * generationMultiplier)
                .save(pWriter, Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(TCItems.MEDIUM_ENRICHED_TECHNETIUM.get())).withPrefix("firing/"));
        TechnetiumFiringRecipeBuilder.create(
                        TCItems.HIGH_ENRICHED_TECHNETIUM.get(),
                        base3 * generationMultiplier)
                .save(pWriter, Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(TCItems.HIGH_ENRICHED_TECHNETIUM.get())).withPrefix("firing/"));


        oreSmelting(pWriter,
                List.of(TCItems.RAW_TECHNETIUM.get(),
                        TCBlocks.TECHNETIUM_ORE.get(),
                        TCBlocks.TECHNETIUM_DEEPSLATE_ORE.get()),
                RecipeCategory.MISC,
                TCItems.TECHNETIUM_INGOT.get(),
                0.25f,
                200,
                "technetium");
        oreSmelting(pWriter,
                List.of(TCItems.LOW_ENRICHED_TECHNETIUM.get()),
                RecipeCategory.MISC,
                TCItems.RAW_TECHNETIUM.get(),
                0.25f,
                200,
                "technetium");
        oreSmelting(pWriter,
                List.of(TCItems.MEDIUM_ENRICHED_TECHNETIUM.get()),
                RecipeCategory.MISC,
                TCItems.LOW_ENRICHED_TECHNETIUM.get(),
                0.25f,
                200,
                "technetium");
        oreSmelting(pWriter,
                List.of(TCItems.HIGH_ENRICHED_TECHNETIUM.get()),
                RecipeCategory.MISC,
                TCItems.MEDIUM_ENRICHED_TECHNETIUM.get(),
                0.25f,
                200,
                "technetium");

        oreBlasting(pWriter,
                List.of(TCItems.RAW_TECHNETIUM.get(),
                        TCBlocks.TECHNETIUM_ORE.get(),
                        TCBlocks.TECHNETIUM_DEEPSLATE_ORE.get()),
                RecipeCategory.MISC,
                TCItems.TECHNETIUM_INGOT.get(),
                0.25f,
                100,
                "technetium");
        oreBlasting(pWriter,
                List.of(TCItems.LOW_ENRICHED_TECHNETIUM.get()),
                RecipeCategory.MISC,
                TCItems.RAW_TECHNETIUM.get(),
                0.25f,
                100,
                "technetium");
        oreBlasting(pWriter,
                List.of(TCItems.MEDIUM_ENRICHED_TECHNETIUM.get()),
                RecipeCategory.MISC,
                TCItems.LOW_ENRICHED_TECHNETIUM.get(),
                0.25f,
                100,
                "technetium");
        oreBlasting(pWriter,
                List.of(TCItems.HIGH_ENRICHED_TECHNETIUM.get()),
                RecipeCategory.MISC,
                TCItems.MEDIUM_ENRICHED_TECHNETIUM.get(),
                0.25f,
                100,
                "technetium");
    }

    private void buildTechnetiumArmor(@NotNull Consumer<FinishedRecipe> pWriter) {
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, TCItems.TECHNETIUM_CROWN.get())
                .pattern("T T")
                .pattern("TTT")
                .define('T', TCItems.TECHNETIUM_INGOT.get())
                .unlockedBy(getHasName(TCItems.TECHNETIUM_INGOT.get()), has(TCItems.TECHNETIUM_INGOT.get()))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, TCItems.TECHNETIUM_CROWN.get())
                .pattern("T T")
                .pattern("TTT")
                .pattern(" C ")
                .define('T', TCItems.TECHNETIUM_INGOT.get())
                .define('C', Tags.Items.ARMORS_HELMETS)
                .unlockedBy(getHasName(TCItems.TECHNETIUM_INGOT.get()), has(TCItems.TECHNETIUM_INGOT.get()))
                .save(WrapperResult.ofType(TagTransferShapedRecipe.SERIALIZER, pWriter),
                        TerraCompositio.modLoc("with_tag/" + getDefaultRecipeId(TCItems.TECHNETIUM_CROWN.get()).getPath()));


        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, TCItems.TECHNETIUM_CHESTPLATE.get())
                .pattern("T T")
                .pattern("TTT")
                .pattern("TTT")
                .define('T', TCItems.TECHNETIUM_INGOT.get())
                .unlockedBy(getHasName(TCItems.TECHNETIUM_INGOT.get()), has(TCItems.TECHNETIUM_INGOT.get()))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, TCItems.TECHNETIUM_CHESTPLATE.get())
                .pattern("TCT")
                .pattern("TTT")
                .pattern("TTT")
                .define('T', TCItems.TECHNETIUM_INGOT.get())
                .define('C', Tags.Items.ARMORS_CHESTPLATES)
                .unlockedBy(getHasName(TCItems.TECHNETIUM_INGOT.get()), has(TCItems.TECHNETIUM_INGOT.get()))
                .save(WrapperResult.ofType(TagTransferShapedRecipe.SERIALIZER, pWriter),
                        TerraCompositio.modLoc("with_tag/" + getDefaultRecipeId(TCItems.TECHNETIUM_CHESTPLATE.get()).getPath()));


        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, TCItems.TECHNETIUM_LEGGINGS.get())
                .pattern("TTT")
                .pattern("T T")
                .pattern("T T")
                .define('T', TCItems.TECHNETIUM_INGOT.get())
                .unlockedBy(getHasName(TCItems.TECHNETIUM_INGOT.get()), has(TCItems.TECHNETIUM_INGOT.get()))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, TCItems.TECHNETIUM_LEGGINGS.get())
                .pattern("TTT")
                .pattern("TCT")
                .pattern("T T")
                .define('T', TCItems.TECHNETIUM_INGOT.get())
                .define('C', Tags.Items.ARMORS_LEGGINGS)
                .unlockedBy(getHasName(TCItems.TECHNETIUM_INGOT.get()), has(TCItems.TECHNETIUM_INGOT.get()))
                .save(WrapperResult.ofType(TagTransferShapedRecipe.SERIALIZER, pWriter),
                        TerraCompositio.modLoc("with_tag/" + getDefaultRecipeId(TCItems.TECHNETIUM_LEGGINGS.get()).getPath()));


        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, TCItems.TECHNETIUM_BOOTS.get())
                .pattern("T T")
                .pattern("T T")
                .pattern("F F")
                .define('F', Items.FEATHER)
                .define('T', TCItems.TECHNETIUM_INGOT.get())
                .unlockedBy(getHasName(TCItems.TECHNETIUM_INGOT.get()), has(TCItems.TECHNETIUM_INGOT.get()))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, TCItems.TECHNETIUM_BOOTS.get())
                .pattern("T T")
                .pattern("TCT")
                .pattern("F F")
                .define('F', Items.FEATHER)
                .define('T', TCItems.TECHNETIUM_INGOT.get())
                .define('C', Tags.Items.ARMORS_BOOTS)
                .unlockedBy(getHasName(TCItems.TECHNETIUM_INGOT.get()), has(TCItems.TECHNETIUM_INGOT.get()))
                .save(WrapperResult.ofType(TagTransferShapedRecipe.SERIALIZER, pWriter),
                        TerraCompositio.modLoc("with_tag/" + getDefaultRecipeId(TCItems.TECHNETIUM_BOOTS.get()).getPath()));
    }

    private static void buildPathPointers(@NotNull Consumer<FinishedRecipe> pWriter) {
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, TCBlocks.PP_COLLECTOR.get())
                .pattern(" T ")
                .pattern("LLT")
                .pattern(" T ")
                .define('T', TCItems.TECHNETIUM_INGOT.get())
                .define('L', TCTags.Items.FLOW_CEDAR_LOGS)
                .unlockedBy(getHasName(TCItems.TECHNETIUM_INGOT.get()), has(TCItems.TECHNETIUM_INGOT.get()))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, TCBlocks.PP_EMITTER.get())
                .pattern(" TR")
                .pattern("TLI")
                .pattern(" TR")
                .define('T', TCItems.TECHNETIUM_INGOT.get())
                .define('I', TCItems.INFUSED_IRON_INGOT.get())
                .define('R', TCItems.INFUSED_IRON_ROD.get())
                .define('L', TCTags.Items.FLOW_CEDAR_LOGS)
                .unlockedBy(getHasName(TCItems.TECHNETIUM_INGOT.get()), has(TCItems.TECHNETIUM_INGOT.get()))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, TCBlocks.PP_SENDER.get())
                .pattern(" TC")
                .pattern("TC ")
                .pattern(" TC")
                .define('T', TCItems.TECHNETIUM_INGOT.get())
                .define('C', Items.COPPER_INGOT)
                .unlockedBy(getHasName(TCItems.TECHNETIUM_INGOT.get()), has(TCItems.TECHNETIUM_INGOT.get()))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, TCBlocks.PP_RECEIVER.get())
                .pattern("LT ")
                .pattern(" LT")
                .pattern("LT ")
                .define('T', TCItems.TECHNETIUM_INGOT.get())
                .define('L', Items.LAPIS_LAZULI)
                .unlockedBy(getHasName(TCItems.TECHNETIUM_INGOT.get()), has(TCItems.TECHNETIUM_INGOT.get()))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, TCBlocks.PP_EXTRACTOR.get())
                .pattern("LT ")
                .pattern("NLT")
                .pattern("LT ")
                .define('T', TCItems.TECHNETIUM_INGOT.get())
                .define('N', TCItems.INFUSED_IRON_NUGGET.get())
                .define('L', TCTags.Items.FLOW_CEDAR_LOGS)
                .unlockedBy(getHasName(TCItems.TECHNETIUM_INGOT.get()), has(TCItems.TECHNETIUM_INGOT.get()))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, TCBlocks.PP_INFUSER.get())
                .pattern(" TR")
                .pattern("TLR")
                .pattern(" TR")
                .define('T', TCItems.TECHNETIUM_INGOT.get())
                .define('R', TCItems.INFUSED_IRON_ROD.get())
                .define('L', TCTags.Items.FLOW_CEDAR_LOGS)
                .unlockedBy(getHasName(TCItems.TECHNETIUM_INGOT.get()), has(TCItems.TECHNETIUM_INGOT.get()))
                .save(pWriter);
    }

    private static void buildWrenchAxe(@NotNull Consumer<FinishedRecipe> pWriter) {
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, TCItems.WRENCH_AXE.get())
                .pattern("II")
                .pattern("IS")
                .pattern(" S")
                .define('I', TCItems.INFUSED_IRON_INGOT.get())
                .define('S', Items.STICK)
                .unlockedBy(getHasName(TCItems.INFUSED_IRON_INGOT.get()), has(TCItems.INFUSED_IRON_INGOT.get()))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, TCItems.WRENCH_AXE.get())
                .pattern("II")
                .pattern("SI")
                .pattern("S ")
                .define('I', TCItems.INFUSED_IRON_INGOT.get())
                .define('S', Items.STICK)
                .unlockedBy(getHasName(TCItems.INFUSED_IRON_INGOT.get()), has(TCItems.INFUSED_IRON_INGOT.get()))
                .save(pWriter, Objects.requireNonNull(ResourceLocation.tryBuild(Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(TCItems.WRENCH_AXE.get())).getNamespace(),
                        Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(TCItems.WRENCH_AXE.get())).getPath() + "_mirrored")));
    }

    private static void buildMisc(@NotNull Consumer<FinishedRecipe> pWriter) {
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, Items.BUNDLE)
                .pattern("S")
                .pattern("L")
                .define('S', Items.STRING)
                .define('L',Items.LEATHER)
                .unlockedBy(getHasName(Items.STRING), has(Items.STRING))
                .save(pWriter);
        FlowInfusionRecipeBuilder.create(
                TCItems.ECF_CHARGE.get().getDefaultInstance(),
                Ingredient.of(Items.SNOWBALL),
                10,
                20
        ).save(pWriter, TerraCompositio.modLoc("flow_infusion/ecf_charge"));
        TechnetiumFiringRecipeBuilder.create(
                        TCItems.ECF_CHARGE.get(),
                        8)
                .save(pWriter, TerraCompositio.modLoc("firing/ecf_charge"));
        oreSmelting(pWriter,
                List.of(TCItems.ECF_CHARGE.get()),
                RecipeCategory.MISC,
                Items.SNOWBALL,
                0.0f,
                40,
                "technetium");
        cookSmelting(pWriter,
                List.of(TCItems.ECF_CHARGE.get()),
                RecipeCategory.MISC,
                Items.SNOWBALL,
                0.0f,
                20,
                "technetium");
        FlowInfusionRecipeBuilder.create(
                TCItems.INFUSED_FERTILIZER.get().getDefaultInstance(),
                Ingredient.of(ItemTags.VILLAGER_PLANTABLE_SEEDS),
                200,
                200
        ).save(pWriter, TerraCompositio.modLoc("flow_infusion/infused_fertilizer"));
    }

    private static void buildDesorbers(@NotNull Consumer<FinishedRecipe> pWriter) {
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, TCBlocks.CONSTRUCTION_DESORBER.get())
                .pattern("ILI")
                .pattern("III")
                .define('I', Items.IRON_INGOT)
                .define('L', TCTags.Items.FLOW_CEDAR_LOGS)
                .unlockedBy(getHasName(TCBlocks.FLOW_CEDAR_LOG.get()), has(TCBlocks.FLOW_CEDAR_LOG.get()))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, TCBlocks.CULTIVATION_DESORBER.get())
                .pattern("ILI")
                .pattern("III")
                .define('I', Items.COPPER_INGOT)
                .define('L', TCTags.Items.FLOW_CEDAR_LOGS)
                .unlockedBy(getHasName(TCBlocks.FLOW_CEDAR_LOG.get()), has(TCBlocks.FLOW_CEDAR_LOG.get()))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, TCBlocks.TIME_PASSAGE_DESORBER.get())
                .pattern(" R ")
                .pattern("ILI")
                .pattern("III")
                .define('I', Items.GOLD_INGOT)
                .define('R', Items.REDSTONE)
                .define('L', TCTags.Items.FLOW_CEDAR_LOGS)
                .unlockedBy(getHasName(TCBlocks.FLOW_CEDAR_LOG.get()), has(TCBlocks.FLOW_CEDAR_LOG.get()))
                .save(pWriter);
    }

    private static void buildGoldMaterials(@NotNull Consumer<FinishedRecipe> pWriter) {
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, TCItems.GOLD_ROD.get(), 2)
                .pattern("R")
                .pattern("R")
                .define('R', Items.GOLD_INGOT)
                .unlockedBy(getHasName(Items.GOLD_INGOT), has(Items.GOLD_INGOT))
                .save(pWriter);
    }

    private static void buildInfusedIronMaterials(@NotNull Consumer<FinishedRecipe> pWriter) {
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(TCItems.INFUSED_IRON_INGOT.get());

        FlowInfusionRecipeBuilder.create(
                TCItems.INFUSED_IRON_INGOT.get().getDefaultInstance(),
                Ingredient.of(Items.IRON_INGOT),
                50,
                100
        ).save(pWriter,TerraCompositio.modLoc("flow_infusion/infused_iron"));


        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, TCItems.INFUSED_IRON_INGOT.get(), 1)
                .requires(TCItems.INFUSED_IRON_NUGGET.get(),9)
                .unlockedBy(getHasName(TCItems.INFUSED_IRON_INGOT.get()), has(TCItems.INFUSED_IRON_INGOT.get()))
                .save(pWriter,Objects.requireNonNull(ResourceLocation.tryBuild(Objects.requireNonNull(key).getNamespace(),
                        Objects.requireNonNull(key).getPath() + "_from_nugget")));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, TCBlocks.INFUSED_IRON_BLOCK.get(), 1)
                .requires(TCItems.INFUSED_IRON_INGOT.get(),9)
                .unlockedBy(getHasName(TCItems.INFUSED_IRON_INGOT.get()), has(TCItems.TECHNETIUM_INGOT.get()))
                .save(pWriter);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, TCItems.INFUSED_IRON_INGOT.get(), 9)
                .requires(TCBlocks.INFUSED_IRON_BLOCK.get(),1)
                .unlockedBy(getHasName(TCItems.INFUSED_IRON_INGOT.get()), has(TCItems.INFUSED_IRON_INGOT.get()))
                .save(pWriter,Objects.requireNonNull(ResourceLocation.tryBuild(Objects.requireNonNull(key).getNamespace(),
                        Objects.requireNonNull(key).getPath() + "_from_block")));
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, TCItems.INFUSED_IRON_ROD.get(), 2)
                .pattern("R")
                .pattern("R")
                .define('R', TCItems.INFUSED_IRON_INGOT.get())
                .unlockedBy(getHasName(TCItems.INFUSED_IRON_INGOT.get()), has(TCItems.INFUSED_IRON_INGOT.get()))
                .save(pWriter);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, TCItems.INFUSED_IRON_NUGGET.get(), 9)
                .requires(TCItems.INFUSED_IRON_INGOT.get())
                .unlockedBy(getHasName(TCItems.INFUSED_IRON_INGOT.get()), has(TCItems.INFUSED_IRON_INGOT.get()))
                .save(pWriter);
    }

    private static void buildCedarArmor(@NotNull Consumer<FinishedRecipe> pWriter) {
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, TCItems.FLOW_CEDAR_BOOTS.get())
                .pattern("WLW")
                .pattern("W W")
                .define('W', TCTags.Items.FLOW_CEDAR_LOGS)
                .define('L', Items.LEATHER)
                .unlockedBy(getHasName(TCBlocks.FLOW_CEDAR_LOG.get()), has(TCBlocks.FLOW_CEDAR_LOG.get()))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, TCItems.FLOW_CEDAR_LEGGINGS.get())
                .pattern("WWW")
                .pattern("WLW")
                .pattern("W W")
                .define('W', TCTags.Items.FLOW_CEDAR_LOGS)
                .define('L', Items.LEATHER)
                .unlockedBy(getHasName(TCBlocks.FLOW_CEDAR_LOG.get()), has(TCBlocks.FLOW_CEDAR_LOG.get()))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, TCItems.FLOW_CEDAR_CHESTPLATE.get())
                .pattern("WLW")
                .pattern("WWW")
                .pattern("WWW")
                .define('W', TCTags.Items.FLOW_CEDAR_LOGS)
                .define('L', Items.LEATHER)
                .unlockedBy(getHasName(TCBlocks.FLOW_CEDAR_LOG.get()), has(TCBlocks.FLOW_CEDAR_LOG.get()))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, TCItems.FLOW_CEDAR_HELMET.get())
                .pattern("WWW")
                .pattern("WLW")
                .define('W', TCTags.Items.FLOW_CEDAR_LOGS)
                .define('L', Items.LEATHER)
                .unlockedBy(getHasName(TCBlocks.FLOW_CEDAR_LOG.get()), has(TCBlocks.FLOW_CEDAR_LOG.get()))
                .save(pWriter);
    }

    private static void buildTechnetiumMaterials(@NotNull Consumer<FinishedRecipe> pWriter) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, TCItems.RAW_TECHNETIUM.get(), 9)
                .requires(TCBlocks.TECHNETIUM_RAW_ORE_BLOCK.get())
                .unlockedBy(getHasName(TCBlocks.TECHNETIUM_RAW_ORE_BLOCK.get()), has(TCBlocks.TECHNETIUM_RAW_ORE_BLOCK.get()))
                .save(pWriter);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, TCBlocks.TECHNETIUM_RAW_ORE_BLOCK.get(), 1)
                .requires(TCItems.RAW_TECHNETIUM.get(),9)
                .unlockedBy(getHasName(TCItems.RAW_TECHNETIUM.get()), has(TCItems.RAW_TECHNETIUM.get()))
                .save(pWriter);
        ResourceLocation technetium = ForgeRegistries.ITEMS.getKey(TCItems.TECHNETIUM_INGOT.get());
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, TCItems.TECHNETIUM_INGOT.get(), 1)
                .requires(TCItems.TECHNETIUM_NUGGET.get(),9)
                .unlockedBy(getHasName(TCItems.TECHNETIUM_INGOT.get()), has(TCItems.TECHNETIUM_INGOT.get()))
                .save(pWriter,Objects.requireNonNull(ResourceLocation.tryBuild(Objects.requireNonNull(technetium).getNamespace(),
                        Objects.requireNonNull(technetium).getPath() + "_from_nugget")));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, TCBlocks.TECHNETIUM_BLOCK.get(), 1)
                .requires(TCItems.TECHNETIUM_INGOT.get(),9)
                .unlockedBy(getHasName(TCItems.TECHNETIUM_INGOT.get()), has(TCItems.TECHNETIUM_INGOT.get()))
                .save(pWriter);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, TCItems.TECHNETIUM_INGOT.get(), 9)
                .requires(TCBlocks.TECHNETIUM_BLOCK.get(),1)
                .unlockedBy(getHasName(TCItems.TECHNETIUM_INGOT.get()), has(TCItems.TECHNETIUM_INGOT.get()))
                .save(pWriter,Objects.requireNonNull(ResourceLocation.tryBuild(Objects.requireNonNull(technetium).getNamespace(),
                        Objects.requireNonNull(technetium).getPath() + "_from_block")));
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, TCItems.TECHNETIUM_ROD.get(), 2)
                .pattern("R")
                .pattern("R")
                .define('R', TCItems.TECHNETIUM_INGOT.get())
                .unlockedBy(getHasName(TCItems.TECHNETIUM_INGOT.get()), has(TCItems.TECHNETIUM_INGOT.get()))
                .save(pWriter);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, TCItems.TECHNETIUM_NUGGET.get(), 9)
                .requires(TCItems.TECHNETIUM_INGOT.get())
                .unlockedBy(getHasName(TCItems.TECHNETIUM_INGOT.get()), has(TCItems.TECHNETIUM_INGOT.get()))
                .save(pWriter);
    }

    private static void buildCopperMaterials(@NotNull Consumer<FinishedRecipe> pWriter) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, TCItems.COPPER_NUGGET.get(), 9)
                .requires(Items.COPPER_INGOT)
                .unlockedBy(getHasName(Items.COPPER_INGOT), has(Items.COPPER_INGOT))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, TCItems.COPPER_ROD.get(), 2)
                .pattern("R")
                .pattern("R")
                .define('R', Items.COPPER_INGOT)
                .unlockedBy(getHasName(Items.COPPER_INGOT), has(Items.COPPER_INGOT))
                .save(pWriter);
    }

    private static void buildSpecial(@NotNull Consumer<FinishedRecipe> pWriter) {
        buildWrenchAxe(pWriter);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, TCBlocks.WEDGE.get())
                .pattern("S S")
                .pattern("SSS")
                .pattern(" S ")
                .define('S', Items.IRON_NUGGET)
                .unlockedBy(getHasName(Items.IRON_BARS), has(Items.IRON_BARS))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, TCItems.FLOW_INFUSER_KIT.get())
                .pattern(" N ")
                .pattern("SNS")
                .pattern(" N ")
                .define('S', Items.STICK)
                .define('N', TCItems.COPPER_NUGGET.get())
                .unlockedBy(getHasName(Items.STICK), has(TCItems.COPPER_NUGGET.get()))
                .save(pWriter);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, TCItems.SHIELDED_BUNDLE.get(), 1)
                .requires(Items.BUNDLE)
                .requires(TCItems.INFUSED_IRON_INGOT.get())
                .unlockedBy(getHasName(TCItems.INFUSED_IRON_INGOT.get()), has(Items.BUNDLE))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, TCItems.FLUID_APPLIER.get())
                .pattern("  N")
                .pattern(" T ")
                .pattern("S  ")
                .define('T', TCItems.TECHNETIUM_INGOT.get())
                .define('N', TCItems.INFUSED_IRON_NUGGET.get())
                .define('S', Items.STICK)
                .unlockedBy(getHasName(TCItems.TECHNETIUM_INGOT.get()), has(TCItems.TECHNETIUM_INGOT.get()))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TCBlocks.AIR_SATURATOR.get())
                .pattern(" I ")
                .pattern("RCR")
                .pattern(" L ")
                .define('I', Items.IRON_INGOT)
                .define('C', Items.COBWEB)
                .define('R', TCItems.INFUSED_IRON_ROD.get())
                .define('L', TCBlocks.FLOW_CEDAR_LOG.get())
                .unlockedBy(getHasName(TCItems.INFUSED_IRON_INGOT.get()), has(TCItems.INFUSED_IRON_INGOT.get()))
                .save(pWriter);
    }

    private static void buildMatterInfuserBlocks(@NotNull Consumer<FinishedRecipe> pWriter) {
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, TCBlocks.MATTER_INFUSER_PORT.get())
                .pattern(" R ")
                .pattern("RFR")
                .pattern(" R ")
                .define('R', TCItems.COPPER_ROD.get())
                .define('F',Items.ITEM_FRAME)
                .unlockedBy(getHasName(Items.ITEM_FRAME), has(Items.ITEM_FRAME))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, TCBlocks.MATTER_INFUSER_UNIT.get())
                .pattern("RRR")
                .pattern(" I ")
                .pattern("RRR")
                .define('R', TCItems.COPPER_ROD.get())
                .define('I',TCItems.INFUSED_IRON_INGOT.get())
                .unlockedBy(getHasName(Items.ITEM_FRAME), has(Items.ITEM_FRAME))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, TCItems.OUTPUT_BUS.get())
                .pattern(" R ")
                .pattern("RHR")
                .pattern(" R ")
                .define('R', TCItems.COPPER_ROD.get())
                .define('H', Items.IRON_INGOT)
                .unlockedBy(getHasName(Items.IRON_INGOT), has(Items.IRON_INGOT))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, TCItems.INPUT_BUS.get())
                .pattern(" R ")
                .pattern("RHR")
                .pattern(" R ")
                .define('R', Items.LAPIS_LAZULI)
                .define('H', Items.IRON_INGOT)
                .unlockedBy(getHasName(Items.IRON_INGOT), has(Items.IRON_INGOT))
                .save(pWriter);
    }

    private void buildFloatingRedstone(@NotNull Consumer<FinishedRecipe> pWriter) {
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, TCBlocks.FLOATING_REDSTONE.get())
                .pattern("R")
                .pattern("N")
                .define('R', Items.REDSTONE)
                .define('N', TCItems.INFUSED_IRON_NUGGET.get())
                .unlockedBy(getHasName(TCItems.INFUSED_IRON_INGOT.get()), has(TCItems.INFUSED_IRON_INGOT.get()))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, TCBlocks.FLOATING_TORCH_HOLDER.get())
                .pattern("P")
                .pattern("I")
                .define('P', Items.FLOWER_POT)
                .define('I', TCItems.INFUSED_IRON_INGOT.get())
                .unlockedBy(getHasName(TCItems.INFUSED_IRON_INGOT.get()), has(TCItems.INFUSED_IRON_INGOT.get()))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, TCBlocks.FLOATING_REPEATER.get())
                .pattern("TRT")
                .pattern("SIS")
                .define('T', Items.REDSTONE_TORCH)
                .define('R', Items.REDSTONE)
                .define('S', Items.STONE)
                .define('I',TCItems.INFUSED_IRON_INGOT.get())
                .unlockedBy(getHasName(TCItems.INFUSED_IRON_INGOT.get()), has(TCItems.INFUSED_IRON_INGOT.get()))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, TCBlocks.FLOATING_COMPARATOR.get())
                .pattern(" T ")
                .pattern("TQT")
                .pattern("SIS")
                .define('T', Items.REDSTONE_TORCH)
                .define('Q', Items.QUARTZ)
                .define('S', Items.STONE)
                .define('I',TCItems.INFUSED_IRON_INGOT.get())
                .unlockedBy(getHasName(TCItems.INFUSED_IRON_INGOT.get()), has(TCItems.INFUSED_IRON_INGOT.get()))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, TCBlocks.INFUSED_IRON_PRESSURE_PLATE.get())
                .pattern("II")
                .define('I',TCItems.INFUSED_IRON_INGOT.get())
                .unlockedBy(getHasName(TCItems.INFUSED_IRON_INGOT.get()), has(TCItems.INFUSED_IRON_INGOT.get()))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, TCBlocks.INFUSED_IRON_DOOR.get())
                .pattern("II")
                .pattern("II")
                .pattern("II")
                .define('I',TCItems.INFUSED_IRON_INGOT.get())
                .unlockedBy(getHasName(TCItems.INFUSED_IRON_INGOT.get()), has(TCItems.INFUSED_IRON_INGOT.get()))
                .save(pWriter);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, TCBlocks.FLOATING_BUTTON.get(), 1)
                .requires(TCItems.INFUSED_IRON_INGOT.get())
                .requires(ItemTags.STONE_BUTTONS)
                .unlockedBy(getHasName(TCItems.INFUSED_IRON_INGOT.get()), has(TCItems.INFUSED_IRON_INGOT.get()))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, TCBlocks.FLOATING_LEVER.get())
                .pattern("n")
                .pattern("p")
                .pattern("i")
                .define('i', TCItems.INFUSED_IRON_INGOT.get())
                .define('n', TCItems.INFUSED_IRON_NUGGET.get())
                .define('p', TCBlocks.FLOW_CEDAR_PLANKS.get())
                .unlockedBy(getHasName(TCItems.INFUSED_IRON_INGOT.get()), has(TCItems.INFUSED_IRON_INGOT.get()))
                .save(pWriter);
    }

    private static ItemStack createCFJBook(int day) {
        ItemStack stack = new ItemStack(TCItems.CREATION_FLOW_JOURNAL.get());
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("day", day);
        stack.setTag(nbt);
        return stack;
    }

    protected static void oreSmelting(@NotNull Consumer<FinishedRecipe> pFinishedRecipeConsumer, List<ItemLike> pIngredients, @NotNull RecipeCategory pCategory, @NotNull ItemLike pResult, float pExperience, int pCookingTIme, @NotNull String pGroup) {
        oreCooking(pFinishedRecipeConsumer, RecipeSerializer.SMELTING_RECIPE, pIngredients, pCategory, pResult, pExperience, pCookingTIme, pGroup, "_from_smelting");
    }

    protected static void oreBlasting(@NotNull Consumer<FinishedRecipe> pFinishedRecipeConsumer, List<ItemLike> pIngredients, @NotNull RecipeCategory pCategory, @NotNull ItemLike pResult, float pExperience, int pCookingTime, @NotNull String pGroup) {
        oreCooking(pFinishedRecipeConsumer, RecipeSerializer.BLASTING_RECIPE, pIngredients, pCategory, pResult, pExperience, pCookingTime, pGroup, "_from_blasting");
    }

    protected static void cookSmelting(@NotNull Consumer<FinishedRecipe> pFinishedRecipeConsumer, List<ItemLike> pIngredients, @NotNull RecipeCategory pCategory, @NotNull ItemLike pResult, float pExperience, int pCookingTime, @NotNull String pGroup) {
        oreCooking(pFinishedRecipeConsumer, RecipeSerializer.SMOKING_RECIPE, pIngredients, pCategory, pResult, pExperience, pCookingTime, pGroup, "_from_cooking");
    }

    protected static void oreCooking(@NotNull Consumer<FinishedRecipe> pFinishedRecipeConsumer, @NotNull RecipeSerializer<? extends AbstractCookingRecipe> pCookingSerializer, List<ItemLike> pIngredients, @NotNull RecipeCategory pCategory, @NotNull ItemLike pResult, float pExperience, int pCookingTime, @NotNull String pGroup, String pRecipeName) {
        for(ItemLike itemlike : pIngredients) {
            SimpleCookingRecipeBuilder.generic(Ingredient.of(itemlike), pCategory, pResult,
                    pExperience, pCookingTime, pCookingSerializer)
                    .group(pGroup).unlockedBy(getHasName(itemlike), has(itemlike))
                    .save(pFinishedRecipeConsumer,  TerraCompositio.MOD_ID + ":" + getItemName(pResult) + pRecipeName + "_" + getItemName(itemlike));
        }
    }

    @ParametersAreNonnullByDefault
    @MethodsReturnNonnullByDefault
    protected void specialCraftingRecipe(Consumer<FinishedRecipe> consumer, NoOpRecipeSerializer<? extends CraftingRecipe> serializer) {
        ResourceLocation name = ForgeRegistries.RECIPE_SERIALIZERS.getKey(serializer);
        assert name != null;
        SpecialRecipeBuilder.special(serializer).save(consumer, TerraCompositio.modLoc("dynamic/" + name.getPath()).toString());
    }
}
