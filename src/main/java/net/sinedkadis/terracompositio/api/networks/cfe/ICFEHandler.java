package net.sinedkadis.terracompositio.api.networks.cfe;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

public interface ICFEHandler {
    int getCFE();
    int takeCFE(int cfe,boolean simulate);
    int addCFE(int cfe, BlockPos sourcePos, boolean simulate);
    void setCFE(int cfe);
    int getMaxCFE();
    int getMinCFE();
    void containerTick();
    void writeToNBT(CompoundTag pTag);
    void readFromNBT(CompoundTag pTag);
}
