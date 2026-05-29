package net.sinedkadis.terracompositio.block.entity;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.sinedkadis.terracompositio.api.TCCapabilities;
import net.sinedkadis.terracompositio.api.behaviors.blockentity.IBEItemBehaviour;
import net.sinedkadis.terracompositio.api.dummies.DummyBehaviour;
import net.sinedkadis.terracompositio.api.dummies.DummyCFEHandler;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class TCCraftingBlockEntity extends TCBlockEntity implements WorldlyContainer {
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

    abstract protected IItemHandlerModifiable getItemHandler();

    public ItemStack getRenderStack() {
        for (int i = getItemHandler().getSlots() - 1; i >= 0; i--) {
            if (!getItemHandler().getStackInSlot(i).isEmpty()) {
                return getItemHandler().getStackInSlot(i);
            }
        }
        return ItemStack.EMPTY;
    }


    @Override
    public int[] getSlotsForFace(Direction pSide) {
        return getBehaviour().getSlotsForFace(pSide);
    }

    public IBEItemBehaviour getBehaviour() {
        IBEItemBehaviour itemBehaviour = getItemBehaviour();
        if (itemBehaviour == null) return DummyBehaviour.instance;
        return itemBehaviour;
    }

    @Override
    public boolean canPlaceItemThroughFace(int pIndex, ItemStack pItemStack, @Nullable Direction pDirection) {
        return getBehaviour().canPlaceItemThroughFace(pIndex,pItemStack,pDirection);
    }

    @Override
    public boolean canTakeItemThroughFace(int pIndex, ItemStack pStack, Direction pDirection) {
        return getBehaviour().canTakeItemThroughFace(pIndex,pStack,pDirection);
    }

    @Override
    public int getContainerSize() {
        return getBehaviour().getContainerSize();
    }

    @Override
    public boolean isEmpty() {
        return getBehaviour().isEmpty();
    }

    @Override
    public ItemStack getItem(int pSlot) {
        return getBehaviour().getItem(pSlot);
    }

    @Override
    public ItemStack removeItem(int pSlot, int pAmount) {
        return getBehaviour().removeItem(pSlot,pAmount);
    }

    @Override
    public ItemStack removeItemNoUpdate(int pSlot) {
        return getBehaviour().removeItemNoUpdate(pSlot);
    }

    @Override
    public void setItem(int pSlot, ItemStack pStack) {
        getBehaviour().setItem(pSlot,pStack);
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return getBehaviour().stillValid(pPlayer);
    }

    @Override
    public void clearContent() {
        getBehaviour().clearContent();
    }
}
