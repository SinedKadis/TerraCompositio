package net.sinedkadis.terracompositio.util.behaviors.blockentity;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.Nullable;

public interface IBEItemBehaviour extends IBEBehaviour {
    IItemHandlerModifiable getItemHandler();

    boolean allowExtract(int pSlot, ItemStack pStack, @Nullable Direction pDirection, boolean manualExtraction);

    boolean allowInsert(int pSlot, ItemStack pStack, @Nullable Direction pDirection, boolean manualInsertion);
}
