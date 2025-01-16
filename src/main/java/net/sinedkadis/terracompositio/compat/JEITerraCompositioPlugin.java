package net.sinedkadis.terracompositio.compat;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.block.ModBlocks;
import net.sinedkadis.terracompositio.recipe.FlowInfusionRecipe;
import net.sinedkadis.terracompositio.recipe.FlowSaturationRecipe;
import net.sinedkadis.terracompositio.screen.FlowBlockPortScreen;

import java.util.List;

@JeiPlugin
public class JEITerraCompositioPlugin implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(TerraCompositio.MOD_ID,"jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new FlowPortCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new FlowInfusionCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();
        List<FlowSaturationRecipe> flowSaturationRecipes = recipeManager.getAllRecipesFor(FlowSaturationRecipe.Type.INSTANCE);
        List<FlowInfusionRecipe> flowInfusionRecipes = recipeManager.getAllRecipesFor(FlowInfusionRecipe.Type.INSTANCE);
        registration.addRecipes(FlowPortCategory.FLOW_SATURATION_RECIPE_RECIPE_TYPE,flowSaturationRecipes);
        registration.addRecipes(FlowInfusionCategory.FLOW_INFUSION_RECIPE_RECIPE_TYPE,flowInfusionRecipes);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(FlowBlockPortScreen.class,60,30,20,30,
                FlowPortCategory.FLOW_SATURATION_RECIPE_RECIPE_TYPE);

    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.FLOW_INFUSER.get()),FlowInfusionCategory.FLOW_INFUSION_RECIPE_RECIPE_TYPE);
    }
}
