package net.sinedkadis.terracompositio.api.cfe;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.Set;

public interface CFENetwork {
    CFESource getClosestSource(BlockPos pos, Level level, int limit);
    Set<CFESource> getAllCFESources(Level level);
    void fireCFENetworkEvent(CFESource source, CFENetworkAction action);
}
