package net.sinedkadis.terracompositio.util;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;
import net.sinedkadis.terracompositio.block.entity.TCItemIOCFEBlockEntity;
import org.jetbrains.annotations.NotNull;

public class TCItemStackHandler extends ItemStackHandler {
    private final BlockEntity blockEntity;

    public TCItemStackHandler(int size, BlockEntity blockEntity) {
        super(size);
        this.blockEntity = blockEntity;
    }

    public TCItemStackHandler(BlockEntity blockEntity) {
        this(1,blockEntity);
    }

    @Override
    protected void onContentsChanged(int slot) {
        blockEntity.setChanged();
        Level level = blockEntity.getLevel();
        if (level != null && !level.isClientSide()) {
            BlockState blockState = blockEntity.getBlockState();
            level.sendBlockUpdated(blockEntity.getBlockPos(), blockState, blockState, 3);
        }
    }

    @Override
    public int getSlotLimit(int slot) {
        if (blockEntity instanceof TCItemIOCFEBlockEntity entity){
            return entity.getSlotLimit(slot);
        }
        return super.getSlotLimit(slot);
    }


    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (slot != 1)
            return super.insertItem(slot, stack, simulate);
        return stack;
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (slot != 0)
            return super.extractItem(slot, amount, simulate);
        return ItemStack.EMPTY;
    }

    public ItemStack forceInsertItem(int slot, @NotNull ItemStack stack, boolean simulate){
        return super.insertItem(slot,stack,simulate);
    }

    public ItemStack forceExtractItem(int slot, int amount, boolean simulate) {
        return super.extractItem(slot, amount, simulate);
    }
}
