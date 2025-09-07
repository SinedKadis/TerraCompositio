package net.sinedkadis.terracompositio.api.networks.cfe;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;

import java.util.function.Function;

public interface ICFEHandler {
    int getCFE();

    ICFEHandler setIndex(int index);

    ICFEHandler setOffset(Function<Vec3, Vec3> offset);

    int takeCFE(int cfe, boolean simulate);
    int addCFE(int cfe, ICFEHandler source, boolean simulate,boolean doRender);
    int addCFE(int cfe, BlockPos sourcePos, boolean simulate);
    void setCFE(int cfe);
    int getMaxCFE();

    ICFEHandler setCfeTravelSpeed(float cfeTravelSpeed);

    ICFEHandler setMaxCFE(int max);
    void containerTick();
    //Function<BlockPos, BlockPos> getTargetOffset();
    void writeToNBT(CompoundTag pTag);
    void readFromNBT(CompoundTag pTag);

    boolean isEmpty();
    int getFreeSpace();
    int getQueued();

    BlockPos getBlockPos();

    float getCfeTravelSpeed();

    CFENetworkMember getAttachedMember();

    Function<Vec3, Vec3> getOffset();

    void setAttachedMember(CFENetworkMember member);

    boolean isEntity();

    int getIndex();
}
