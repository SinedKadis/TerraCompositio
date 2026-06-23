package net.sinedkadis.terracompositio.api;

import net.minecraft.world.item.ItemStack;

public interface IHaveExtensibleECFStorageItem {
    IECFStorageExtensionItem getCurrentExtension(ItemStack stack);

    void setExtension(ItemStack stack, IECFStorageExtensionItem extensionItem);
}
