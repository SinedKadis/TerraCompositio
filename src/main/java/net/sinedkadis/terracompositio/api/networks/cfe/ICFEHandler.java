package net.sinedkadis.terracompositio.api.networks.cfe;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;

import java.util.function.Function;

public interface ICFEHandler extends BlockSource {
    int getCFE();

    ICFEHandler setIndex(int index);

    ICFEHandler setOffset(Function<Vec3, Vec3> offset);

    int takeCFE(int cfe, boolean simulate);
    int addCFE(int cfe,boolean simulate);
    int sendCFE(int cfe, ICFEHandler target, boolean simulate);
    int sendCFE(int cfe, BlockPos target, boolean simulate);
    void setCFE(int cfe);
    int getMaxCFE();

    ICFEHandler setCfeTravelSpeed(float cfeTravelSpeed);

    ICFEHandler setMaxCFE(int max);

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

    float getCfeTravelSpeed();

    CFENetworkMember getAttachedMember();

    Function<Vec3, Vec3> getOffset();

    int getIndex();
}
