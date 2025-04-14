package net.sinedkadis.terracompositio.util;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import static net.sinedkadis.terracompositio.block.custom.FlowCedarCasingBlock.isPortAttached;

public class ModItemStackHandler extends ItemStackHandler {
    private final BlockEntity blockEntity;

    public ModItemStackHandler(int size, BlockEntity blockEntity) {
        super(size);
        this.blockEntity = blockEntity;
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
        return isPortAttached(blockEntity.getLevel(),blockEntity.getBlockState(),blockEntity.getBlockPos()) ? 1 : 64;
    }


    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (slot != 1)
            return super.insertItem(slot, stack, simulate);
        return stack;
    }

    public ItemStack forceInsertItem(int slot, @NotNull ItemStack stack, boolean simulate){
        return super.insertItem(slot,stack,simulate);
    }
}
