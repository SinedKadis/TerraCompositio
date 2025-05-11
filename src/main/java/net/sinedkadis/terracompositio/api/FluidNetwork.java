package net.sinedkadis.terracompositio.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.sinedkadis.terracompositio.api.cfe.NetworkAction;

import java.util.List;

public interface FluidNetwork {
    FluidSource getClosestFluidHandlerWithMatchingContent(BlockPos pos, Level level, Fluid current, int limit, int priority);
    FluidSource getRandomFluidHandlerInRange(BlockPos pos, Level level, Fluid current, int limit, int priority);
    List<FluidSource> getAllFluidSources(Level level);
    void fireFluidNetworkEvent(FluidSource source, NetworkAction action);
    boolean isIn(Level pLevel, IFluidHandler fluidHandler);
}
