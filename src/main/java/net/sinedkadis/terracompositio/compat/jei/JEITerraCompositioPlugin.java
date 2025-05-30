package net.sinedkadis.terracompositio.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraftforge.registries.ForgeRegistries;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.compat.jei.categories.FlowInfusionCategory;
import net.sinedkadis.terracompositio.compat.jei.categories.FlowPortCategory;
import net.sinedkadis.terracompositio.compat.jei.categories.MatterInfusionCategory;
import net.sinedkadis.terracompositio.compat.jei.categories.TechnetiumFiringCategory;
import net.sinedkadis.terracompositio.recipe.MatterInfusionRecipe;
import net.sinedkadis.terracompositio.recipe.TechnetiumFiringRecipe;
import net.sinedkadis.terracompositio.registries.ModBlocks;
import net.sinedkadis.terracompositio.recipe.FlowInfusionRecipe;
import net.sinedkadis.terracompositio.recipe.FlowSaturationRecipe;
import net.sinedkadis.terracompositio.registries.ModItems;
import net.sinedkadis.terracompositio.screen.FlowBlockPortScreen;
import org.jetbrains.annotations.NotNull;

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
        registration.addRecipeCategories(new FlowPortCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new FlowInfusionCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new MatterInfusionCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new TechnetiumFiringCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(@NotNull IRecipeRegistration registration) {
        assert Minecraft.getInstance().level != null;
        RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();

        List<FlowSaturationRecipe> flowSaturationRecipes = recipeManager.getAllRecipesFor(FlowSaturationRecipe.Type.INSTANCE);
        List<FlowInfusionRecipe> flowInfusionRecipes = recipeManager.getAllRecipesFor(FlowInfusionRecipe.Type.INSTANCE);
        List<MatterInfusionRecipe> matterInfusionRecipes = recipeManager.getAllRecipesFor(MatterInfusionRecipe.Type.INSTANCE);
        List<TechnetiumFiringRecipe> technetiumFiringRecipes = recipeManager.getAllRecipesFor(TechnetiumFiringRecipe.Type.INSTANCE);

        registration.addRecipes(FlowPortCategory.FLOW_SATURATION_RECIPE_RECIPE_TYPE,flowSaturationRecipes);
        registration.addRecipes(FlowInfusionCategory.FLOW_INFUSION_RECIPE_RECIPE_TYPE,flowInfusionRecipes);
        registration.addRecipes(MatterInfusionCategory.MATTER_INFUSION_RECIPE_RECIPE_TYPE,matterInfusionRecipes);
        registration.addRecipes(TechnetiumFiringCategory.TECHNETIUM_FIRING_RECIPE_RECIPE_TYPE,technetiumFiringRecipes);

    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(FlowBlockPortScreen.class,60,30,20,30,
                FlowPortCategory.FLOW_SATURATION_RECIPE_RECIPE_TYPE);

    }

    @Override
    public void registerRecipeCatalysts(@NotNull IRecipeCatalystRegistration registration) {
        if (Minecraft.getInstance().level != null) {
            RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();
        }
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.FLOW_INFUSER.get()),FlowInfusionCategory.FLOW_INFUSION_RECIPE_RECIPE_TYPE);
        addCatalysts(registration,MatterInfusionCategory.MATTER_INFUSION_RECIPE_RECIPE_TYPE,
                ModBlocks.FLOW_CEDAR_CASING.get(),
                ModBlocks.MATTER_INFUSER_PORT.get(),
                ModBlocks.MATTER_INFUSER_IO.get(),
                ModItems.INPUT_BUS.get(),
                ModItems.OUTPUT_BUS.get());
        addCatalysts(registration,TechnetiumFiringCategory.TECHNETIUM_FIRING_RECIPE_RECIPE_TYPE, ForgeRegistries.BLOCKS.getValues().stream()
                .filter(block -> block instanceof AbstractFurnaceBlock)
                .map(block -> ((ItemLike) block.asItem()))
                .toList());

    }

    private static void addCatalysts(IRecipeCatalystRegistration registration,RecipeType<?> recipeType, ItemLike... items){
        for (ItemLike item : items){
            registration.addRecipeCatalyst(new ItemStack(item.asItem()),recipeType);
        }
    }
    private static void addCatalysts(IRecipeCatalystRegistration registration,RecipeType<?> recipeType, List<ItemLike> items){
        for (ItemLike item : items){
            registration.addRecipeCatalyst(new ItemStack(item.asItem()),recipeType);
        }
    }
}
