package net.sinedkadis.terracompositio.api;

import net.minecraft.world.item.ItemStack;
import net.sinedkadis.terracompositio.item.custom.TechnetiumArmorItem;

/**
 * Implement that to your item to be able to accept {@link IECFStorageExtensionItem}.
 * Recommended to override {@link TechnetiumArmorItem#getCraftingRemainingItem(ItemStack)} to return last extension
 */
public interface IHaveExtensibleECFStorageItem {
    /**
     * Gets current extension.
     *
     * @param stack the stack
     * @return the current extension
     */
    IECFStorageExtensionItem getCurrentExtension(ItemStack stack);

    /**
     * Sets extension.
     *
     * @param stack         the stack
     * @param extensionItem the extension item
     */
    void setExtension(ItemStack stack, IECFStorageExtensionItem extensionItem);
}
