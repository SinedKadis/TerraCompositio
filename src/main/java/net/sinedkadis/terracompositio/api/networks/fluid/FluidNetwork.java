package net.sinedkadis.terracompositio.api.networks.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.sinedkadis.terracompositio.api.networks.NetworkAction;

import java.util.Set;

/**
 * The Fluid network singleton. Used to manage interactions between different {@link FluidNetworkMember}.
 */
public interface FluidNetwork {
    /**
     * Fire fluid network event.
     *
     * @param source the source
     * @param action the action
     */
    void fireFluidNetworkEvent(FluidNetworkMember source, NetworkAction action);

    /**
     * Checks if handler already added to network.
     *
     * @param pLevel     the p level
     * @param ecfHandler the ecf handler
     * @return the boolean
     */
    boolean isIn(Level pLevel, IFluidHandler ecfHandler);

    /**
     * Checks if member already added to network.
     *
     * @param pLevel       the p level
     * @param fluidHandler the fluid handler
     * @return the boolean
     */
    boolean isIn(Level pLevel, FluidNetworkMember fluidHandler);

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
    Set<FluidNetworkMember> getAvailableNetworkTargets(FluidNetworkMember requesterMember);

    /**
     * Gets all fluid network members in given World.
     *
     * @param level the level
     * @return the all fluid network members
     */
    Set<FluidNetworkMember> getAllFluidNetworkMembers(Level level);
}
