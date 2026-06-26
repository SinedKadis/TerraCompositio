package net.sinedkadis.terracompositio.api.networks.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.sinedkadis.terracompositio.api.networks.NetworkAction;

import java.util.Set;

public interface FluidNetwork {
    //Event
    void fireFluidNetworkEvent(FluidNetworkMemberBE source, NetworkAction action);

    //Existence checks
    boolean isIn(Level pLevel, IFluidHandler ecfHandler);

    boolean isIn(Level pLevel, FluidNetworkMemberBE fluidHandler);

    void updateInRange(Level level, BlockPos origin, int range);

    Set<FluidNetworkMemberBE> getAvailableNetworkTargets(FluidNetworkMemberBE requesterMember);

    Set<FluidNetworkMemberBE> getAllFluidNetworkMembers(Level level);
}
