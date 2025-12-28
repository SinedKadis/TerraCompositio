package net.sinedkadis.terracompositio.api.networks.cfe;


import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public interface CFENetworkMemberEntity extends CFENetworkMember{
    default Entity getEntity(){
        return  ((Entity) this);
    }

    default Vec3 getPosition() {
        return getEntity().position();
    }
    default Level getLevel() {
        return getEntity().level();
    }
    default BlockPos getPos() {
        return getEntity().blockPosition();
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
