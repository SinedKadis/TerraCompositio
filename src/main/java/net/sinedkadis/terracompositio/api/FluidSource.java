package net.sinedkadis.terracompositio.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.capability.IFluidHandler;


public interface FluidSource {
    Level getLevel();
    BlockPos getBlockPos();
    int getPriority();
    IFluidHandler getFluidHandler();
}
