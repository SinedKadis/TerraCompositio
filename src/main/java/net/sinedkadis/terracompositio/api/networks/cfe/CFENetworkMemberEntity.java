package net.sinedkadis.terracompositio.api.networks.cfe;


import net.minecraft.world.entity.Entity;

public interface CFENetworkMemberEntity extends CFENetworkMember{
    default Entity getEntity(){
        return  ((Entity) this);
    }
}
