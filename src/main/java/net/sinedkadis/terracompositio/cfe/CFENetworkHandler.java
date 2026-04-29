package net.sinedkadis.terracompositio.cfe;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.MinecraftForge;
import net.sinedkadis.terracompositio.api.networks.cfe.*;
import net.sinedkadis.terracompositio.api.networks.NetworkAction;
import net.sinedkadis.terracompositio.block.entity.PathPointerBlockEntity;
import net.sinedkadis.terracompositio.events.CFENetworkEvent;
import net.sinedkadis.terracompositio.util.TCUtil;
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
        Level level = updated.getLevel();
        if (cfeSources.containsKey(level)) {
            Queue<CFENetworkMember> queue = new LinkedList<>();
            queue.add(updated);
            while (!queue.isEmpty()) {
                CFENetworkMember current = queue.poll();
                cfeSources.get(level).stream()
                        //Range check: taken max range between two
                        .filter(member -> current.getPos().closerThan(member.getPos(), Math.max(current.getRange(), member.getRange())))
                        //Entity check: no self update
                        .filter(member -> !current.getEntity().equals(member.getEntity()))
                        //Priority check: High priority updates low priority
                        .filter(member -> current.getPriority() > member.getPriority())
                        .peek(member -> {
                            if (member.getEntity() instanceof PathPointerBlockEntity ppBE
                                    && ppBE.parts.contains(PathPointerBlockEntity.PPPart.EMITTER)) {
                                Set<BlockPos> inputPoses = ppBE.getInputPoses();
                                inputPoses.forEach(blockPos -> {
                                    BlockEntity blockEntity = level.getBlockEntity(blockPos);
                                    if (blockEntity instanceof PathPointerBlockEntity inputEntity) {
                                        queue.add(new CFEMemberProxy(updated,inputEntity));
                                    }
                                });
                            }
                        })
                        .forEach(cfeNetworkMember -> cfeNetworkMember.scheduleMemberUpdate(current));
            }
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
    public Set<CFENetworkMember> getAvailableNetworkTargets(CFENetworkMember requesterMember) {
        Level level = requesterMember.getLevel();
        if (cfeSources.containsKey(level)) {
            Set<CFENetworkMember> toReturn = new HashSet<>();
            Set<CFENetworkMember> cfeNetworkMembers = cfeSources.get(level);
            Queue<CFENetworkMember> queue = new LinkedList<>();
            queue.add(requesterMember);
            while (!queue.isEmpty()) {
                CFENetworkMember current = queue.poll();
                Set<CFENetworkMember> filtered = cfeNetworkMembers.stream()
                        //Distance check: needs to be in source range
                        .filter(member -> member.getPos().closerThan(current.getPos(), current.getRange()))
                        //Priority check: needs to be higher
                        .filter(member -> member.getPriority() > current.getPriority())
                        //Space check: needs to have empty space
                        //.filter(member -> member.getMainHandler().getFreeSpace() > 0)
                        //Entity check: don't send to itself
                        .filter(member -> !member.getEntity().equals(current.getEntity()))
                        //Redirect position of target to closest collector
                        .map(member -> {
                            if (current instanceof CFEMemberProxy proxy) {
                                PathPointerBlockEntity ppBE = proxy.proxy();
                                if (ppBE.parts.contains(PathPointerBlockEntity.PPPart.EMITTER)) {
                                    BlockPos requesterMemberPos = requesterMember.getPos();
                                    Optional<BlockPos> reduced = ppBE.getInputPoses().stream()
                                            .reduce((input1, input2) ->
                                                    requesterMemberPos.distSqr(input1) < requesterMemberPos.distSqr(input2)
                                                            ? input1 : input2);
                                    if (reduced.isPresent()) {
                                        return new CFEMemberProxy(member,((PathPointerBlockEntity) level.getBlockEntity(reduced.get())));
                                    }
                                }
                            }
                            return member;
                        })
                        .collect(Collectors.toSet());

                toReturn.addAll(filtered);
                filtered.forEach(member -> {
                            if (member.getEntity() instanceof PathPointerBlockEntity ppBE
                                    && ppBE.parts.contains(PathPointerBlockEntity.PPPart.COLLECTOR)) {
                                BlockEntity blockEntity = level.getBlockEntity(ppBE.getOutputPos());
                                if (blockEntity instanceof PathPointerBlockEntity outputEntity) {
                                    queue.add(new CFEMemberProxy(requesterMember,outputEntity));
                                }
                            }
                        });

            }
            return toReturn.stream()
                    .filter(TCUtil::validMember)
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
