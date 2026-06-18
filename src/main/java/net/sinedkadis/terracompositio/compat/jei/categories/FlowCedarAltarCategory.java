package net.sinedkadis.terracompositio.compat.jei.categories;

import com.mojang.blaze3d.vertex.PoseStack;
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
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.recipe.AltarTransformationRecipe;
import net.sinedkadis.terracompositio.registries.TCBlocks;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

@SuppressWarnings("DataFlowIssue")
@ParametersAreNonnullByDefault
public class FlowCedarAltarCategory implements IRecipeCategory<AltarTransformationRecipe> {
    public static final ResourceLocation UID = ResourceLocation.tryBuild(TerraCompositio.MOD_ID,"flow_saturation");
    public static final ResourceLocation TEXTURE_1 = ResourceLocation.tryBuild(TerraCompositio.MOD_ID,
            "textures/gui/flow_cedar_altar_gui_1.png");
    public static final ResourceLocation TEXTURE_2 = ResourceLocation.tryBuild(TerraCompositio.MOD_ID,
            "textures/gui/flow_cedar_altar_gui_2.png");

    public static final RecipeType<AltarTransformationRecipe> FLOW_CEDAR_ALTAR_RECIPE_RECIPE_TYPE =
            new RecipeType<>(Objects.requireNonNull(UID), AltarTransformationRecipe.class);
    private final IDrawable background;
    private final IDrawable secondSlot;
    private final IDrawable icon;
    private final IDrawableAnimated animatedArrow;

    public FlowCedarAltarCategory(IGuiHelper helper) {

        this.background = helper.createDrawable(Objects.requireNonNull(TEXTURE_1), 0, 0, 176, 85);
        this.secondSlot = helper.createDrawable(Objects.requireNonNull(TEXTURE_2), 0, 0, 176, 85);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(TCBlocks.FLOW_CEDAR_ALTAR.get()));
        IDrawableStatic staticArrow = helper.createDrawable(TEXTURE_1, 0, 85, 104, 96 - 84);
        this.animatedArrow = helper.createAnimatedDrawable(staticArrow, 50, IDrawableAnimated.StartDirection.LEFT, true);

    }

    @Override
    public @NotNull RecipeType<AltarTransformationRecipe> getRecipeType() {
        return FLOW_CEDAR_ALTAR_RECIPE_RECIPE_TYPE;
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
        NonNullList<Ingredient> ingredients = altarTransformationRecipe.getIngredients();
        iRecipeLayoutBuilder.addSlot(RecipeIngredientRole.INPUT, 41, 14).addIngredients(ingredients.get(0));
        if (ingredients.size() == 2)
            iRecipeLayoutBuilder.addSlot(RecipeIngredientRole.INPUT, 20, 14).addIngredients(ingredients.get(1));
        iRecipeLayoutBuilder.addSlot(RecipeIngredientRole.OUTPUT, 121, 14).addItemStack(altarTransformationRecipe.getResultItem(null));
    }

    @Override
    public void draw(AltarTransformationRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {


        NonNullList<Ingredient> ingredients = recipe.getIngredients();

        if (ingredients.size() == 2) {
            secondSlot.draw(guiGraphics);
        } else {
            background.draw(guiGraphics);
        }
        animatedArrow.draw(guiGraphics,47,34);

        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        poseStack.scale(3, 3, 3);
        poseStack.translate(-58, -53, 0);
        poseStack.translate(0, 0, -100);
        guiGraphics.renderFakeItem(TCBlocks.FLOW_CEDAR_PEDESTAL.get().asItem().getDefaultInstance(), 80, 68);
        poseStack.translate(0, 0, 50);
        guiGraphics.renderFakeItem(TCBlocks.FLOW_CEDAR_ALTAR.get().asItem().getDefaultInstance(), 80, 60);
        poseStack.popPose();
    }
}
