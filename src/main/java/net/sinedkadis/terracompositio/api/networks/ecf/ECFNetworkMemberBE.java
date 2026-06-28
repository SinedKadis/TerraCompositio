package net.sinedkadis.terracompositio.api.networks.ecf;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;


/**
 * The BlockEntity ECF Network Member. Implement that to your {@link BlockEntity}
 */
public interface ECFNetworkMemberBE extends ECFNetworkMember {
    @SuppressWarnings("unchecked")
    default BlockEntity getEntity(){
        return  ((BlockEntity) this);
    }
    Level getLevel();

    default BlockPos getPos() {
        return getEntity().getBlockPos();
    }
}
