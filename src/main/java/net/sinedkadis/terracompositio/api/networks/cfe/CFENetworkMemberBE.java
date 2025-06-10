package net.sinedkadis.terracompositio.api.networks.cfe;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;


public interface CFENetworkMemberBE {
    Level getLevel();
    BlockPos getBlockPos();
    int getLimit();
    default BlockEntity getBE(){
        return  ((BlockEntity) this);
    }
    int getPriority();
    default void onCFENetworkMemberUpdate(){}

    Vec3 center = new Vec3(0.5d,0.5d,0.5d);
    default Vec3 particleTargetOffset(){
        return center;
    }
}
