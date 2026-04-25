package net.sinedkadis.terracompositio.compat.create.datagen;


import com.simibubi.create.AllBlocks;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.compat.create.TCCreateCompat;
import net.sinedkadis.terracompositio.compat.create.registries.CreateBlocks;
import net.sinedkadis.terracompositio.registries.TCBlocks;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class CreateCompatRecipeProvider extends RecipeProvider {

    public CreateCompatRecipeProvider(PackOutput pOutput) {
        super(pOutput);
    }

    public static void buildRecipes(RecipeProvider ignoredInstance, @NotNull Consumer<FinishedRecipe> pWriter) {
        CreateBlocks createBlocks = ((TCCreateCompat) TerraCompositio.createCompat).blocks;
        if (createBlocks.CEDAR_GEARBOX == null) return;
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, createBlocks.CEDAR_GEARBOX.get())
                .pattern("S")
                .pattern("L")
                .pattern("S")
                .define('L', TCBlocks.FLOW_CEDAR_LOG.get())
                .define('S', AllBlocks.SHAFT)
                .unlockedBy(getHasName(TCBlocks.FLOW_CEDAR_LOG.get()), has(TCBlocks.FLOW_CEDAR_LOG.get()))
                .save(pWriter);


    }

    @Override
    protected void buildRecipes(@NotNull Consumer<FinishedRecipe> pWriter) {

    }
}
