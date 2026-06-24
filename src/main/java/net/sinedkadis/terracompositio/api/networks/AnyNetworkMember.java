package net.sinedkadis.terracompositio.api.networks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface AnyNetworkMember {
    //World data getters
    <T> T getEntity();

    Level getLevel();

    BlockPos getPos();

    //Filter values
    int getRange();

    int getPriority();
}
