package net.sinedkadis.terracompositio.block.entity;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.sinedkadis.terracompositio.registries.ModBlockEntities;
import net.sinedkadis.terracompositio.registries.ModBlocks;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;

@Setter
@Getter
public class MatterInfuserPortBlockEntity extends ModCFEBlockEntity {

    private ItemStack clientItem = ItemStack.EMPTY;


    public MatterInfuserPortBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.MATTER_INFUSER_PORT_BE.get(),pPos, pBlockState,100,10);
    }

    public ItemStack getInputSlot() {
        Direction direction = this.getBlockState().getValue(HORIZONTAL_FACING);
        BlockPos blockpos = this.getBlockPos().relative(direction.getOpposite());
        if (this.level != null && this.level.getBlockState(blockpos).is(ModBlocks.FLOW_CEDAR_CASING.get())){
            BlockEntity blockEntity = this.level.getBlockEntity(blockpos);
            if (blockEntity != null){
                ItemStack inputSlot = ((FlowCedarCasingBlockEntity) blockEntity).getFirstSlot();
                return inputSlot.isEmpty() ? clientItem : inputSlot;
            }
        }
        return ItemStack.EMPTY;
    }


    @Override
    public void tick(Level pLevel, BlockPos pPos, BlockState pState) {
        super.tick(pLevel, pPos, pState);
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
