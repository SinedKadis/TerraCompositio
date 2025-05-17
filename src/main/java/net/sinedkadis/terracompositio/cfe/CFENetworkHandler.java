package net.sinedkadis.terracompositio.cfe;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetwork;
import net.sinedkadis.terracompositio.api.networks.NetworkAction;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetworkMemberBE;
import net.sinedkadis.terracompositio.api.networks.cfe.ICFEHandler;
import net.sinedkadis.terracompositio.events.CFENetworkEvent;
import net.sinedkadis.terracompositio.util.TCUtil;

import java.util.*;

import static net.sinedkadis.terracompositio.util.TCUtil.distSqr;

public class CFENetworkHandler implements CFENetwork {
    public static final CFENetworkHandler instance = new CFENetworkHandler();

    private final Map<Level, Set<CFENetworkMemberBE>> cfeSources = new WeakHashMap<>();

    public void onNetworkEvent(CFENetworkMemberBE source, NetworkAction action) {
        if (action == NetworkAction.ADD){
            add(cfeSources,source.getLevel(), source);
        } else if (action == NetworkAction.REMOVE){
            remove(cfeSources,source.getLevel(), source);
        } else {
            networkMemberUpdated(source);
        }
    }

    public void networkMemberUpdated(CFENetworkMemberBE updated) {
        cfeSources.get(updated.getLevel()).stream()
                .filter(member -> {
                    if (updated.getBlockPos() != member.getBlockPos()) {
                        return true;
                    } else {
                        member.onCFENetworkMemberUpdate();
                        return false;
                    }
                })
                .filter(member -> updated.getPriority() < member.getPriority())
                .filter(member -> distSqr(member.getBlockPos(), updated.getBlockPos()) <= ((long) member.getLimit() * member.getLimit()))
                .forEach(CFENetworkMemberBE::onCFENetworkMemberUpdate);
    }

    @Override
    public CFENetworkMemberBE getClosestSourceWithCFE(BlockPos pos, Level level, int limit, int priority) {
        if (cfeSources.containsKey(level)) {
            Set<CFENetworkMemberBE> sources = cfeSources.get(level);
            long minDist = Long.MAX_VALUE;
            long limitSquared = (long) limit * limit;
            CFENetworkMemberBE closest = null;

            for (CFENetworkMemberBE source : sources) {
                long distance = distSqr(source.getBlockPos(), pos);
                Optional<ICFEHandler> cfeHandlerOptional = source.getBE().getCapability(CFECapability.CFE).resolve();
                if (distance <= limitSquared
                        && distance < minDist
                        && cfeHandlerOptional.isPresent()
                        && cfeHandlerOptional.get().getCFE() > 0
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
    public CFENetworkMemberBE getRandomSourceInRange(BlockPos pos, Level level, int limit, int priority) {
        if (cfeSources.containsKey(level)) {
            long limitSquared = (long) limit * limit;
            List<CFENetworkMemberBE> sources = new ArrayList<>(cfeSources.get(level));
            Collections.shuffle(sources);
            for (CFENetworkMemberBE source : sources) {
                long distance = TCUtil.distSqr(source.getBlockPos(), pos);
                Optional<ICFEHandler> fluidHandler = CFENetwork.getCFEHandler(source);
                int cfe = 0;
                if (fluidHandler.isPresent()) {
                    cfe = fluidHandler.get().getCFE();
                }
                if (distance <= limitSquared
                        && cfe > 0
                        && source.getPriority() < priority) {
                    return source;
                }
            }
        }
        return null;
    }

    @Override
    public List<CFENetworkMemberBE> getAllCFENetworkMembers(Level level) {
        if (cfeSources.containsKey(level)) {
            return cfeSources.get(level).stream().toList();
        }
        return List.of();
    }

    @Override
    public void fireCFENetworkEvent(CFENetworkMemberBE source, NetworkAction action) {
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
    public boolean isIn(Level pLevel, CFENetworkMemberBE cfeHandler) {
        return cfeSources.getOrDefault(pLevel, Collections.emptySet()).stream().anyMatch(fluidSource -> fluidSource.equals(cfeHandler));
    }



    private <T> void remove(Map<Level, Set<T>> map, Level level, T thing) {
        if (!map.containsKey(level)) {
            return;
        }

        var set = map.get(level);
        set.remove(thing);
        if (set.isEmpty()) {
            map.remove(level);
        }
    }

    private <T> void add(Map<Level, Set<T>> map, Level level, T thing) {
        map.computeIfAbsent(level, k -> new HashSet<>()).add(thing);
        networkMemberUpdated((CFENetworkMemberBE) thing);
    }
}
