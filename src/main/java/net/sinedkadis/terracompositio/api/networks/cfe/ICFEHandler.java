package net.sinedkadis.terracompositio.api.networks.cfe;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public interface ICFEHandler {
    int getCFE();
    List<Pair<Integer, Double>> getCfeQueue();
    int takeCFE(int cfe,boolean simulate);
    int addCFE(int cfe, BlockPos sourcePos, boolean simulate);
    void setCFE(int cfe);
    int getMaxCFE();
    int getMinCFE();
    void containerTick();
    void writeToNBT(CompoundTag pTag);
    void readFromNBT(CompoundTag pTag);
}
