package net.sinedkadis.terracompositio.api.networks.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;


public interface FluidNetworkMemberBE {
    String UPDATE_TAG = "scheduledFluidUpdate";
    Level getLevel();
    BlockPos getBlockPos();
    int getPriority();
    int getLimit();
    default void scheduleMemberUpdate() {
        getEntity().getPersistentData().putBoolean(UPDATE_TAG,true);
    }
    default void updateIfScheduled() {
        CompoundTag persistentData = getEntity().getPersistentData();
        if (persistentData.contains(UPDATE_TAG) && persistentData.getBoolean(UPDATE_TAG)) {
            onFluidNetworkMemberUpdate();
            persistentData.putBoolean(UPDATE_TAG,false);
        }
    }
    void onFluidNetworkMemberUpdate();
    default BlockEntity getEntity(){
        return  ((BlockEntity) this);
    }
}
