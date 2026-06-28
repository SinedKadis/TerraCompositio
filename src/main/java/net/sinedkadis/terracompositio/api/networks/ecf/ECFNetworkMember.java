package net.sinedkadis.terracompositio.api.networks.ecf;

import net.sinedkadis.terracompositio.api.networks.AnyNetworkMember;


/**
 * The ECF Network Member. Holds data about ECF handler and capable to scheduled lazy updates.
 */
public interface ECFNetworkMember extends AnyNetworkMember {
    /**
     * Gets main handler of member.
     *
     * @return the main handler
     */
    IECFHandler getMainHandler();

    /**
     * Executes updates, that was scheduled. Behavior relates on implementation, recommended to call at tick method of your entity,
     * and in method check if scheduled, than execute {@link ECFNetworkMember#onECFNetworkMemberUpdate()}.
     */
    void updateIfScheduled();

    /**
     * Marks member to be scheduled for update on next tick.
     */
    void scheduleMemberUpdate();

    /**
     * Executes scheduled update, recommended to call from {@link ECFNetworkMember#updateIfScheduled()}.
     */
    default void onECFNetworkMemberUpdate() {
    }

    /**
     * Marks member to be scheduled for update with given cause on next tick.
     *
     * @param updated the member, that caused update. Needs to be stored internally to make possible
     *                {@link ECFNetworkMember#onECFNetworkMemberUpdate(ECFNetworkMember)} call
     */
    default void scheduleMemberUpdate(ECFNetworkMember updated){
        scheduleMemberUpdate();
    }

    /**
     * Executes scheduled update, recommended to call from {@link ECFNetworkMember#updateIfScheduled()}.
     *
     * @param updated the member, that caused update
     */
    default void onECFNetworkMemberUpdate(ECFNetworkMember updated) {
        onECFNetworkMemberUpdate();
    }

}
