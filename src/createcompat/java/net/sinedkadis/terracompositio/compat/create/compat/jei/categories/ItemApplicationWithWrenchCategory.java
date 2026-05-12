package net.sinedkadis.terracompositio.compat.create.compat.jei.categories;

import com.simibubi.create.compat.jei.category.ItemApplicationCategory;
import com.simibubi.create.content.kinetics.deployer.ItemApplicationRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.foundation.utility.CreateLang;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.crafting.Ingredient;
import net.sinedkadis.terracompositio.compat.create.recipes.ManualApplicationWIthToolRecipe;
import net.sinedkadis.terracompositio.registries.TCTags;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
public class ItemApplicationWithWrenchCategory extends ItemApplicationCategory {
    public ItemApplicationWithWrenchCategory(Info<ItemApplicationRecipe> info) {
        super(info);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ItemApplicationRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 27, 38)
                .setBackground(getRenderedSlot(), -1, -1)
                .addIngredients(recipe.getProcessedItem());

        builder.addSlot(RecipeIngredientRole.INPUT, 51, 5)
                .setBackground(getRenderedSlot(), -1, -1)
                .addIngredients(recipe.getRequiredHeldItem())
                .addRichTooltipCallback(
                        recipe.shouldKeepHeldItem()
                                ? (view, tooltip) -> tooltip.add(CreateLang.translateDirect("recipe.deploying.not_consumed")
                                                                    .withStyle(ChatFormatting.GOLD))
                                : (view, tooltip) -> {
                        }
                );
        Ingredient tool;
        if (recipe instanceof ManualApplicationWIthToolRecipe toolRecipe) {
            tool = toolRecipe.getToolHeldItem();
        } else {
            tool = Ingredient.of(TCTags.Items.WRENCHES);
        }
        builder.addSlot(RecipeIngredientRole.INPUT, 31, 5)
                .setBackground(getRenderedSlot(), -1, -1)
                .addIngredients(tool)
                .addRichTooltipCallback(
                        (view, tooltip) -> tooltip.add(CreateLang.translateDirect("recipe.deploying.not_consumed")
                                .withStyle(ChatFormatting.GOLD))
                );

        List<ProcessingOutput> results = recipe.getRollableResults();
        boolean single = results.size() == 1;
        for (int i = 0; i < results.size(); i++) {
            ProcessingOutput output = results.get(i);
            int xOffset = i % 2 == 0 ? 0 : 19;
            int yOffset = (i / 2) * -19;
            builder.addSlot(RecipeIngredientRole.OUTPUT, single ? 132 : 132 + xOffset, 38 + yOffset)
                    .setBackground(getRenderedSlot(output), -1, -1)
                    .addItemStack(output.getStack())
                    .addRichTooltipCallback(addStochasticTooltip(output));
        }
    }

}
