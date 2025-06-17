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
import net.sinedkadis.terracompositio.registries.TCBlocks;
import net.sinedkadis.terracompositio.recipe.FlowInfusionRecipe;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.awt.*;
import java.util.Objects;

@ParametersAreNonnullByDefault
public class FlowInfusionCategory implements IRecipeCategory<FlowInfusionRecipe> {
    public static final ResourceLocation UID = ResourceLocation.tryBuild(TerraCompositio.MOD_ID,"flow_infusion");
    public static final ResourceLocation TEXTURE = ResourceLocation.tryBuild(TerraCompositio.MOD_ID,
            "textures/gui/flow_infuser_gui.png");

    public static final RecipeType<FlowInfusionRecipe> FLOW_INFUSION_RECIPE_RECIPE_TYPE=
            new RecipeType<>(Objects.requireNonNull(UID), FlowInfusionRecipe.class);
    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawableAnimated animatedArrow;

    public FlowInfusionCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(Objects.requireNonNull(TEXTURE),0,0,176,85);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK,new ItemStack(TCBlocks.FLOW_INFUSER.get()));
        IDrawableStatic staticArrow = helper.createDrawable(TEXTURE, 0, 86, 94, 96-84);
        this.animatedArrow = helper.createAnimatedDrawable(staticArrow, 60, IDrawableAnimated.StartDirection.LEFT, true);

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
    public void setRecipe(IRecipeLayoutBuilder iRecipeLayoutBuilder, FlowInfusionRecipe flowSaturationRecipe, @NotNull IFocusGroup iFocusGroup) {
        iRecipeLayoutBuilder.addSlot(RecipeIngredientRole.INPUT,41,16).addIngredients(flowSaturationRecipe.getIngredients().get(0));
        iRecipeLayoutBuilder.addSlot(RecipeIngredientRole.OUTPUT,121,48).addItemStack(flowSaturationRecipe.getResultItem(null));
    }

    @Override
    public void draw(FlowInfusionRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        background.draw(guiGraphics);
        animatedArrow.draw(guiGraphics,47,35);
        String cfe = "CFE: "+ recipe.getCfe();
        String cfe_t = "CFE/t: "+ recipe.getCFETick();
        String duration = "Duration: "+ recipe.getTicks()+" t.";
        guiGraphics.drawString(Minecraft.getInstance().font,cfe,5,50, Color.WHITE.getRGB());
        guiGraphics.drawString(Minecraft.getInstance().font,cfe_t,5,60, Color.WHITE.getRGB());
        guiGraphics.drawString(Minecraft.getInstance().font,duration,5,70, Color.WHITE.getRGB());
    }

}
