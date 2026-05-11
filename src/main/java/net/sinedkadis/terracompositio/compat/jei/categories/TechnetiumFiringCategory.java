package net.sinedkadis.terracompositio.compat.jei.categories;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
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
import net.sinedkadis.terracompositio.recipe.TechnetiumFiringRecipe;
import net.sinedkadis.terracompositio.registries.TCItems;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.awt.*;
import java.util.Objects;

public class TechnetiumFiringCategory implements IRecipeCategory<TechnetiumFiringRecipe> {
    public static final ResourceLocation UID = ResourceLocation.tryBuild(TerraCompositio.MOD_ID,"technetium_firing");
    public static final ResourceLocation TEXTURE = ResourceLocation.tryBuild(TerraCompositio.MOD_ID,
            "textures/gui/technetium_firing_gui.png");

    public static final RecipeType<TechnetiumFiringRecipe> TECHNETIUM_FIRING_RECIPE_RECIPE_TYPE =
            new RecipeType<>(Objects.requireNonNull(UID), TechnetiumFiringRecipe.class);
    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawableAnimated animatedFlame;
    private final IDrawableAnimated animatedEmittedLeft;
    private final IDrawableAnimated animatedEmittedRight;

    public TechnetiumFiringCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(Objects.requireNonNull(TEXTURE),0,0,82,61);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK,new ItemStack(TCItems.TECHNETIUM_INGOT.get()));
        IDrawableStatic staticFlame = helper.createDrawable(TEXTURE, 82, 0, 14, 14);
        IDrawableStatic staticEmittedLeft = helper.createDrawable(TEXTURE,96,0,41,61);
        IDrawableStatic staticEmittedRight = helper.createDrawable(TEXTURE,137,0,41,61);
        this.animatedFlame = helper.createAnimatedDrawable(staticFlame, 300, IDrawableAnimated.StartDirection.TOP, true);
        this.animatedEmittedLeft = helper.createAnimatedDrawable(staticEmittedLeft, 60, IDrawableAnimated.StartDirection.RIGHT, true);
        this.animatedEmittedRight = helper.createAnimatedDrawable(staticEmittedRight, 60, IDrawableAnimated.StartDirection.LEFT, true);
    }

    @Override
    public @NotNull RecipeType<TechnetiumFiringRecipe> getRecipeType() {
        return TECHNETIUM_FIRING_RECIPE_RECIPE_TYPE;
    }

    @Override
    public @NotNull Component getTitle() {
        return Component.translatable("block.terracompositio.technetium_firing");
    }

    @Override
    public int getHeight() {
        return 61;
    }

    @Override
    public int getWidth() {
        return 82;
    }

    @Override
    public @NotNull IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder iRecipeLayoutBuilder, TechnetiumFiringRecipe flowSaturationRecipe, @NotNull IFocusGroup iFocusGroup) {
        iRecipeLayoutBuilder.addSlot(RecipeIngredientRole.INPUT,33,13).addIngredients(flowSaturationRecipe.getIngredients().get(0));
    }

    @Override
    @ParametersAreNonnullByDefault
    public void draw(TechnetiumFiringRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        background.draw(guiGraphics);
        this.animatedEmittedLeft.draw(guiGraphics,0,0);
        this.animatedEmittedRight.draw(guiGraphics,41,0);
        this.animatedFlame.draw(guiGraphics, 33, 33);
        String cfe = "CFE: "+ recipe.getCfe();
        //guiGraphics.drawString(Minecraft.getInstance().font,"Generation",5,40, Color.WHITE.getRGB());
        guiGraphics.drawString(Minecraft.getInstance().font,cfe,5,50, Color.WHITE.getRGB());

    }

}
