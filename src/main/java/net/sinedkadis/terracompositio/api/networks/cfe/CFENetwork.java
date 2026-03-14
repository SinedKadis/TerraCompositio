package net.sinedkadis.terracompositio.api.networks.cfe;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.sinedkadis.terracompositio.api.networks.NetworkAction;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Set;

public interface CFENetwork {
    //Event
    void fireCFENetworkEvent(CFENetworkMember source, NetworkAction action);

    //Existence checks
    boolean isIn(Level pLevel, ICFEHandler cfeHandler);
    boolean isIn(Level pLevel, CFENetworkMember cfeHandler);

    //Member getters
    @Nullable CFENetworkMember getMemberAt(Level level,BlockPos blockPos);
    Set<CFENetworkMember> getAvailableNetworkTargets(CFENetworkMember sender);
    Set<CFENetworkMember> getAllCFENetworkMembers(Level level);

}
