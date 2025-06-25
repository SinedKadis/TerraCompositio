package net.sinedkadis.terracompositio.api.networks.cfe;

import net.minecraft.world.level.block.entity.BlockEntity;


public interface CFENetworkMemberBE extends CFENetworkMember{
    default BlockEntity getBE(){
        return  ((BlockEntity) this);
    }
}
