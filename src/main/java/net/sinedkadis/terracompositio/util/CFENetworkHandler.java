package net.sinedkadis.terracompositio.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.sinedkadis.terracompositio.api.cfe.CFENetwork;
import net.sinedkadis.terracompositio.api.cfe.NetworkAction;
import net.sinedkadis.terracompositio.api.cfe.CFESource;
import net.sinedkadis.terracompositio.events.CFENetworkEvent;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static net.sinedkadis.terracompositio.util.TCUtil.distSqr;

public class CFENetworkHandler implements CFENetwork {
    public static final CFENetworkHandler instance = new CFENetworkHandler();

    private final Map<Level, Set<CFESource>> cfeSources = new WeakHashMap<>();

    public void onNetworkEvent(CFESource source, NetworkAction action) {
        if (action == NetworkAction.ADD){
            add(cfeSources,source.getCFESourceLevel(), source);
        }else{
            remove(cfeSources,source.getCFESourceLevel(), source);
        }
    }

    @Override
    public CFESource getClosestSourceWithCFE(BlockPos pos, Level level, int limit) {
        if (cfeSources.containsKey(level)) {
            return getClosestWithCFE(cfeSources.get(level), pos, limit);
        }
        return null;
    }

    @Override
    public CFESource getRandomSourceInRange(BlockPos pos, Level level, int limit) {
        if (cfeSources.containsKey(level)) {
            ArrayList<CFESource> list = new ArrayList<>(cfeSources.get(level).stream().toList());
            Collections.shuffle(list);
            return list.get(0);
        }
        return null;
    }

    @Nullable
    private <T extends CFESource> T getClosestWithCFE(Set<T> sources, BlockPos pos, int limit) {
        long minDist = Long.MAX_VALUE;
        long limitSquared = (long) limit * limit;
        T closest = null;

        for (var source : sources) {
            long distance = distSqr(source.getCFESourceBlockPos(), pos);
            if (distance <= limitSquared && distance < minDist && source.getCurrentCFE() > 0) {
                minDist = distance;
                closest = source;
            }
        }

        return closest;
    }

    @Override
    public List<CFESource> getAllCFESources(Level level) {
        if (cfeSources.containsKey(level)) {
            return cfeSources.get(level).stream().toList();
        }
        return List.of();
    }

    @Override
    public void fireCFENetworkEvent(CFESource source, NetworkAction action) {
        MinecraftForge.EVENT_BUS.post(new CFENetworkEvent(source,action));
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
    }

    public boolean isIn(Level pLevel, CFESource cfeSource) {
        return cfeSources.getOrDefault(pLevel, Collections.emptySet()).contains(cfeSource);
    }
}
