package net.sinedkadis.terracompositio.recipe;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.sinedkadis.terracompositio.api.ICFEStorageExtension;
import net.sinedkadis.terracompositio.api.IHaveExtensibleCFEStorage;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ArmorStorageUpgradeRecipe extends CustomRecipe {
    public static final NoOpRecipeSerializer<ArmorStorageUpgradeRecipe> SERIALIZER = new NoOpRecipeSerializer<>(ArmorStorageUpgradeRecipe::new);


    public ArmorStorageUpgradeRecipe(ResourceLocation id) {
        super(id, CraftingBookCategory.MISC);
    }

    @Override
    public boolean matches(CraftingContainer inv, Level world) {
        boolean foundWill = false;
        boolean foundItem = false;

        ICFEStorageExtension currentExtension = null;

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty()) {
                if (stack.getItem() instanceof IHaveExtensibleCFEStorage cont) {
                    currentExtension = cont.getCurrentExtension(stack);
                    if (foundWill) {
                        return false;
                    }
                    foundWill = true;
                }
            }
        }
        if (currentExtension == null) return false;

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.getItem() instanceof ICFEStorageExtension extension
                    && extension.maxStorage() > 0) {
                if (currentExtension.self().equals(extension.self())) {
                    return false;
                }
                if (foundItem) {
                    return false;
                }
                foundItem = true;
            }
        }

        return foundItem;
    }


    @Override
    public ItemStack assemble(CraftingContainer inv, RegistryAccess registries) {
        ItemStack item = ItemStack.EMPTY;
        ICFEStorageExtension extension = () -> 0;
        int emptySlot = -1;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty()) {
                if (stack.getItem() instanceof IHaveExtensibleCFEStorage && item.isEmpty()) {
                    item = stack;
                } else {
                    extension = ((ICFEStorageExtension) stack.getItem()); // we already verified this is a storage extension in matches()
                }
            } else {
                if (emptySlot != -1) {
                    continue;
                }
                emptySlot = i;
            }
        }

        IHaveExtensibleCFEStorage container = (IHaveExtensibleCFEStorage) item.getItem();

        ICFEStorageExtension currentExtension = container.getCurrentExtension(item);
        if (currentExtension.equals(extension) || extension.maxStorage() <= 0) {
            return ItemStack.EMPTY;
        }

        ItemStack copy = item.copy();
        container.setExtension(copy, extension);

        return copy;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width > 1 || height > 1;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }
}
