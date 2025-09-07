package net.sinedkadis.terracompositio.api.networks.cfe;


import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public interface CFENetworkMemberEntity extends CFENetworkMember{
    default Entity getEntity(){
        return  ((Entity) this);
    }
    default Vec3 getPosition() {
        return getEntity().position();
    }
}
