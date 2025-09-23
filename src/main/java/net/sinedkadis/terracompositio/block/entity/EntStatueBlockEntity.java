package net.sinedkadis.terracompositio.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.sinedkadis.terracompositio.registries.TCBlockEntities;

public class EntStatueBlockEntity extends TCItemIOCFEBlockEntity{
    public EntStatueBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.ENT_STATUE_BE.get(), pos, state, BlockMode.NONE);
    }

    @Override
    public int getContainerSize() {
        return 1;
    }
}
