package net.sinedkadis.terracompositio.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.sinedkadis.terracompositio.api.networks.fluid.FluidNetworkMemberBE;
import net.sinedkadis.terracompositio.api.networks.NetworkAction;
import net.sinedkadis.terracompositio.api.networks.fluid.FluidNetwork;
import net.sinedkadis.terracompositio.events.FluidNetworkEvent;
import net.sinedkadis.terracompositio.util.TCUtil;

import java.util.*;

public class FluidNetworkHandler implements FluidNetwork {
    public static final FluidNetworkHandler instance = new FluidNetworkHandler();

    private final Map<Level, Set<FluidNetworkMemberBE>> fluidSources = new WeakHashMap<>();

    public void onNetworkEvent(FluidNetworkMemberBE source, NetworkAction action) {
        if (action == NetworkAction.ADD){
            add(fluidSources, source.getLevel(),source);
        } else if (action == NetworkAction.REMOVE){
            remove(fluidSources, source.getLevel(),source);
        } else {
            networkMemberUpdated(source);
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

    @Override
    public boolean isIn(Level pLevel, IFluidHandler fluidHandler) {
        return fluidSources.getOrDefault(pLevel, Collections.emptySet()).stream().anyMatch(fluidSource -> {
            Optional<IFluidHandler> fluidHandler2 = FluidNetwork.getFluidHandler(fluidSource);
            return fluidHandler2.map(iFluidHandler -> iFluidHandler.equals(fluidHandler)).orElse(false);
        });
    }

    @Override
    public boolean isIn(Level pLevel, FluidNetworkMemberBE fluidNetworkMemberBE) {
        return fluidSources.getOrDefault(pLevel, Collections.emptySet()).stream().anyMatch(fluidSource -> fluidSource.equals(fluidNetworkMemberBE));
    }

    public void networkMemberUpdated(FluidNetworkMemberBE updated) {
        if (fluidSources.containsKey(updated.getLevel())) {
            for (FluidNetworkMemberBE source : fluidSources.get(updated.getLevel())) {
                if (TCUtil.distSqr(source.getBlockPos(), updated.getBlockPos()) <= 10) {
                    source.onFluidNetworkMemberUpdate();
                }
            }
        }
    }

    @Override
    public FluidNetworkMemberBE getClosestFluidHandlerWithMatchingContent(BlockPos pos, Level level, Fluid current, int limit, int priority) {
        if (fluidSources.containsKey(level)) {
            long minDist = Long.MAX_VALUE;
            long limitSquared = (long) limit * limit;
            FluidNetworkMemberBE closest = null;

            for (FluidNetworkMemberBE source : fluidSources.get(level)) {
                long distance = TCUtil.distSqr(source.getBlockPos(), pos);
                Optional<IFluidHandler> fluidHandler = FluidNetwork.getFluidHandler(source);
                FluidStack fluidInTank = FluidStack.EMPTY;
                if (fluidHandler.isPresent()) {
                    fluidInTank = fluidHandler.get().getFluidInTank(0);
                }
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
    public FluidNetworkMemberBE getRandomFluidHandlerInRange(BlockPos pos, Level level, Fluid current, int limit, int priority) {
        if (fluidSources.containsKey(level)) {
            long limitSquared = (long) limit * limit;
            List<FluidNetworkMemberBE> sources = new ArrayList<>(fluidSources.get(level));
            Collections.shuffle(sources);
            for (FluidNetworkMemberBE source : sources) {
                long distance = TCUtil.distSqr(source.getBlockPos(), pos);
                Optional<IFluidHandler> fluidHandler = FluidNetwork.getFluidHandler(source);
                FluidStack fluidInTank = FluidStack.EMPTY;
                if (fluidHandler.isPresent()) {
                    fluidInTank = fluidHandler.get().getFluidInTank(0);
                }
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
    public List<FluidNetworkMemberBE> getAllFluidSources(Level level) {
        if (fluidSources.containsKey(level)) {
            return fluidSources.get(level).stream().toList();
        }
        return List.of();

    }

    @Override
    public void fireFluidNetworkEvent(FluidNetworkMemberBE source, NetworkAction action) {
        MinecraftForge.EVENT_BUS.post(new FluidNetworkEvent(source,action));
    }


}
