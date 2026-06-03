package net.sinedkadis.terracompositio.api.networks.cfe;

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
public interface ICFEHandler extends BlockSource {
    int getCFE();
    void setCFE(int cfe);

    int getMaxCFE();
    ICFEHandler setMaxCFE(int max);

    int addCFE(int cfe, boolean simulate);
    int takeCFE(int cfe, boolean simulate);

    int sendCFE(int cfe, CFENetworkMember target, float speed, boolean simulate);

    int sendCFE(int cfe, ICFEHandler target, float speed, boolean noCollision, boolean simulate);

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

    int getCFEWithQueue();

    boolean isEmpty();
    int getFreeSpace();

    void clear();

    CFENetworkMember getAttachedMember();

    Function<Vec3, Vec3> getOffset();
    ICFEHandler setOffset(Function<Vec3, Vec3> offset);

    int getIndex();
    ICFEHandler setIndex(int index);

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

    default <T extends BlockEntity> T getEntity() {
        //noinspection unchecked
        return (T) Objects.requireNonNull(this.getLevel().getBlockEntity(this.getPos()));
    }
}
