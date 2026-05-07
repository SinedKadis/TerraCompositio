package net.sinedkadis.terracompositio.compat.create.recipes;

import com.google.common.collect.Lists;
import com.simibubi.create.content.kinetics.deployer.ManualApplicationRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.registries.TCBlocks;
import net.sinedkadis.terracompositio.registries.TCItems;
import net.sinedkadis.terracompositio.registries.TCTags;

import java.util.ArrayList;
import java.util.List;

public class ManualApplicationFakeRecipes {

    public static List<ManualApplicationRecipe> createRecipes() {
        List<ManualApplicationRecipe> recipes = new ArrayList<>();

        //todo: make axe
        recipes.add(new ProcessingRecipeBuilder<>(ManualApplicationRecipe::new, TerraCompositio.modLoc("flow_infuser"))
                .require(TCBlocks.FLOW_CEDAR_LOG.get())
                .require(TCItems.FLOW_INFUSER_KIT.get())
                .output(TCBlocks.FLOW_INFUSER.get())
                .build());

        List<ItemStack> goldRods = Lists.newArrayList();


        //noinspection deprecation
        for(Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(TCTags.Items.GOLD_RODS)) {
            goldRods.add(new ItemStack(holder, 4));
        }

        recipes.add(new ProcessingRecipeBuilder<>(ManualApplicationRecipe::new, TerraCompositio.modLoc("cedar_casing"))
                .require(TCBlocks.FLOW_CEDAR_LOG.get())
                .require(Ingredient.of(goldRods.stream()))
                .output(TCBlocks.FLOW_CEDAR_CASING.get())
                .build());
        recipes.add(new ProcessingRecipeBuilder<>(ManualApplicationRecipe::new, TerraCompositio.modLoc("cedar_tank_1"))
                .require(TCBlocks.FLOW_CEDAR_CASING.get())
                .require(Ingredient.of(new ItemStack(TCItems.INFUSED_IRON_ROD.get(),8)))
                .output(TCBlocks.FLOW_CEDAR_TANK_3.get())
                .build());
        recipes.add(new ProcessingRecipeBuilder<>(ManualApplicationRecipe::new, TerraCompositio.modLoc("cedar_tank_2"))
                .require(TCBlocks.FLOW_CEDAR_TANK_2.get())
                .require(Blocks.GLASS)
                .output(TCBlocks.FLOW_CEDAR_TANK.get())
                .build());
        return recipes;
    }

}


