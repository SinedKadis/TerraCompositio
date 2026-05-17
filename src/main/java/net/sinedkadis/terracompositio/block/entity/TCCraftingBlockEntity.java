package net.sinedkadis.terracompositio.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;
import net.sinedkadis.terracompositio.api.TCCapabilities;
import net.sinedkadis.terracompositio.api.dummies.DummyCFEHandler;

import java.util.Optional;

public abstract class TCCraftingBlockEntity extends TCBlockEntity{
    protected int progress = 0;
    protected int maxProgress;
    protected float tickCFECost;

    public TCCraftingBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    protected void resetProgress() {
        progress = 0;
    }

    protected boolean hasProgressFinished() {
        return progress>=maxProgress;
    }

    private float partialCFE = 0;
    protected void consumeCFE() {
        int floorCFE = (int) Math.floor(tickCFECost);
        partialCFE += tickCFECost- floorCFE;
        int floorPart = (int) Math.floor(partialCFE);
        partialCFE = partialCFE - floorPart;
        this.getCapability(TCCapabilities.CFE).orElse(DummyCFEHandler.instance).takeCFE(floorCFE+floorPart,false);
    }

    protected void increaseCraftingProgress() {
        progress++;
    }


    protected boolean sameItemInOutput(Item item) {
        return this.getItemHandler().getStackInSlot(getOutputSlotIndex()).isEmpty() || this.getItemHandler().getStackInSlot(getOutputSlotIndex()).is(item);
    }

    public int getOutputSlotIndex() {
        return getItemHandler().getSlots() - 1;
    }

    protected boolean enoughSpaceInOutput(int count) {
        return this.getItemHandler().getStackInSlot(getOutputSlotIndex()).getCount() + count
                <= Math.min(this.getItemHandler().getStackInSlot(getOutputSlotIndex()).getMaxStackSize(),
                getItemHandler().getSlotLimit(1));
    }

    protected ItemStack craftItem(){return ItemStack.EMPTY;}
    protected Optional<?> getCurrentRecipe(){return Optional.empty();}
    protected boolean hasRecipe(){return false;}

    abstract protected ItemStackHandler getItemHandler();

    public ItemStack getRenderStack() {
        for (int i = getItemHandler().getSlots() - 1; i >= 0; i--) {
            if (!getItemHandler().getStackInSlot(i).isEmpty()) {
                return getItemHandler().getStackInSlot(i);
            }
        }
        return ItemStack.EMPTY;
    }
}
