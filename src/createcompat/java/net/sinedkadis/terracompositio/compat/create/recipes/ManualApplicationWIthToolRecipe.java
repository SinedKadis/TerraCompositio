package net.sinedkadis.terracompositio.compat.create.recipes;

import com.simibubi.create.content.kinetics.deployer.ManualApplicationRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import net.minecraft.world.item.crafting.Ingredient;

public class ManualApplicationWIthToolRecipe extends ManualApplicationRecipe {
    public ManualApplicationWIthToolRecipe(ProcessingRecipeBuilder.ProcessingRecipeParams params) {
        super(params);
    }

    public Ingredient getToolHeldItem() {
        if (ingredients.size() < 3)
            throw new IllegalStateException("Item Application Recipe: " + id.toString() + " has no offhand tool!");
        return ingredients.get(2);
    }
}
