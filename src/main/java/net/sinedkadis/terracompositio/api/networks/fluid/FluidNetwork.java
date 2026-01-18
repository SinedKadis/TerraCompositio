package net.sinedkadis.terracompositio.api.networks.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.sinedkadis.terracompositio.api.networks.NetworkAction;

import java.util.List;
import java.util.Optional;

public interface FluidNetwork {
    FluidNetworkMemberBE getClosestFluidHandlerWithMatchingContent(BlockPos pos, Level level, Fluid current, int limit, int priority);
    FluidNetworkMemberBE getRandomFluidHandlerInRange(BlockPos pos, Level level, Fluid current, int limit, int priority);
    List<FluidNetworkMemberBE> getAllFluidSources(Level level);
    void fireFluidNetworkEvent(FluidNetworkMemberBE source, NetworkAction action);
    boolean isIn(Level pLevel, IFluidHandler fluidHandler);
    boolean isIn(Level pLevel, FluidNetworkMemberBE cfeHandler);
    static Optional<IFluidHandler> getFluidHandler(FluidNetworkMemberBE source){
        return source.getEntity().getCapability(ForgeCapabilities.FLUID_HANDLER).resolve();
    }
}
