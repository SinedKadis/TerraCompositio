package net.sinedkadis.terracompositio.api.behaviors.blockentity;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public interface IBEItemBehaviour extends IBEBehaviour {
    ItemStackHandler getItemHandler();

    boolean allowExtract(int pSlot, ItemStack pStack, @Nullable Direction pDirection);

    boolean allowInsert(int pSlot, ItemStack pStack, @Nullable Direction pDirection);
}
