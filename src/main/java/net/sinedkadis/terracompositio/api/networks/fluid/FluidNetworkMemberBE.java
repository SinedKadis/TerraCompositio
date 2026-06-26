package net.sinedkadis.terracompositio.api.networks.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.sinedkadis.terracompositio.api.networks.AnyNetworkMember;


public interface FluidNetworkMemberBE extends AnyNetworkMember {
    //Fluid Handler reference
    IFluidHandler getMainHandler();

    //Updates
    void updateIfScheduled();

    //Causeless updates
    void scheduleMemberUpdate();

    default void onFluidNetworkMemberUpdate() {
    }

    //Update because of "updated"
    default void scheduleMemberUpdate(FluidNetworkMemberBE updated) {
        scheduleMemberUpdate();
    }

    default void onFluidNetworkMemberUpdate(FluidNetworkMemberBE updated) {
        onFluidNetworkMemberUpdate();
    }

    @SuppressWarnings("unchecked")
    default BlockEntity getEntity(){
        return  ((BlockEntity) this);
    }

    @Override
    default BlockPos getPos() {
        return getEntity().getBlockPos();
    }
}
