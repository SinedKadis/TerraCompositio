package net.sinedkadis.terracompositio.api.dummies;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.sinedkadis.terracompositio.api.networks.NetworkAction;
import net.sinedkadis.terracompositio.api.networks.cfe.ECFNetwork;
import net.sinedkadis.terracompositio.api.networks.cfe.ECFNetworkMember;
import net.sinedkadis.terracompositio.api.networks.cfe.IECFHandler;
import net.sinedkadis.terracompositio.api.networks.fluid.FluidNetwork;
import net.sinedkadis.terracompositio.api.networks.fluid.FluidNetworkMemberBE;

import java.util.Set;

public class DummyNetwork implements ECFNetwork, FluidNetwork {
    public static final DummyNetwork instance = new DummyNetwork();


    @Override
    public Set<ECFNetworkMember> getAvailableNetworkTargets(ECFNetworkMember requesterMember) {
        return Set.of();
    }

    @Override
    public Set<ECFNetworkMember> getAllCFENetworkMembers(Level level) {
        return Set.of();
    }

    @Override
    public void fireCFENetworkEvent(ECFNetworkMember source, NetworkAction action) {

    }

    @Override
    public boolean isIn(Level pLevel, IECFHandler cfeHandler) {
        return true;
    }

    @Override
    public boolean isIn(Level pLevel, ECFNetworkMember cfeHandler) {
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
