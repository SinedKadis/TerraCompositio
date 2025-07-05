package net.sinedkadis.terracompositio.api.networks.cfe;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.function.Function;

public interface ICFEHandler {
    int getCFE();
    List<Pair<Integer, Double>> getCfeQueue();
    int takeCFE(int cfe,boolean simulate);
    int addCFE(int cfe, BlockPos sourcePos, boolean simulate);
    void setCFE(int cfe);
    int getMaxCFE();
    void containerTick();
    Function<BlockPos, BlockPos> getTargetOffset();
    void writeToNBT(CompoundTag pTag);
    void readFromNBT(CompoundTag pTag);
}
