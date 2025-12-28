package net.sinedkadis.terracompositio.api.networks.cfe;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;


public interface CFENetworkMemberBE extends CFENetworkMember{
    default BlockEntity getEntity(){
        return  ((BlockEntity) this);
    }
    default Level getLevel() {
        return getEntity().getLevel();
    }
    default BlockPos getPos() {
        return getEntity().getBlockPos();
    }

    default void scheduleMemberUpdate() {
        getEntity().getPersistentData().putBoolean(UPDATE_TAG,true);
    }
    default void updateIfScheduled() {
        CompoundTag persistentData = getEntity().getPersistentData();
        if (persistentData.contains(UPDATE_TAG) && persistentData.getBoolean(UPDATE_TAG)) {
            onCFENetworkMemberUpdate();
            persistentData.putBoolean(UPDATE_TAG,false);
        }
    }
}
