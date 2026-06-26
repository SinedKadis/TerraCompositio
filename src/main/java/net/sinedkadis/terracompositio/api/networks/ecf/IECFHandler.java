package net.sinedkadis.terracompositio.api.networks.ecf;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;
import java.util.function.Function;


@MethodsReturnNonnullByDefault
public interface IECFHandler extends BlockSource, ECFNetworkMember {
    int getECF();

    void setECF(int cfe);

    int getMaxECF();

    IECFHandler setMaxECF(int max);

    int addECF(int cfe, boolean simulate);

    int takeECF(int cfe, boolean simulate);

    int sendECF(ECFNetworkMember target, int cfe, float speed, boolean simulate);

    void writeToNBT(CompoundTag pTag);
    void readFromNBT(CompoundTag pTag);

    int getQueued();
    void setQueued(int queued);

    default void addToQueue(int toAdd){
        setQueued(getQueued()+toAdd);
    }
    default void subFromQueue(int toSub){
        setQueued(Math.max(getQueued()-toSub,0));
    }

    int getECFWithQueue();

    boolean isEmpty();
    int getFreeSpace();

    void clear();

    ECFNetworkMember getAttachedMember();

    Function<Vec3, Vec3> getOffset();
    IECFHandler setOffset(Function<Vec3, Vec3> offset);

    int getIndex();
    IECFHandler setIndex(int index);

    @Override
    default double x() {
        return (double)this.getPos().getX() + 0.5D;
    }
    @Override
    default double y() {
        return (double)this.getPos().getY() + 0.5D;
    }
    @Override
    default double z() {
        return (double)this.getPos().getZ() + 0.5D;
    }

    @Override
    default BlockPos getPos() {
        return getAttachedMember().getPos();
    }

    @Override
    default BlockState getBlockState() {
        return this.getLevel().getBlockState(this.getPos());
    }
    @Override
    default ServerLevel getLevel() {
        return (ServerLevel) getAttachedMember().getLevel();
    }

    @SuppressWarnings("unchecked")
    default BlockEntity getEntity() {
        return Objects.requireNonNull(this.getLevel().getBlockEntity(this.getPos()));
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
