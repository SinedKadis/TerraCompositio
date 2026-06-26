package net.sinedkadis.terracompositio.api.networks.ecf;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;


public interface ECFNetworkMemberBE extends ECFNetworkMember {
    @SuppressWarnings("unchecked")
    default BlockEntity getEntity(){
        return  ((BlockEntity) this);
    }
    default Level getLevel() {
        return getEntity().getLevel();
    }
    default BlockPos getPos() {
        return getEntity().getBlockPos();
    }
}
