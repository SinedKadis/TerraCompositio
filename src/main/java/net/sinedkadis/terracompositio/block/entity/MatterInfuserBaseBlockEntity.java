package net.sinedkadis.terracompositio.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.sinedkadis.terracompositio.registries.ModBlocks;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;

public abstract class MatterInfuserBaseBlockEntity extends ModCFEBlockEntity{
    public MatterInfuserBaseBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int maxCFE, int connectRange) {
        super(type, pos, state, maxCFE, connectRange);
    }

    public ItemStack getInputSlot() {
        Direction direction = this.getBlockState().getValue(HORIZONTAL_FACING);
        BlockPos blockpos = this.getBlockPos().relative(direction.getOpposite());
        if (this.level != null && this.level.getBlockState(blockpos).is(ModBlocks.FLOW_CEDAR_CASING.get())){
            BlockEntity blockEntity = this.level.getBlockEntity(blockpos);
            if (blockEntity != null){
                ItemStack inputSlot = ((FlowCedarCasingBlockEntity) blockEntity).getFirstSlot();
                if (this instanceof MatterInfuserPortBlockEntity)
                    return inputSlot;
                return inputSlot;
            }
        }
        return ItemStack.EMPTY;
    }

    public ItemStack insertItemStack(int slot, ItemStack itemstack) {
        Direction direction = this.getBlockState().getValue(HORIZONTAL_FACING);
        BlockPos blockpos = this.getBlockPos().relative(direction.getOpposite());
        if (this.level != null && this.level.getBlockState(blockpos)
                .is(ModBlocks.FLOW_CEDAR_CASING.get())){
            BlockEntity blockEntity = this.level.getBlockEntity(blockpos);
            if (blockEntity != null){
                return ((FlowCedarCasingBlockEntity) blockEntity).insertItemStack(slot,itemstack);
            }
        }
        return itemstack;
    }

    public void setSlotEmpty(int slot) {
        Direction direction = this.getBlockState().getValue(HORIZONTAL_FACING);
        BlockPos blockpos = this.getBlockPos().relative(direction.getOpposite());
        if (this.level != null && this.level.getBlockState(blockpos).is(ModBlocks.FLOW_CEDAR_CASING.get())){
            BlockEntity blockEntity = this.level.getBlockEntity(blockpos);
            if (blockEntity != null){
                ((FlowCedarCasingBlockEntity) blockEntity).setSlotEmpty(slot);
            }
        }
    }
}
