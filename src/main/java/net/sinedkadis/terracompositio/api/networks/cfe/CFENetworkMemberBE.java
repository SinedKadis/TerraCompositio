package net.sinedkadis.terracompositio.api.networks.cfe;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;


public interface CFENetworkMemberBE {
    Level getLevel();
    BlockPos getBlockPos();
    int getLimit();
    default BlockEntity getBE(){
        return  ((BlockEntity) this);
    }
    int getPriority();
    default void onCFENetworkMemberUpdate(){}
}
