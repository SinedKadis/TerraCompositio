package net.sinedkadis.terracompositio.compat;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.tags.FluidTagsProvider;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.BlockTagsProvider;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.function.Consumer;

public interface ISoftDataGen {
    void buildRecipes(RecipeProvider instance, @NotNull Consumer<FinishedRecipe> pWriter);

    void addItemTags(ItemTagsProvider instance, HolderLookup.@NotNull Provider pProvider);

    void addBlockTags(BlockTagsProvider instance, HolderLookup.@NotNull Provider pProvider);

    void addFluidTags(FluidTagsProvider instance, HolderLookup.@NotNull Provider pProvider);

    void registerItemModels();

    void registerBlockStatesAndModels();

    void dropSelf(Set<Block> blocks);
}
