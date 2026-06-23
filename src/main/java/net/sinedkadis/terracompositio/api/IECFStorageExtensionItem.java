package net.sinedkadis.terracompositio.api;


import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public interface IECFStorageExtensionItem {
    default ItemStack self() {
        return this instanceof Item item ? item.getDefaultInstance() : ItemStack.EMPTY;
    }

    int maxStorage();
}
