package net.sinedkadis.terracompositio.api.cfe;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;


public interface CFESource {
    Level getCFESourceLevel();
    BlockPos getCFESourceBlockPos();
    int getCurrentCFE();
    int takeCFE(int cfe);
    default int addCFE(int cfe){
        return -takeCFE(-cfe);
    }
}
