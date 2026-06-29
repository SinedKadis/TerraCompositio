package net.sinedkadis.terracompositio.api.networks.ecf;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;
import net.sinedkadis.terracompositio.api.helpers.ECFHelper;
import net.sinedkadis.terracompositio.util.IEntityInstance;

import java.util.function.Function;


/**
 * The ECF Handler interface for custom implementations. Extends {@link ECFNetworkMember} to be possible to pass as argument to methods like
 * {@link ECFHelper#doECFTransfer(ECFNetworkMember, ECFNetworkMember, int, float)}.
 */
@MethodsReturnNonnullByDefault
public interface IECFHandler extends ECFNetworkMember {
    /**
     * Gets energy value.
     *
     * @return the ECF
     */
    int getECF();

    /**
     * Sets energy value.
     *
     * @param ecf the ECF
     */
    void setECF(int ecf);

    /**
     * Gets max energy value, that handler can hold.
     *
     * @return the max ECF
     */
    int getMaxECF();

    /**
     * Sets max energy value, that handler can hold.
     *
     * @param max the max energy value, that handler can hold.
     * @return this handler, for chaining
     */
    IECFHandler setMaxECF(int max);

    /**
     * Adding ECF operation.
     *
     * @param cfe      the cfe to add
     * @param simulate pass true if just want the added value, or false to really add
     * @return the added value
     */
    int addECF(int cfe, boolean simulate);

    /**
     * Taking ECF operation.
     *
     * @param cfe      the cfe to take
     * @param simulate pass true if just want the taken value, or false to really take
     * @return the taken value
     */
    int takeECF(int cfe, boolean simulate);

    /**
     * Send ECF operation. Just creates ECF Burst with simulated taken value and sends it towards target. Also adds to targets queue sent value.
     * Do not take ECF by itself
     *
     * @param target the target member
     * @param cfe    the cfe
     * @param speed  the speed, default 1/20f - 1 block per second
     * @return the int
     */
    int sendECF(ECFNetworkMember target, int cfe, float speed);

    /**
     * Write data to nbt.
     *
     * @param pTag the tag
     */
    void writeToNBT(CompoundTag pTag);

    /**
     * Read data from nbt.
     *
     * @param pTag the tag
     */
    void readFromNBT(CompoundTag pTag);

    /**
     * Gets queued. Represents ECF value in bursts, that target that handler
     *
     * @return the queued
     */
    int getQueued();

    /**
     * Sets queued.
     *
     * @param queued the queued
     */
    void setQueued(int queued);

    /**
     * Adds to queue.
     *
     * @param toAdd the to add
     */
    default void addToQueue(int toAdd){
        setQueued(getQueued()+toAdd);
    }

    /**
     * Subs from queue.
     *
     * @param toSub the to sub
     */
    default void subFromQueue(int toSub){
        setQueued(Math.max(getQueued()-toSub,0));
    }

    /**
     * Gets ECF value with queue.
     *
     * @return the ecf with queue
     */
    int getECFWithQueue();

    /**
     * Checks if it has ECF.
     *
     * @return the boolean
     */
    boolean isEmpty();

    /**
     * Gets free space value in handler, that can be added.
     *
     * @return the free space
     */
    int getFreeSpace();

    /**
     * Clears the values of handler. Use via command /terracompositio clear-ecf-data
     */
    void clear();

    /**
     * Gets attached member.
     *
     * @return the attached member
     */
    IEntityInstance getAttachedEntity();

    /**
     * Gets offset. Used to move target relative attached member. Pass {@link Vec3#ZERO} to get clear offset,
     * or position to gen offset position
     *
     * @return the offset
     */
    Function<Vec3, Vec3> getOffset();

    /**
     * Sets offset.
     *
     * @param offset the offset
     * @return the offset
     */
    IECFHandler setOffset(Function<Vec3, Vec3> offset);

    /**
     * Gets index. Used to be possible having more than one handler in one member. Use in serialization
     *
     * @return the index
     */
    int getIndex();

    /**
     * Sets index.
     *
     * @param index the index
     * @return the index
     */
    IECFHandler setIndex(int index);


    default IEntityInstance getEntityInstance() {
        return getAttachedEntity();
    }

    default ECFNetworkMember getAttachedMember() {
        if (getAttachedEntity() instanceof ECFNetworkMember member) return member;
        throw new RuntimeException("Attached entity is not ECF Network Member");
    }

    @Override
    default int getRange() {
        return getAttachedMember().getRange();
    }

    @Override
    default int getPriority() {
        return getAttachedMember().getPriority();
    }

    @Override
    default IECFHandler getMainHandler() {
        return this;
    }

    @Override
    default void updateIfScheduled() {
        getAttachedMember().updateIfScheduled();
    }

    @Override
    default void scheduleMemberUpdate() {
        getAttachedMember().scheduleMemberUpdate();
    }
}
