package net.sinedkadis.terracompositio.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.sinedkadis.terracompositio.api.helpers.ECFHelper;
import net.sinedkadis.terracompositio.api.networks.NetworkAction;
import net.sinedkadis.terracompositio.api.networks.fluid.FluidNetwork;
import net.sinedkadis.terracompositio.api.networks.fluid.FluidNetworkMember;
import net.sinedkadis.terracompositio.events.FluidNetworkEvent;

import java.util.*;

public class FluidNetworkHandler implements FluidNetwork {
    public static final FluidNetworkHandler INSTANCE = new FluidNetworkHandler();

    private final Map<Level, Set<FluidNetworkMember>> fluidSources = new WeakHashMap<>();

    public void onNetworkEvent(FluidNetworkMember source, NetworkAction action) {
        if (action == NetworkAction.ADD){
            add(fluidSources, source.getEntityInstance().tc$getLevel(), source);
        } else if (action == NetworkAction.REMOVE){
            remove(fluidSources, source.getEntityInstance().tc$getLevel(), source);
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
    public boolean isIn(Level pLevel, FluidNetworkMember fluidHandler) {
        return fluidSources.getOrDefault(pLevel, Collections.emptySet()).stream().anyMatch(fluidSource -> fluidSource.equals(fluidHandler));
    }

    @Override
    public void updateInRange(Level level, BlockPos origin, int range) {
        Set<FluidNetworkMember> members = fluidSources.get(level);
        if (members == null) return;
        for (FluidNetworkMember member : members) {
            if (member.getEntityInstance().tc$getBlockPos().closerThan(origin, range)) {
                member.scheduleMemberUpdate();
            }
        }
    }

    @Override
    public Set<FluidNetworkMember> getAvailableNetworkTargets(FluidNetworkMember requesterMember) {
        Level level = requesterMember.getEntityInstance().tc$getLevel();
        Set<FluidNetworkMember> members = fluidSources.get(level);
        if (members == null) return Set.of();

        Set<FluidNetworkMember> toReturn = new HashSet<>();

        for (FluidNetworkMember member : members) {
            if (!member.getEntityInstance().tc$getBlockPos().closerThan(requesterMember.getEntityInstance().tc$getBlockPos(), requesterMember.getRange()))
                continue;
            if (member.getPriority() <= requesterMember.getPriority()) continue;
            if (member.getEntityInstance().equals(requesterMember.getEntityInstance())) continue;

            toReturn.add(member);
        }


        toReturn.removeIf(m -> !ECFHelper.validMember(m));
        return toReturn;
    }

    @Override
    public Set<FluidNetworkMember> getAllFluidNetworkMembers(Level level) {
        return fluidSources.get(level);
    }

    public void networkMemberUpdated(FluidNetworkMember updated) {
        if (fluidSources.containsKey(updated.getEntityInstance().tc$getLevel())) {
            for (FluidNetworkMember source : fluidSources.get(updated.getEntityInstance().tc$getLevel())) {
                if (source.getEntityInstance().tc$getBlockPos().closerThan(updated.getEntityInstance().tc$getBlockPos(), updated.getRange())) {
                    source.scheduleMemberUpdate();
                }
            }
        }
    }


    @Override
    public void fireFluidNetworkEvent(FluidNetworkMember source, NetworkAction action) {
        MinecraftForge.EVENT_BUS.post(new FluidNetworkEvent(source,action));
    }

    @Override
    public boolean isIn(Level pLevel, IFluidHandler fluidHandler) {
        return fluidSources.getOrDefault(pLevel, Collections.emptySet()).stream().anyMatch(fluidSource -> {
            IFluidHandler fluidHandler2 = fluidSource.getMainHandler();
            return fluidHandler2.equals(fluidHandler);
        });
    }


}
