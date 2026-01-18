package net.sinedkadis.terracompositio.fluid;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.sinedkadis.terracompositio.api.networks.NetworkAction;
import net.sinedkadis.terracompositio.api.networks.fluid.FluidNetworkMemberBE;
import net.sinedkadis.terracompositio.api.TerraCompositioAPI;

public class TCFluidTank extends FluidTank {
    private final BlockEntity blockEntity;

    public TCFluidTank(int size, FluidNetworkMemberBE blockEntity) {
        super(size);

        this.blockEntity = blockEntity.getEntity();
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
        if (blockEntity instanceof FluidNetworkMemberBE fluidNetworkMemberBE){
            TerraCompositioAPI.INSTANCE.getFluidNetworkInstance().fireFluidNetworkEvent(fluidNetworkMemberBE, NetworkAction.UPDATE);
        }
    }
}
