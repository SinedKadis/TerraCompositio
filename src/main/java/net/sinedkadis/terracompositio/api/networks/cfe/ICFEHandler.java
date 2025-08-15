package net.sinedkadis.terracompositio.api.networks.cfe;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.function.Function;

public interface ICFEHandler {
    int getCFE();
    List<Pair<Integer, Double>> getCfeQueue();

    ICFEHandler setIndex(int index);

    ICFEHandler setTargetOffset(Function<BlockPos, BlockPos> offset);

    int takeCFE(int cfe, boolean simulate);
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

    net.minecraft.world.level.block.entity.BlockEntity getBlockEntity();

    net.minecraft.world.entity.Entity getEntity();

    Function<BlockPos, BlockPos> getTargetOffset();

    void setBlockEntity(net.minecraft.world.level.block.entity.BlockEntity blockEntity);

    void setEntity(net.minecraft.world.entity.Entity entity);
}
