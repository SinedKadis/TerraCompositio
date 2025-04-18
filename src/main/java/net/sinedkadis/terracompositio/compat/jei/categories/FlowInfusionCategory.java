package net.sinedkadis.terracompositio.compat.jei.categories;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.registries.ModBlocks;
import net.sinedkadis.terracompositio.recipe.FlowInfusionRecipe;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class FlowInfusionCategory implements IRecipeCategory<FlowInfusionRecipe> {
    public static final ResourceLocation UID = new ResourceLocation(TerraCompositio.MOD_ID,"flow_infusion");
    public static final ResourceLocation TEXTURE = new ResourceLocation(TerraCompositio.MOD_ID,
            "textures/gui/flow_infuser_gui.png");

    public static final RecipeType<FlowInfusionRecipe> FLOW_INFUSION_RECIPE_RECIPE_TYPE=
            new RecipeType<>(UID, FlowInfusionRecipe.class);
    private final IDrawable background;
    private final IDrawable icon;

    public FlowInfusionCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE,0,0,176,85);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK,new ItemStack(ModBlocks.FLOW_INFUSER.get()));
    }

    @Override
    public @NotNull RecipeType<FlowInfusionRecipe> getRecipeType() {
        return FLOW_INFUSION_RECIPE_RECIPE_TYPE;
    }

    @Override
    public @NotNull Component getTitle() {
        return Component.translatable("block.terracompositio.flow_infuser");
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
    public void setRecipe(IRecipeLayoutBuilder iRecipeLayoutBuilder, FlowInfusionRecipe flowSaturationRecipe, @NotNull IFocusGroup iFocusGroup) {
        iRecipeLayoutBuilder.addSlot(RecipeIngredientRole.INPUT,41,16).addIngredients(flowSaturationRecipe.getIngredients().get(0));
        iRecipeLayoutBuilder.addSlot(RecipeIngredientRole.OUTPUT,121,48).addItemStack(flowSaturationRecipe.getResultItem(null));
    }

    @Override
    public void draw(FlowInfusionRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        String cfe = "CFE: "+ recipe.getCfe();
        String cfe_t = "CFE/t: "+ recipe.getCFETick();
        String duration = "Duration: "+ recipe.getTicks()+" t.";
        guiGraphics.drawString(Minecraft.getInstance().font,cfe,5,50, Color.WHITE.getRGB());
        guiGraphics.drawString(Minecraft.getInstance().font,cfe_t,5,60, Color.WHITE.getRGB());
        guiGraphics.drawString(Minecraft.getInstance().font,duration,5,70, Color.WHITE.getRGB());
    }

}
