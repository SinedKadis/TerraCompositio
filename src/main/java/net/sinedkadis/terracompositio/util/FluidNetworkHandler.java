package net.sinedkadis.terracompositio.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.sinedkadis.terracompositio.api.FluidSource;
import net.sinedkadis.terracompositio.api.cfe.NetworkAction;
import net.sinedkadis.terracompositio.api.FluidNetwork;
import net.sinedkadis.terracompositio.events.FluidNetworkEvent;

import java.util.*;

public class FluidNetworkHandler implements FluidNetwork {
    public static final FluidNetworkHandler instance = new FluidNetworkHandler();

    private final Map<Level, Set<FluidSource>> fluidSources = new WeakHashMap<>();

    public void onNetworkEvent(FluidSource source, NetworkAction action) {
        if (action == NetworkAction.ADD){
            add(fluidSources, source.getLevel(),source);
        }else{
            remove(fluidSources, source.getLevel(),source);
        }
    }

    private <T> void remove(Map<Level, Set<T>> map, Level level, T source) {
        if (!map.containsKey(level)) {
            return;
        }

        var set = map.get(level);
        set.remove(source);
        if (set.isEmpty()) {
            map.remove(level);
        }
    }

    private <T> void add(Map<Level, Set<T>> map, Level level, T thing) {
        map.computeIfAbsent(level, k -> new HashSet<>()).add(thing);
    }

    public boolean isIn(Level pLevel, IFluidHandler fluidHandler) {
        return fluidSources.getOrDefault(pLevel, Collections.emptySet()).stream().anyMatch(fluidSource -> fluidSource.getFluidHandler().equals(fluidHandler));
    }

    @Override
    public FluidSource getClosestFluidHandlerWithMatchingContent(BlockPos pos, Level level, Fluid current, int limit, int priority) {
        if (fluidSources.containsKey(level)) {
            long minDist = Long.MAX_VALUE;
            long limitSquared = (long) limit * limit;
            FluidSource closest = null;

            for (FluidSource source : fluidSources.get(level)) {
                long distance = TCUtil.distSqr(source.getBlockPos(), pos);
                FluidStack fluidInTank = source.getFluidHandler().getFluidInTank(0);
                if (distance <= limitSquared
                        && distance < minDist
                        && fluidInTank.getAmount() > 0
                        && (fluidInTank.getFluid().isSame(current) || current.isSame(Fluids.EMPTY))
                        && source.getPriority() < priority) {
                    minDist = distance;
                    closest = source;
                }
            }

            return closest;
        }
        return null;
    }

    @Override
    public FluidSource getRandomFluidHandlerInRange(BlockPos pos, Level level, Fluid current, int limit, int priority) {
        if (fluidSources.containsKey(level)) {
            long limitSquared = (long) limit * limit;
            List<FluidSource> sources = new ArrayList<>(fluidSources.get(level));
            Collections.shuffle(sources);
            for (FluidSource source : sources) {
                long distance = TCUtil.distSqr(source.getBlockPos(), pos);
                FluidStack fluidInTank = source.getFluidHandler().getFluidInTank(0);
                if (distance <= limitSquared
                        && fluidInTank.getAmount() > 0
                        && (fluidInTank.getFluid().isSame(current) || current.isSame(Fluids.EMPTY))
                        && source.getPriority() < priority) {
                    return source;
                }
            }
        }
        return null;
    }

    @Override
    public List<FluidSource> getAllFluidSources(Level level) {
        if (fluidSources.containsKey(level)) {
            return fluidSources.get(level).stream().toList();
        }
        return List.of();

    }

    @Override
    public void fireFluidNetworkEvent(FluidSource source, NetworkAction action) {
        MinecraftForge.EVENT_BUS.post(new FluidNetworkEvent(source,action));
    }


}
