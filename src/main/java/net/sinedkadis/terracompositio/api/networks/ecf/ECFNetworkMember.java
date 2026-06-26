package net.sinedkadis.terracompositio.api.networks.ecf;

import net.minecraft.world.phys.Vec3;
import net.sinedkadis.terracompositio.api.networks.AnyNetworkMember;


public interface ECFNetworkMember extends AnyNetworkMember {

    //Particle offset
    Vec3 center = new Vec3(0.5d,0.5d,0.5d);
    default Vec3 particleTargetOffset(){
        return center;
    }

    //Cfe Handler reference
    IECFHandler getMainHandler();

    //Updates
    void updateIfScheduled();

    //Causeless updates
    void scheduleMemberUpdate();

    default void onECFNetworkMemberUpdate() {
    }

    //Update because of "updated"
    default void scheduleMemberUpdate(ECFNetworkMember updated){
        scheduleMemberUpdate();
    }

    default void onECFNetworkMemberUpdate(ECFNetworkMember updated) {
        onECFNetworkMemberUpdate();
    }




}
