package net.sinedkadis.terracompositio.compat;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.registries.ModBlocks;
import net.sinedkadis.terracompositio.recipe.FlowSaturationRecipe;
import org.jetbrains.annotations.NotNull;

public class FlowPortCategory implements IRecipeCategory<FlowSaturationRecipe> {
    public static final ResourceLocation UID = new ResourceLocation(TerraCompositio.MOD_ID,"flow_saturation");
    public static final ResourceLocation TEXTURE = new ResourceLocation(TerraCompositio.MOD_ID,
            "textures/gui/flow_port_gui.png");

    public static final RecipeType<FlowSaturationRecipe> FLOW_SATURATION_RECIPE_RECIPE_TYPE=
            new RecipeType<>(UID, FlowSaturationRecipe.class);
    private final IDrawable background;
    private final IDrawable icon;

    public FlowPortCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE,0,0,176,85);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK,new ItemStack(ModBlocks.FLOW_PORT.get()));
    }

    @Override
    public @NotNull RecipeType<FlowSaturationRecipe> getRecipeType() {
        return FLOW_SATURATION_RECIPE_RECIPE_TYPE;
    }

    @Override
    public @NotNull Component getTitle() {
        return Component.translatable("block.terracompositio.flow_port");
    }

    @Override
    public @NotNull IDrawable getBackground() {
        return this.background;
    }

    @Override
    public @NotNull IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder iRecipeLayoutBuilder, FlowSaturationRecipe flowSaturationRecipe, @NotNull IFocusGroup iFocusGroup) {
        iRecipeLayoutBuilder.addSlot(RecipeIngredientRole.INPUT,41,16).addIngredients(flowSaturationRecipe.getIngredients().get(0));
        iRecipeLayoutBuilder.addSlot(RecipeIngredientRole.OUTPUT,121,48).addItemStack(flowSaturationRecipe.getResultItem(null));
    }
}
