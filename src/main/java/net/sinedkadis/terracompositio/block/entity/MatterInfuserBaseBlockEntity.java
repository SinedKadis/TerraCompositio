package net.sinedkadis.terracompositio.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.sinedkadis.terracompositio.registries.ModBlocks;
import org.jetbrains.annotations.Nullable;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;

public abstract class MatterInfuserBaseBlockEntity extends ModCFEBlockEntity{
    public MatterInfuserBaseBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int maxCFE, int connectRange) {
        super(type, pos, state, maxCFE, connectRange,BlockMode.CONSUMER);
    }

    public MatterInfuserBaseBlockEntity(BlockEntityType<?> type, BlockPos pPos, BlockState pBlockState) {
        super(type,pPos,pBlockState,BlockMode.CONSUMER);
    }

    public ItemStack getInputSlot() {
        ItemStack itemInSlot = this.getItemInSlot(0);
        if (itemInSlot != null)
            return itemInSlot;
        return ItemStack.EMPTY;
    }
    public ItemStack getOutputSlot() {
        return this.getItemInSlot(this.getSlotCount()-1);
    }

    public ItemStack getItemInSlot(int slot) {
        FlowCedarCasingBlockEntity blockEntity = this.getCasingBE();
        if (blockEntity != null){
            return blockEntity.itemHandler.getStackInSlot(slot);
        }
        return ItemStack.EMPTY;
    }

    public ItemStack insertItemStack(int slot, ItemStack itemstack) {
        FlowCedarCasingBlockEntity blockEntity = this.getCasingBE();
        if (blockEntity != null){
            return blockEntity.insertItemStack(slot,itemstack);
        }
        return itemstack;
    }
    public ItemStack forceInsertItemStack(int slot, ItemStack itemstack) {
        FlowCedarCasingBlockEntity blockEntity = this.getCasingBE();
        if (blockEntity != null){
            return blockEntity.forceInsertItemStack(slot,itemstack);
        }
        return itemstack;
    }
    public ItemStack extractItemStack(int slot, int count) {
        ItemStack itemStack;
        FlowCedarCasingBlockEntity blockEntity = this.getCasingBE();
        if (blockEntity != null){
            itemStack = blockEntity.itemHandler.extractItem(slot,count,false);
            return itemStack;
        }
        return ItemStack.EMPTY;
    }

    public void setSlotEmpty(int slot) {
        FlowCedarCasingBlockEntity casingBE = this.getCasingBE();
        if (casingBE != null) {
            casingBE.setSlotEmpty(slot);
        }
    }

    public int getSlotCount(){
        FlowCedarCasingBlockEntity casingBE = this.getCasingBE();
        if (casingBE != null) {
            return casingBE.itemHandler.getSlots();
        }
        return 0;
    }

    protected @Nullable FlowCedarCasingBlockEntity getCasingBE() {
        Direction direction = this.getBlockState().getValue(HORIZONTAL_FACING);
        BlockPos blockpos = this.getBlockPos().relative(direction.getOpposite());
        if (this.level != null && this.level.getBlockState(blockpos).is(ModBlocks.FLOW_CEDAR_CASING.get())){
            BlockEntity blockEntity = this.level.getBlockEntity(blockpos);
            if (blockEntity != null){
                return ((FlowCedarCasingBlockEntity) blockEntity);
            }
        }
        return null;
    }
}
