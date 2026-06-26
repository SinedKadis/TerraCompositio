package net.sinedkadis.terracompositio.api.networks.ecf;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.sinedkadis.terracompositio.api.dummies.DummyECFHandler;
import net.sinedkadis.terracompositio.api.networks.NetworkAction;

import java.util.Set;

/**
 * The ECF network singleton. Used to manage interactions between different {@link ECFNetworkMember}.
 */
public interface ECFNetwork {
    /**
     * Fire forge ecf network event.
     *
     * @param source the source
     * @param action the action
     */
    void fireECFNetworkEvent(ECFNetworkMember source, NetworkAction action);

    /**
     * Checks if member already added to network.
     *
     * @param pLevel     the p level
     * @param ecfHandler the ecf handler
     * @return the boolean
     */
    boolean isIn(Level pLevel, ECFNetworkMember ecfHandler);

    /**
     * Updates members in given range.
     *
     * @param level  the level
     * @param origin the origin
     * @param range  the range
     */
    void updateInRange(Level level, BlockPos origin, int range);

    /**
     * Searches for available to sent ECF members
     *
     * @param requesterMember the member that request search
     * @return the available network targets
     */
    Set<ECFNetworkMember> getAvailableNetworkTargets(ECFNetworkMember requesterMember);

    /**
     * Gets all ecf network members in given world.
     *
     * @param level the level
     * @return the all ecf network members
     */
    Set<ECFNetworkMember> getAllECFNetworkMembers(Level level);

    /**
     * Gets ecf transfer limit. Reads config value if Terracompositio exist
     *
     * @return the ecf transfer limit
     */
    int getECFTransferLimit();

    /**
     * Creates default ecf handler if Terracompositio exist. Usable in
     * {@link net.minecraft.world.level.block.entity.BlockEntity},
     * {@link net.minecraft.world.entity.Entity} and other
     *
     * @param attachedMember the attached to handler member
     * @return the iecf handler
     */
    default IECFHandler createDefaultECFHandler(ECFNetworkMember attachedMember) {
        return DummyECFHandler.instance;
    }
}
