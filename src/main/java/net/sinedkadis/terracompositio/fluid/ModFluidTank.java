package net.sinedkadis.terracompositio.fluid;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class ModFluidTank extends FluidTank {
    private final BlockEntity blockEntity;

    public ModFluidTank(int size, BlockEntity blockEntity) {
        super(size);

        this.blockEntity = blockEntity;
    }

    public ModFluidTank(BlockEntity blockEntity) {
        this(1000,blockEntity);
    }

    @Override
    protected void onContentsChanged() {
        super.onContentsChanged();
        blockEntity.setChanged();
        Level level = blockEntity.getLevel();
        if (level != null && !level.isClientSide()) {
            BlockState blockState = blockEntity.getBlockState();
            level.sendBlockUpdated(blockEntity.getBlockPos(), blockState, blockState, 3);
        }
    }
}
