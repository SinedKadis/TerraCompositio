package net.sinedkadis.terracompositio.api.networks.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;


public interface FluidNetworkMemberBE {
    Level getLevel();
    BlockPos getBlockPos();
    int getPriority();
    int getLimit();
    void onFluidNetworkMemberUpdate();
    default BlockEntity getBE(){
        return  ((BlockEntity) this);
    }
}
