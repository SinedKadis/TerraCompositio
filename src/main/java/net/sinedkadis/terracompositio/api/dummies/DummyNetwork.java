package net.sinedkadis.terracompositio.api.dummies;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.sinedkadis.terracompositio.api.TerraCompositioAPI;
import net.sinedkadis.terracompositio.api.networks.NetworkAction;
import net.sinedkadis.terracompositio.api.networks.ecf.ECFNetwork;
import net.sinedkadis.terracompositio.api.networks.ecf.ECFNetworkMember;
import net.sinedkadis.terracompositio.api.networks.fluid.FluidNetwork;
import net.sinedkadis.terracompositio.api.networks.fluid.FluidNetworkMemberBE;

import java.util.Set;

/**
 * The placeholder, returned by {@link TerraCompositioAPI#getECFNetworkInstance()} and {@link TerraCompositioAPI#getFluidNetworkInstance()}, if Terracompositio is not present
 */
public class DummyNetwork implements ECFNetwork, FluidNetwork {
    public static final DummyNetwork instance = new DummyNetwork();


    @Override
    public Set<ECFNetworkMember> getAvailableNetworkTargets(ECFNetworkMember requesterMember) {
        return Set.of();
    }

    @Override
    public Set<ECFNetworkMember> getAllECFNetworkMembers(Level level) {
        return Set.of();
    }

    @Override
    public int getECFTransferLimit() {
        return 20;
    }

    @Override
    public void fireECFNetworkEvent(ECFNetworkMember source, NetworkAction action) {

    }

    @Override
    public boolean isIn(Level pLevel, ECFNetworkMember ecfHandler) {
        return true;
    }

    @Override
    public void fireFluidNetworkEvent(FluidNetworkMemberBE source, NetworkAction action) {

    }

    @Override
    public boolean isIn(Level pLevel, IFluidHandler ecfHandler) {
        return false;
    }

    @Override
    public boolean isIn(Level pLevel, FluidNetworkMemberBE ecfHandler) {
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
