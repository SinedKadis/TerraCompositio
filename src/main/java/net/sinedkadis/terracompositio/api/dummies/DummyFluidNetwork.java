package net.sinedkadis.terracompositio.api.dummies;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.sinedkadis.terracompositio.api.networks.NetworkAction;
import net.sinedkadis.terracompositio.api.networks.fluid.FluidNetwork;
import net.sinedkadis.terracompositio.api.networks.fluid.FluidNetworkMemberBE;

import java.util.List;

public class DummyFluidNetwork implements FluidNetwork {
    public static final DummyFluidNetwork instance = new DummyFluidNetwork();

    @Override
    public FluidNetworkMemberBE getClosestFluidHandlerWithMatchingContent(BlockPos pos, Level level, Fluid current, int limit, int priority) {
        return null;
    }

    @Override
    public FluidNetworkMemberBE getRandomFluidHandlerInRange(BlockPos pos, Level level, Fluid current, int limit, int priority) {
        return null;
    }

    @Override
    public List<FluidNetworkMemberBE> getAllFluidSources(Level level) {
        return List.of();
    }

    @Override
    public void fireFluidNetworkEvent(FluidNetworkMemberBE source, NetworkAction action) {

    }

    @Override
    public boolean isIn(Level pLevel, IFluidHandler fluidHandler) {
        return true;
    }

    @Override
    public boolean isIn(Level pLevel, FluidNetworkMemberBE cfeHandler) {
        return true;
    }
}
