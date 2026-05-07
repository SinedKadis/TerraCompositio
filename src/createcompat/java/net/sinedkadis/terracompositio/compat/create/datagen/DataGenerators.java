package net.sinedkadis.terracompositio.compat.create.datagen;


import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.tags.FluidTagsProvider;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.sinedkadis.terracompositio.compat.soft_compat.ISoftDataGen;
import net.sinedkadis.terracompositio.compat.create.datagen.loot.CreateCompatBlockLootTables;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.function.Consumer;


public class DataGenerators implements ISoftDataGen {

    @Override
    public void buildRecipes(RecipeProvider instance, @NotNull Consumer<FinishedRecipe> pWriter) {
        CreateCompatRecipeProvider.buildRecipes(instance, pWriter);
    }

    @Override
    public void addItemTags(ItemTagsProvider instance, HolderLookup.@NotNull Provider pProvider) {
        CreateCompatItemTagGenerator.addTags(instance, pProvider);
    }

    @Override
    public void addBlockTags(BlockTagsProvider instance, HolderLookup.@NotNull Provider pProvider) {
        CreateCompatBlockTagGenerator.addTags(instance, pProvider);
    }

    @Override
    public void addFluidTags(FluidTagsProvider instance, HolderLookup.@NotNull Provider pProvider) {
        CreateCompatFluidTagGenerator.addTags(instance, pProvider);
    }

    @Override
    public void registerItemModels() {
        CreateCompatItemModelProvider.registerModels();
    }

    @Override
    public void registerBlockStatesAndModels() {
        CreateCompatBlockStateProvider.registerStatesAndModels();
    }

    @Override
    public void dropSelf(Set<Block> blocks) {
        CreateCompatBlockLootTables.dropSelf(blocks);
    }
}
