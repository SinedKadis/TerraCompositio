package net.sinedkadis.terracompositio.api.networks.cfe;

import net.minecraft.world.phys.Vec3;
import net.sinedkadis.terracompositio.api.networks.AnyNetworkMember;


public interface CFENetworkMember extends AnyNetworkMember {

    //Particle offset
    Vec3 center = new Vec3(0.5d,0.5d,0.5d);
    default Vec3 particleTargetOffset(){
        return center;
    }

    //Cfe Handler reference
    ICFEHandler getMainHandler();

    //Updates
    void updateIfScheduled();

    //Causeless updates
    void scheduleMemberUpdate();
    default void onCFENetworkMemberUpdate(){}

    //Update because of "updated"
    default void scheduleMemberUpdate(CFENetworkMember updated){
        scheduleMemberUpdate();
    }
    default void onCFENetworkMemberUpdate(CFENetworkMember updated){
        onCFENetworkMemberUpdate();
    }




}
