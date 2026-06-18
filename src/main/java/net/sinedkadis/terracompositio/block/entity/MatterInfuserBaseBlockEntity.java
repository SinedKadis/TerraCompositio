package net.sinedkadis.terracompositio.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.sinedkadis.terracompositio.registries.TCBlocks;
import net.sinedkadis.terracompositio.util.helpers.ItemHelper;
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

    public void extractItemStackViaSetter(int slot, int count) {
        FlowCedarCasingBlockEntity blockEntity = this.getCasingBE();
        if (blockEntity != null){
            IItemHandlerModifiable itemHandler = blockEntity.getItemHandler();
            ItemStack copy = itemHandler.getStackInSlot(slot).copy();
            copy.shrink(count);
            itemHandler.setStackInSlot(slot, copy);
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

    @Override
    public void setRemoved() {
        Direction direction = getBlockState().getValue(HORIZONTAL_FACING);
        BlockPos blockPos2 = worldPosition.relative(direction.getCounterClockWise());
        BlockEntity blockEntity1 = null;
        if (level != null) {
            blockEntity1 = level.getBlockEntity(blockPos2);
        }
        if (blockEntity1 instanceof MatterInfuserUnitBlockEntity) {
            ItemHelper.dropContents(blockEntity1);
        }
        BlockPos blockpos = worldPosition.relative(direction.getOpposite());
        BlockState blockState = level.getBlockState(blockpos);
        if (blockState.is(TCBlocks.FLOW_CEDAR_CASING.get())) {
            level.setBlockAndUpdate(blockpos, blockState.setValue(BlockStateProperties.FACING, Direction.DOWN));
            BlockEntity blockEntity = level.getBlockEntity(blockpos);
            if (blockEntity instanceof FlowCedarCasingBlockEntity) {
                ItemHelper.dropContents(blockEntity,
                        FlowCedarCasingBlockEntity.UP_CONNECTION_SLOT,
                        FlowCedarCasingBlockEntity.DOWN_CONNECTION_SLOT);
            }
        }
        super.setRemoved();
    }
}
