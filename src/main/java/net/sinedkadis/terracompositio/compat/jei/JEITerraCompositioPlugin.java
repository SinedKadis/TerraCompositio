package net.sinedkadis.terracompositio.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.*;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraftforge.registries.ForgeRegistries;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.compat.jei.categories.*;
import net.sinedkadis.terracompositio.compat.jei.extensions.CFEStorageUpdateRecipeWrapper;
import net.sinedkadis.terracompositio.recipe.*;
import net.sinedkadis.terracompositio.registries.TCBlocks;
import net.sinedkadis.terracompositio.registries.TCItems;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@JeiPlugin
public class JEITerraCompositioPlugin implements IModPlugin {
    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return Objects.requireNonNull(ResourceLocation.tryBuild(TerraCompositio.MOD_ID, "jei_plugin"));
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new FlowCedarAltarCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new FlowInfusionCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new MatterInfusionCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new TechnetiumFiringCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(@NotNull IRecipeRegistration registration) {
        assert Minecraft.getInstance().level != null;
        RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();

        List<AltarTransformationRecipe> altarTransformationRecipes = recipeManager.getAllRecipesFor(AltarTransformationRecipe.Type.INSTANCE);
        List<FlowInfusionRecipe> flowInfusionRecipes = recipeManager.getAllRecipesFor(FlowInfusionRecipe.Type.INSTANCE);
        List<MatterInfusionRecipe> matterInfusionRecipes = recipeManager.getAllRecipesFor(MatterInfusionRecipe.Type.INSTANCE);
        List<TechnetiumFiringRecipe> technetiumFiringRecipes = recipeManager.getAllRecipesFor(TechnetiumFiringRecipe.Type.INSTANCE);

        registration.addRecipes(FlowCedarAltarCategory.FLOW_CEDAR_ALTAR_RECIPE_RECIPE_TYPE, altarTransformationRecipes);
        registration.addRecipes(FlowInfusionCategory.FLOW_INFUSION_RECIPE_RECIPE_TYPE,flowInfusionRecipes);
        registration.addRecipes(MatterInfusionCategory.MATTER_INFUSION_RECIPE_RECIPE_TYPE,matterInfusionRecipes);
        registration.addRecipes(TechnetiumFiringCategory.TECHNETIUM_FIRING_RECIPE_RECIPE_TYPE,technetiumFiringRecipes);

    }

    @Override
    public void registerVanillaCategoryExtensions(IVanillaCategoryExtensionRegistration registration) {
        registration.getCraftingCategory().addCategoryExtension(CFEStorageUpgradeRecipe.class, CFEStorageUpdateRecipeWrapper::new);
    }

    @Override
    public void registerRecipeCatalysts(@NotNull IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(TCBlocks.FLOW_INFUSER.get()),FlowInfusionCategory.FLOW_INFUSION_RECIPE_RECIPE_TYPE);
        addCatalysts(registration,MatterInfusionCategory.MATTER_INFUSION_RECIPE_RECIPE_TYPE,
                TCBlocks.FLOW_CEDAR_CASING.get(),
                TCBlocks.MATTER_INFUSER_PORT.get(),
                TCBlocks.MATTER_INFUSER_UNIT.get(),
                TCItems.INPUT_BUS.get(),
                TCItems.OUTPUT_BUS.get());
        addCatalysts(registration,TechnetiumFiringCategory.TECHNETIUM_FIRING_RECIPE_RECIPE_TYPE, ForgeRegistries.BLOCKS.getValues().stream()
                .filter(block -> block instanceof AbstractFurnaceBlock)
                .map(block -> ((ItemLike) block.asItem()))
                .toList());
        addCatalysts(registration, FlowCedarAltarCategory.FLOW_CEDAR_ALTAR_RECIPE_RECIPE_TYPE,
                TCBlocks.FLOW_CEDAR_ALTAR.get(),
                TCBlocks.FLOW_CEDAR_PEDESTAL.get());

    }

    private static void addCatalysts(IRecipeCatalystRegistration registration,RecipeType<?> recipeType, ItemLike... items){
        addCatalysts(registration,recipeType, Arrays.stream(items).toList());
    }
    private static void addCatalysts(IRecipeCatalystRegistration registration,RecipeType<?> recipeType, List<ItemLike> items){
        for (ItemLike item : items){
            registration.addRecipeCatalyst(new ItemStack(item.asItem()),recipeType);
        }
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        registration.useNbtForSubtypes(TCItems.CREATION_FLOW_JOURNAL.get(),TCItems.FLUID_APPLIER.get());
    }
}
