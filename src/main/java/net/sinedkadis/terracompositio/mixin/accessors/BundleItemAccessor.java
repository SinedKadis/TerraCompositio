package net.sinedkadis.terracompositio.mixin.accessors;

import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BundleItem.class)
public interface BundleItemAccessor {
    @Invoker("add")
    static int invokeAdd(ItemStack bundle, ItemStack stackToAdd) {
        throw new AssertionError("Mixin invoker failed to apply");
    }
}
