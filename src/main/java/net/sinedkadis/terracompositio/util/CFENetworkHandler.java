package net.sinedkadis.terracompositio.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.sinedkadis.terracompositio.api.TerraCompositioAPI;
import net.sinedkadis.terracompositio.api.cfe.CFENetwork;
import net.sinedkadis.terracompositio.api.cfe.CFENetworkAction;
import net.sinedkadis.terracompositio.api.cfe.CFESource;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CFENetworkHandler implements CFENetwork {
    public static final CFENetworkHandler instance = new CFENetworkHandler();

    private final Map<Level, Set<CFESource>> cfeSources = new WeakHashMap<>();

    public void onNetworkEvent(CFESource source, CFENetworkAction action) {
        if (action == CFENetworkAction.ADD){
            add(cfeSources,source.getCFESourceLevel(),(CFESource) source);
        }else{
            remove(cfeSources,source.getCFESourceLevel(),(CFESource) source);
        }
    }

    @Override
    public CFESource getClosestSource(BlockPos pos, Level level, int limit) {
        if (cfeSources.containsKey(level)) {
            return getClosest(cfeSources.get(level), pos, limit);
        }
        return null;
    }
    @Nullable
    private <T extends CFESource> T getClosest(Set<T> receivers, BlockPos pos, int limit) {
        long minDist = Long.MAX_VALUE;
        long limitSquared = (long) limit * limit;
        T closest = null;

        for (var receiver : receivers) {
            long distance = distSqr(receiver.getCFESourceBlockPos(), pos);
            if (distance <= limitSquared && distance < minDist) {
                minDist = distance;
                closest = receiver;
            }
        }

        return closest;
    }
    public static long distSqr(Vec3i a, Vec3i b) {
        //Vec3i#distSqr, while convenient, offsets the second argument by (0.5, 0.5, 0.5).
        //Longs are used because "dx * dx" overflows with distances longer than about 46,300 blocks when using integers.
        long dx = a.getX() - b.getX();
        long dy = a.getY() - b.getY();
        long dz = a.getZ() - b.getZ();
        return dx * dx + dy * dy + dz * dz;
    }

    @Override
    public Set<CFESource> getAllCFESources(Level level) {
        return Set.of();
    }

    @Override
    public void fireCFENetworkEvent(CFESource source, CFENetworkAction action) {
        TerraCompositioAPI.INSTANCE.fireCFENetworkEvent(source,action);
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
