package net.sinedkadis.terracompositio.api;


import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Implement that interface to make your item something like battery for {@link IHaveExtensibleECFStorageItem}, like my Technetium Armor.
 */
public interface IECFStorageExtensionItem {
    /**
     * new instance of itself.
     *
     * @return the item stack
     */
    default ItemStack self() {
        return this instanceof Item item ? item.getDefaultInstance() : ItemStack.EMPTY;
    }

    /**
     * Max ECF storage Overrides max ECF value on applying to item, last {@link IECFStorageExtensionItem} returns in crafting slot after applying .
     *
     * @return the int
     */
    int maxStorage();
}
