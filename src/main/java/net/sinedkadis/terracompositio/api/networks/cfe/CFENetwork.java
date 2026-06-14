package net.sinedkadis.terracompositio.api.networks.cfe;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.sinedkadis.terracompositio.api.networks.NetworkAction;

import java.util.Set;

public interface CFENetwork {
    //Event
    void fireCFENetworkEvent(CFENetworkMember source, NetworkAction action);

    //Existence checks
    boolean isIn(Level pLevel, ICFEHandler cfeHandler);
    boolean isIn(Level pLevel, CFENetworkMember cfeHandler);

    void updateInRange(Level level, BlockPos origin, int range);

    Set<CFENetworkMember> getAvailableNetworkTargets(CFENetworkMember requesterMember);
    Set<CFENetworkMember> getAllCFENetworkMembers(Level level);
}
