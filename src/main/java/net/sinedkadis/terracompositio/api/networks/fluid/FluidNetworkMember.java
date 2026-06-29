package net.sinedkadis.terracompositio.api.networks.fluid;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.sinedkadis.terracompositio.api.networks.AnyNetworkMember;
import net.sinedkadis.terracompositio.api.networks.ecf.ECFNetworkMember;


/**
 * The BlockEntity Fluid Network Member. Implement that to your {@link BlockEntity}
 */
public interface FluidNetworkMember extends AnyNetworkMember {
    /**
     * Gets main handler of member.
     *
     * @return the main handler
     */
    IFluidHandler getMainHandler();

    /**
     * Executes updates, that was scheduled. Behavior relates on implementation, recommended to call at tick method of your entity,
     * and in method check if scheduled, than execute {@link FluidNetworkMember#onFluidNetworkMemberUpdate()}.
     */
    void updateIfScheduled();

    /**
     * Marks member to be scheduled for update on next tick.
     */
    void scheduleMemberUpdate();

    /**
     * Executes scheduled update, recommended to call from {@link FluidNetworkMember#updateIfScheduled()}.
     */
    default void onFluidNetworkMemberUpdate() {
    }

    /**
     * Marks member to be scheduled for update with given cause on next tick.
     *
     * @param updated the member, that caused update. Needs to be stored internally to make possible
     *                {@link FluidNetworkMember#onFluidNetworkMemberUpdate(FluidNetworkMember)} call
     */
    default void scheduleMemberUpdate(FluidNetworkMember updated) {
        scheduleMemberUpdate();
    }

    /**
     * Executes scheduled update, recommended to call from {@link ECFNetworkMember#updateIfScheduled()}.
     *
     * @param updated the member, that caused update
     */
    default void onFluidNetworkMemberUpdate(FluidNetworkMember updated) {
        onFluidNetworkMemberUpdate();
    }

}
