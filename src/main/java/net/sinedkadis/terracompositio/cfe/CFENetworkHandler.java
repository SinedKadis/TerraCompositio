package net.sinedkadis.terracompositio.cfe;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.sinedkadis.terracompositio.api.networks.cfe.*;
import net.sinedkadis.terracompositio.api.networks.NetworkAction;
import net.sinedkadis.terracompositio.block.entity.CFESaturatedAirBlockEntity;
import net.sinedkadis.terracompositio.block.entity.PathPointerBlockEntity;
import net.sinedkadis.terracompositio.events.CFENetworkEvent;
import net.sinedkadis.terracompositio.registries.TCBlocks;
import net.sinedkadis.terracompositio.util.TCUtil;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.*;

import static net.sinedkadis.terracompositio.util.TCUtil.distSqr;

public class CFENetworkHandler implements CFENetwork {
    public static final CFENetworkHandler instance = new CFENetworkHandler();

    private final Map<Level, Set<CFENetworkMember>> cfeSources = new WeakHashMap<>();

    public void onNetworkEvent(CFENetworkMember source, NetworkAction action) {
        if (action == NetworkAction.ADD){
            add(cfeSources,source.getLevel(), source);
        } else if (action == NetworkAction.REMOVE){
            remove(cfeSources,source.getLevel(), source);
        } else {
            networkMemberUpdated(source);
        }
    }

    public void networkMemberUpdated(CFENetworkMember updated) {
        if (cfeSources.containsKey(updated.getLevel())) {
            if (updated instanceof CFESaturatedAirBlockEntity) return;
//            if (updated instanceof Player) {
//                updated.getLimit()
//            }
            cfeSources.get(updated.getLevel()).stream()
                    .filter(member -> updated.getBlockPos().closerThan(member.getBlockPos(),updated.getLimit()))
                    .filter(member -> !updated.getBlockPos().equals(member.getBlockPos()))
                    .peek(member -> {
                        if (member instanceof PathPointerBlockEntity && !(updated.getPriority() < member.getPriority())) {
                            member.onCFENetworkMemberUpdate(member.getLevel(),member.getBlockPos());
                        }
                    })
                    .filter(member -> updated.getPriority() < member.getPriority())
                    .forEach((member) -> member.onCFENetworkMemberUpdate(member.getLevel(),member.getBlockPos()));
            if (updated instanceof PathPointerBlockEntity blockEntity) {
                List<PathPointerBlockEntity> nodes = blockEntity.getNodes();
                nodes.remove(blockEntity);
                nodes.forEach(member -> member.onCFENetworkMemberUpdate(updated.getLevel(),member.getBlockPos()));
            }
        }

    }

    @Override
    public CFENetworkMember getClosestSourceWithCFE(BlockPos pos, Level level, int limit, @Nullable Integer priority) {
        if (cfeSources.containsKey(level)) {
            Set<CFENetworkMember> sources = cfeSources.get(level);
            long minDist = Long.MAX_VALUE;
            long limitSquared = (long) limit * limit;
            CFENetworkMember closest = null;
            BlockState blockState = level.getBlockState(pos);
            boolean skipAir = blockState.is(TCBlocks.AIR_SATURATOR.get());

            for (CFENetworkMember source : sources) {
                if (source instanceof CFENetworkMemberBE memberBE) {
                    if (memberBE instanceof CFESaturatedAirBlockEntity && skipAir) continue;
                    long distance = distSqr(source.getBlockPos(), pos);
                    Optional<ICFEHandler> cfeHandlerOptional = memberBE.getBE().getCapability(CFECapability.CFE).resolve();
                    if (distance <= limitSquared
                            && distance < minDist
                            && distance < (long) source.getLimit() * source.getLimit()
                            && cfeHandlerOptional.isPresent()
                            && cfeHandlerOptional.get().getCFE() > 0
                            && (priority == null || source.getPriority() < priority)) {
                        minDist = distance;
                        closest = memberBE;
                    }
                }
            }

            return closest;
        }
        return null;
    }

    @Override
    public CFENetworkMember getRandomSourceInRange(BlockPos pos, Level level, int limit, @Nullable Integer priority) {
        if (cfeSources.containsKey(level)) {
            long limitSquared = (long) limit * limit;
            List<CFENetworkMember> sources = new ArrayList<>(cfeSources.get(level));
            Collections.shuffle(sources);
            for (CFENetworkMember source : sources) {
                long distance = TCUtil.distSqr(source.getBlockPos(), pos);
                Optional<ICFEHandler> fluidHandler = CFENetwork.getCFEHandler(source);
                int cfe = 0;
                if (fluidHandler.isPresent()) {
                    cfe = fluidHandler.get().getCFE();
                }
                if (distance <= limitSquared
                        && cfe > 0
                        && (priority == null || source.getPriority() < priority)) {
                    return source;
                }
            }
        }
        return null;
    }

    @Override
    public @Nullable CFENetworkMember getMemberAt(Level level, BlockPos blockPos) {
        if (cfeSources.containsKey(level)) {
            for (CFENetworkMember member : cfeSources.get(level)){
                if (!member.getBlockPos().equals(blockPos))
                    continue;
                return member;
            }
        }
        return null;
    }

    @Override
    public List<CFENetworkMember> getAllCFENetworkMembers(Level level) {
        if (cfeSources.containsKey(level)) {
            return cfeSources.get(level).stream().toList();
        }
        return List.of();
    }

    @Override
    public void fireCFENetworkEvent(CFENetworkMember source, NetworkAction action) {
        MinecraftForge.EVENT_BUS.post(new CFENetworkEvent(source,action));
    }

    @Override
    public boolean isIn(Level pLevel, ICFEHandler cfeHandler) {
        return cfeSources.getOrDefault(pLevel, Collections.emptySet()).stream().anyMatch(fluidSource -> {
            Optional<ICFEHandler> fluidHandler2 = CFENetwork.getCFEHandler(fluidSource);
            return fluidHandler2.map(iFluidHandler -> iFluidHandler.equals(cfeHandler)).orElse(false);
        });
    }

    @Override
    public boolean isIn(Level pLevel, CFENetworkMember cfeHandler) {
        return cfeSources.getOrDefault(pLevel, Collections.emptySet()).stream().anyMatch(fluidSource -> fluidSource.equals(cfeHandler));
    }

    @Override
    public ICFEHandler createCFEHandler(CFENetworkMember entity) {
        return new CFEContainer(entity);
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
