package net.sinedkadis.terracompositio.api.cfe;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.List;

public interface CFENetwork {
    CFESource getClosestSource(BlockPos pos, Level level, int limit);
    CFESource getRandomSourceInRange(BlockPos pos, Level level, int limit);
    List<CFESource> getAllCFESources(Level level);
    void fireCFENetworkEvent(CFESource source, CFENetworkAction action);
}
