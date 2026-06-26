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
import net.sinedkadis.terracompositio.recipe.MatterInfusionRecipe;
import net.sinedkadis.terracompositio.registries.TCBlocks;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.awt.*;
import java.util.Objects;

@ParametersAreNonnullByDefault
public class MatterInfusionCategory implements IRecipeCategory<MatterInfusionRecipe> {
    public static final ResourceLocation UID = ResourceLocation.tryBuild(TerraCompositio.MOD_ID,"matter_infusion");
    public static final ResourceLocation TEXTURE = ResourceLocation.tryBuild(TerraCompositio.MOD_ID,
            "textures/gui/matter_infuser_gui.png");

    public static final RecipeType<MatterInfusionRecipe> MATTER_INFUSION_RECIPE_RECIPE_TYPE =
            new RecipeType<>(Objects.requireNonNull(UID), MatterInfusionRecipe.class);
    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawableAnimated animatedArrow;

    public MatterInfusionCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(Objects.requireNonNull(TEXTURE),0,0,176,99);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK,new ItemStack(TCBlocks.MATTER_INFUSER_UNIT.get()));
        IDrawableStatic staticArrow = helper.createDrawable(TEXTURE, 0, 100, 112, 79);
        this.animatedArrow = helper.createAnimatedDrawable(staticArrow, 50, IDrawableAnimated.StartDirection.TOP, true);

    }

    @Override
    public @NotNull RecipeType<MatterInfusionRecipe> getRecipeType() {
        return MATTER_INFUSION_RECIPE_RECIPE_TYPE;
    }

    @Override
    public @NotNull Component getTitle() {
        return Component.translatable("block.terracompositio.matter_infuser");
    }

    @Override
    public int getHeight() {
        return 150;
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
    public void setRecipe(IRecipeLayoutBuilder iRecipeLayoutBuilder, MatterInfusionRecipe matterSaturationRecipe, IFocusGroup iFocusGroup) {
        iRecipeLayoutBuilder.addSlot(RecipeIngredientRole.INPUT,20,10).addIngredients(matterSaturationRecipe.getIngredients().get(1));
        iRecipeLayoutBuilder.addSlot(RecipeIngredientRole.CATALYST,55,42).addIngredients(matterSaturationRecipe.getIngredients().get(0));
        iRecipeLayoutBuilder.addSlot(RecipeIngredientRole.OUTPUT,20,74).addItemStack(matterSaturationRecipe.getResultItem(null));
    }

    @Override
    public void draw(MatterInfusionRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        background.draw(guiGraphics);
        animatedArrow.draw(guiGraphics,40,16);
        String cfe = "CFE: " + recipe.getEcf();
        String cfe_t = "CFE/t: " + recipe.getECFTick();
        Component duration = Component.translatable("block.terracompositio.matter_infuser.duration",(float)(recipe.getTicks()/20));
        Component decayChance = Component.translatable("block.terracompositio.matter_infuser.decay_chance",recipe.getCatalystDecayRate());
        guiGraphics.drawString(Minecraft.getInstance().font,cfe,5,110, Color.WHITE.getRGB());
        guiGraphics.drawString(Minecraft.getInstance().font,cfe_t,5,120, Color.WHITE.getRGB());
        guiGraphics.drawString(Minecraft.getInstance().font,duration,5,130, Color.WHITE.getRGB());
        guiGraphics.drawString(Minecraft.getInstance().font,decayChance,5,140, Color.WHITE.getRGB());
    }

}
