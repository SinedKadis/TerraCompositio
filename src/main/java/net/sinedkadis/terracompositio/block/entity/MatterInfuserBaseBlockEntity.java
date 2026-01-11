package net.sinedkadis.terracompositio.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.sinedkadis.terracompositio.registries.TCBlocks;
import org.jetbrains.annotations.Nullable;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;

public abstract class MatterInfuserBaseBlockEntity extends TCCraftingBlockEntity {
    public MatterInfuserBaseBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public ItemStack getInputSlot() {
        ItemStack itemInSlot = this.getItemInSlot(0);
        if (itemInSlot != null)
            return itemInSlot;
        return ItemStack.EMPTY;
    }
    public ItemStack getSlotOutput() {
        ItemStack itemInSlot = this.getItemInSlot(1);
        if (itemInSlot != null)
            return itemInSlot;
        return ItemStack.EMPTY;
    }

    public ItemStack getItemInSlot(int slot) {
        FlowCedarCasingBlockEntity blockEntity = this.getCasingBE();
        if (blockEntity != null){
            return blockEntity.itemHandler().getStackInSlot(slot);
        }
        return ItemStack.EMPTY;
    }

    public ItemStack insertItemStack(int slot, ItemStack itemstack) {
        FlowCedarCasingBlockEntity blockEntity = this.getCasingBE();
        if (blockEntity != null){
            return blockEntity.itemHandler().insertItem(slot,itemstack,false);
        }
        return itemstack;
    }

    public ItemStack extractItemStack(int slot, int count) {
        ItemStack itemStack;
        FlowCedarCasingBlockEntity blockEntity = this.getCasingBE();
        if (blockEntity != null){
            itemStack = blockEntity.itemHandler().extractItem(slot,count,false);
            return itemStack;
        }
        return ItemStack.EMPTY;
    }

    public void setSlotEmpty(int slot) {
        FlowCedarCasingBlockEntity casingBE = this.getCasingBE();
        if (casingBE != null) {
            casingBE.itemHandler().setStackInSlot(slot,ItemStack.EMPTY);
        }
    }

    public int getSlotCount(){
        FlowCedarCasingBlockEntity casingBE = this.getCasingBE();
        if (casingBE != null) {
            return casingBE.itemHandler().getSlots();
        }
        return 0;
    }

    protected @Nullable FlowCedarCasingBlockEntity getCasingBE() {
        Direction direction = this.getBlockState().getValue(HORIZONTAL_FACING);
        BlockPos blockpos = this.getBlockPos().relative(direction.getOpposite());
        if (this.level != null && this.level.getBlockState(blockpos).is(TCBlocks.FLOW_CEDAR_CASING.get())){
            BlockEntity blockEntity = this.level.getBlockEntity(blockpos);
            if (blockEntity != null){
                return ((FlowCedarCasingBlockEntity) blockEntity);
            }
        }
        return null;
    }
}
