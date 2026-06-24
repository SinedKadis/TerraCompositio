package net.sinedkadis.terracompositio.api.networks.cfe;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.sinedkadis.terracompositio.api.networks.NetworkAction;

import java.util.Set;

public interface ECFNetwork {
    //Event
    void fireCFENetworkEvent(ECFNetworkMember source, NetworkAction action);

    //Existence checks
    boolean isIn(Level pLevel, IECFHandler cfeHandler);
    boolean isIn(Level pLevel, ECFNetworkMember cfeHandler);

    void updateInRange(Level level, BlockPos origin, int range);

    Set<ECFNetworkMember> getAvailableNetworkTargets(ECFNetworkMember requesterMember);
    Set<ECFNetworkMember> getAllCFENetworkMembers(Level level);
}
