package net.sinedkadis.terracompositio.api.dummies;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.sinedkadis.terracompositio.api.networks.NetworkAction;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetwork;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetworkMember;
import net.sinedkadis.terracompositio.api.networks.cfe.ICFEHandler;
import net.sinedkadis.terracompositio.api.networks.fluid.FluidNetwork;
import net.sinedkadis.terracompositio.api.networks.fluid.FluidNetworkMemberBE;

import java.util.Set;

public class DummyNetwork implements CFENetwork, FluidNetwork {
    public static final DummyNetwork instance = new DummyNetwork();


    @Override
    public Set<CFENetworkMember> getAvailableNetworkTargets(CFENetworkMember requesterMember) {
        return Set.of();
    }

    @Override
    public Set<CFENetworkMember> getAllCFENetworkMembers(Level level) {
        return Set.of();
    }

    @Override
    public void fireCFENetworkEvent(CFENetworkMember source, NetworkAction action) {

    }

    @Override
    public boolean isIn(Level pLevel, ICFEHandler cfeHandler) {
        return true;
    }

    @Override
    public boolean isIn(Level pLevel, CFENetworkMember cfeHandler) {
        return true;
    }

    @Override
    public void fireFluidNetworkEvent(FluidNetworkMemberBE source, NetworkAction action) {

    }

    @Override
    public boolean isIn(Level pLevel, IFluidHandler cfeHandler) {
        return false;
    }

    @Override
    public boolean isIn(Level pLevel, FluidNetworkMemberBE cfeHandler) {
        return false;
    }

    @Override
    public void updateInRange(Level level, BlockPos origin, int range) {

    }

    @Override
    public Set<FluidNetworkMemberBE> getAvailableNetworkTargets(FluidNetworkMemberBE requesterMember) {
        return Set.of();
    }

    @Override
    public Set<FluidNetworkMemberBE> getAllFluidNetworkMembers(Level level) {
        return Set.of();
    }

}
