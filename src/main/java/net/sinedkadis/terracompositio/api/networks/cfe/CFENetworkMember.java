package net.sinedkadis.terracompositio.api.networks.cfe;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;


public interface CFENetworkMember {
    //Filter values
    int getRange();
    int getPriority();

    //Particle offset
    Vec3 center = new Vec3(0.5d,0.5d,0.5d);
    default Vec3 particleTargetOffset(){
        return center;
    }

    //Cfe Handler reference
    ICFEHandler getMainHandler();

    //World data getters
    <T> T getEntity();
    Level getLevel();
    BlockPos getPos();

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
