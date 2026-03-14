package net.sinedkadis.terracompositio.cfe;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.sinedkadis.terracompositio.api.networks.cfe.*;
import net.sinedkadis.terracompositio.api.networks.NetworkAction;
import net.sinedkadis.terracompositio.events.CFENetworkEvent;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class CFENetworkHandler implements CFENetwork {
    public static final CFENetworkHandler INSTANCE = new CFENetworkHandler();

    private final Map<Level, Set<CFENetworkMember>> cfeSources = new WeakHashMap<>();

    public void onNetworkEvent(CFENetworkMember source, NetworkAction action) {
        if (action.equals(NetworkAction.ADD)){
            add(cfeSources,source.getLevel(), source);
        } else if (action.equals(NetworkAction.REMOVE)){
            remove(cfeSources,source.getLevel(), source);
        } else if (action.equals(NetworkAction.UPDATE)){
            networkMemberUpdated(source);
        } else throw new RuntimeException("Unsupported Network action: "+ action);
    }

    public void networkMemberUpdated(CFENetworkMember updated) {
        if (cfeSources.containsKey(updated.getLevel())) {
            cfeSources.get(updated.getLevel()).stream()
                    //Range check: taken max range between two
                    .filter(member -> updated.getPos().closerThan(member.getPos(),Math.max(updated.getRange(),member.getRange())))
                    //Entity check: no self update
                    .filter(member -> !updated.getEntity().equals(member.getEntity()))
                    //Priority check: High priority updates low priority
                    .filter(member -> updated.getPriority() > member.getPriority())
                    .forEach(cfeNetworkMember -> cfeNetworkMember.scheduleMemberUpdate(updated));

        }

    }

    @Override
    public @Nullable CFENetworkMember getMemberAt(Level level, BlockPos blockPos) {
        if (cfeSources.containsKey(level)) {
            for (CFENetworkMember member : cfeSources.get(level)){
                if (!member.getPos().equals(blockPos))
                    continue;
                return member;
            }
        }
        return null;
    }

    @Override
    public Set<CFENetworkMember> getAllCFENetworkMembers(Level level) {
        if (cfeSources.containsKey(level)) {
            return cfeSources.get(level);
        }
        return Set.of();
    }

    @Override
    public Set<CFENetworkMember> getAvailableNetworkTargets(CFENetworkMember source) {
        return getAvailableNetworkTargets(source.getLevel(),source.getPos(),source.getRange(),source.getPriority(),source.getEntity());
    }
    public Set<CFENetworkMember> getAvailableNetworkTargets(Level sourceLevel,
                                                                       BlockPos sourcePos,
                                                                       int sourceRange,
                                                                       int sourcePriority,
                                                                       Object sourceEntity) {
        if (cfeSources.containsKey(sourceLevel)) {
            Set<CFENetworkMember> cfeNetworkMembers = cfeSources.get(sourceLevel);
            return cfeNetworkMembers.stream()
                    //Distance check: needs to be in source range
                    .filter(member -> member.getPos().closerThan(sourcePos, sourceRange))
                    //Priority check: needs to be higher
                    .filter(member -> member.getPriority() > sourcePriority)
                    //Space check: needs to have empty space
                    //.filter(member -> member.getMainHandler().getFreeSpace() > 0)
                    //Entity check: don`t send to itself
                    .filter(member -> !member.getEntity().equals(sourceEntity))
                    .collect(Collectors.toSet());
        }
        return Set.of();
    }

    @Override
    public void fireCFENetworkEvent(CFENetworkMember source, NetworkAction action) {
        MinecraftForge.EVENT_BUS.post(new CFENetworkEvent(source,action));
    }

    @Override
    public boolean isIn(Level pLevel, ICFEHandler cfeHandler) {
        return cfeSources.getOrDefault(pLevel, Collections.emptySet()).stream()
                .anyMatch(member -> member.getMainHandler().equals(cfeHandler));
    }

    @Override
    public boolean isIn(Level pLevel, CFENetworkMember networkMember) {
        return cfeSources.getOrDefault(pLevel, Collections.emptySet()).stream()
                .anyMatch(member -> member.equals(networkMember));
    }


    private <T> void remove(Map<Level, Set<T>> map, Level level, T thing) {
        if (!map.containsKey(level)) {
            return;
        }
        networkMemberUpdated((CFENetworkMember) thing);
        var set = map.get(level);
        set.remove(thing);
        if (set.isEmpty()) {
            map.remove(level);
        }

    }

    private <T> void add(Map<Level, Set<T>> map, Level level, T thing) {
        map.computeIfAbsent(level, k -> new HashSet<>()).add(thing);
        networkMemberUpdated((CFENetworkMember) thing);
    }
}
