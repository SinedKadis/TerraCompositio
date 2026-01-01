package net.sinedkadis.terracompositio.api.networks.cfe;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.sinedkadis.terracompositio.cfe.burst.CFEBurstProjectileEntity;

import java.util.List;


public interface CFENetworkMember {
    int getLimit();
    int getPriority();
    default void onCFENetworkMemberUpdate(){}

    Vec3 center = new Vec3(0.5d,0.5d,0.5d);
    default Vec3 particleTargetOffset(){
        return center;
    }

    default List<LazyOptional<ICFEHandler>> getCfeHandlers() {
        return List.of(((ICapabilityProvider) this).getCapability(CFECapability.CFE));
    }
    ICFEHandler getMainHandler();

    default int consumeCFEBurst(CFEBurstProjectileEntity burst) {
        int added = getMainHandler().addCFE(burst.getCFE(), false);
        burst.discard();
        return added;
    }

    String UPDATE_TAG = "scheduledCfeUpdate";

    Level getLevel();

    BlockPos getPos();

    void scheduleMemberUpdate();
    void updateIfScheduled();



}
