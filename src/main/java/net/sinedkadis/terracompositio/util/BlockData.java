package net.sinedkadis.terracompositio.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public record BlockData(BlockPos blockPos, Level level) {
}