package net.sinedkadis.terracompositio.api.cfe;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;


public interface CFESource {
    Level getCFESourceLevel();
    BlockPos getCFESourceBlockPos();
    int getCurrentCFE();
    void takeCFE(int cfe);
}
