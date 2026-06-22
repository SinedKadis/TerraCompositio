package net.sinedkadis.terracompositio.compat.jei.extensions;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import net.minecraftforge.registries.ForgeRegistries;
import net.sinedkadis.terracompositio.api.ICFEStorageExtension;
import net.sinedkadis.terracompositio.api.IHaveExtensibleCFEStorage;
import net.sinedkadis.terracompositio.recipe.CFEStorageUpgradeRecipe;
import net.sinedkadis.terracompositio.registries.TCItems;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CFEStorageUpdateRecipeWrapper implements ICraftingCategoryExtension {
	private final ResourceLocation name;

	public CFEStorageUpdateRecipeWrapper(CFEStorageUpgradeRecipe recipe) {
		this.name = recipe.getId();
	}

	@Nullable
	@Override
	public ResourceLocation getRegistryName() {
		return name;
	}

	@Override
	public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull ICraftingGridHelper helper, @NotNull IFocusGroup focusGroup) {
		List<ItemStack> items = new ArrayList<>();
		List<ICFEStorageExtension> extensions = new ArrayList<>(focusGroup.getFocuses(VanillaTypes.ITEM_STACK, RecipeIngredientRole.INPUT)
                .map(focus -> focus.getTypedValue().getIngredient())
                .filter(itemStack -> itemStack.getItem() instanceof ICFEStorageExtension)
                .peek(items::add)
				.map(ItemStack::getItem)
                .map(ICFEStorageExtension.class::cast)
                .toList());

		if (extensions.isEmpty()) {
			extensions.addAll(ForgeRegistries.ITEMS.getValues().stream()
					.filter(ICFEStorageExtension.class::isInstance)
					.map(ItemStack::new)
					.peek(items::add)
					.map(ItemStack::getItem)
					.map(ICFEStorageExtension.class::cast)
					.toList()
			);
		}

		List<ItemStack> outputStacks = new ArrayList<>();
		for (var extension : extensions) {
			var stack = new ItemStack(TCItems.TECHNETIUM_LEGGINGS.get());
			((IHaveExtensibleCFEStorage) stack.getItem()).setExtension(stack, extension);
			outputStacks.add(stack);
		}

		helper.createAndSetInputs(builder, VanillaTypes.ITEM_STACK,
				List.of(Collections.singletonList(new ItemStack(TCItems.TECHNETIUM_LEGGINGS.get())), items), 0, 0);
		helper.createAndSetOutputs(builder, VanillaTypes.ITEM_STACK, outputStacks);
	}
}
