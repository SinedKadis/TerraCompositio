package net.sinedkadis.terracompositio.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.sinedkadis.terracompositio.api.networks.NetworkAction;
import net.sinedkadis.terracompositio.api.networks.fluid.FluidNetwork;
import net.sinedkadis.terracompositio.api.networks.fluid.FluidNetworkMemberBE;
import net.sinedkadis.terracompositio.events.FluidNetworkEvent;
import net.sinedkadis.terracompositio.util.helpers.ECFHelper;

import java.util.*;

public class FluidNetworkHandler implements FluidNetwork {
    public static final FluidNetworkHandler INSTANCE = new FluidNetworkHandler();

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
    public boolean isIn(Level pLevel, FluidNetworkMemberBE fluidHandler) {
        return fluidSources.getOrDefault(pLevel, Collections.emptySet()).stream().anyMatch(fluidSource -> fluidSource.equals(fluidHandler));
    }

    @Override
    public void updateInRange(Level level, BlockPos origin, int range) {
        Set<FluidNetworkMemberBE> members = fluidSources.get(level);
        if (members == null) return;
        for (FluidNetworkMemberBE member : members) {
            if (member.getPos().closerThan(origin, range)) {
                member.scheduleMemberUpdate();
            }
        }
    }

    @Override
    public Set<FluidNetworkMemberBE> getAvailableNetworkTargets(FluidNetworkMemberBE requesterMember) {
        Level level = requesterMember.getLevel();
        Set<FluidNetworkMemberBE> members = fluidSources.get(level);
        if (members == null) return Set.of();

        Set<FluidNetworkMemberBE> toReturn = new HashSet<>();

        for (FluidNetworkMemberBE member : members) {
            if (!member.getPos().closerThan(requesterMember.getPos(), requesterMember.getRange())) continue;
            if (member.getPriority() <= requesterMember.getPriority()) continue;
            if (member.getEntity().equals(requesterMember.getEntity())) continue;

            toReturn.add(member);
        }


        toReturn.removeIf(m -> !ECFHelper.validMember(m));
        return toReturn;
    }

    @Override
    public Set<FluidNetworkMemberBE> getAllFluidNetworkMembers(Level level) {
        return fluidSources.get(level);
    }

    public void networkMemberUpdated(FluidNetworkMemberBE updated) {
        if (fluidSources.containsKey(updated.getLevel())) {
            for (FluidNetworkMemberBE source : fluidSources.get(updated.getLevel())) {
                if (source.getPos().closerThan(updated.getPos(), updated.getRange())) {
                    source.scheduleMemberUpdate();
                }
            }
        }
    }


    @Override
    public void fireFluidNetworkEvent(FluidNetworkMemberBE source, NetworkAction action) {
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
