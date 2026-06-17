package net.sinedkadis.terracompositio.api;

import net.minecraft.world.item.ItemStack;

public interface IHaveExtensibleCFEStorage {
    ICFEStorageExtension getCurrentExtension(ItemStack stack);

    void setExtension(ItemStack stack, ICFEStorageExtension extensionItem);
}
