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
        ItemStack itemInSlot = this.getItemInSlot(FlowCedarCasingBlockEntity.INPUT_INVENTORY_SLOT);
        if (itemInSlot != null)
            return itemInSlot;
        return ItemStack.EMPTY;
    }
    public ItemStack getSlotOutput() {
        ItemStack itemInSlot = this.getItemInSlot(FlowCedarCasingBlockEntity.OUTPUT_INVENTORY_SLOT);
        if (itemInSlot != null)
            return itemInSlot;
        return ItemStack.EMPTY;
    }

    public ItemStack getItemInSlot(int slot) {
        FlowCedarCasingBlockEntity blockEntity = this.getCasingBE();
        if (blockEntity != null){
            return blockEntity.getItemHandler().getStackInSlot(slot);
        }
        return ItemStack.EMPTY;
    }

    public void extractItemStack(int slot, int count) {
        FlowCedarCasingBlockEntity blockEntity = this.getCasingBE();
        if (blockEntity != null){
            blockEntity.getItemHandler().extractItem(slot, count, false);
        }
    }

    public @Nullable FlowCedarCasingBlockEntity getCasingBE() {
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
