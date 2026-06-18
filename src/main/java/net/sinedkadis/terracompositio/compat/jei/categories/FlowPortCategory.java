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
import net.sinedkadis.terracompositio.recipe.AltarTransformationRecipe;
import net.sinedkadis.terracompositio.registries.TCBlocks;
import net.sinedkadis.terracompositio.registries.TCItems;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.awt.*;
import java.util.Objects;

@SuppressWarnings("DataFlowIssue")
@ParametersAreNonnullByDefault
public class FlowPortCategory implements IRecipeCategory<AltarTransformationRecipe> {
    public static final ResourceLocation UID = ResourceLocation.tryBuild(TerraCompositio.MOD_ID,"flow_saturation");
    public static final ResourceLocation TEXTURE = ResourceLocation.tryBuild(TerraCompositio.MOD_ID,
            "textures/gui/flow_port_gui.png");

    public static final RecipeType<AltarTransformationRecipe> FLOW_SATURATION_RECIPE_RECIPE_TYPE =
            new RecipeType<>(Objects.requireNonNull(UID), AltarTransformationRecipe.class);
    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawableAnimated animatedArrow;

    public FlowPortCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(Objects.requireNonNull(TEXTURE),0,0,176,85);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(TCBlocks.FLOW_CEDAR_ALTAR.get()));
        IDrawableStatic staticArrow = helper.createDrawable(TEXTURE, 0, 85, 104, 96-84);
        this.animatedArrow = helper.createAnimatedDrawable(staticArrow, 50, IDrawableAnimated.StartDirection.LEFT, true);

    }

    @Override
    public @NotNull RecipeType<AltarTransformationRecipe> getRecipeType() {
        return FLOW_SATURATION_RECIPE_RECIPE_TYPE;
    }

    @Override
    public @NotNull Component getTitle() {
        return Component.translatable("block.terracompositio.flow_cedar_altar");
    }

    @Override
    public int getHeight() {
        return 85;
    }

    @Override
    public int getWidth() {
        return 176;
    }

    @Override
    public @NotNull IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder iRecipeLayoutBuilder, AltarTransformationRecipe altarTransformationRecipe, IFocusGroup iFocusGroup) {
        iRecipeLayoutBuilder.addSlot(RecipeIngredientRole.INPUT, 41, 16).addIngredients(altarTransformationRecipe.getIngredients().get(0));
        iRecipeLayoutBuilder.addSlot(RecipeIngredientRole.OUTPUT, 121, 48).addItemStack(altarTransformationRecipe.getResultItem(null));
    }

    @Override
    public void draw(AltarTransformationRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        background.draw(guiGraphics);
        animatedArrow.draw(guiGraphics,47,34);
        if (recipe.getIngredients().get(0).getItems()[0].is(TCItems.CREATION_FLOW_JOURNAL.get()))
            guiGraphics.drawString(Minecraft.getInstance().font, Component.translatable("block.terracompositio.flow_cedar_port_wait"), 5, 75, Color.WHITE.getRGB());
    }
}
