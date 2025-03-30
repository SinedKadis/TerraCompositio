package net.sinedkadis.terracompositio.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.sinedkadis.terracompositio.registries.ModBlockEntities;

public class MatterInfuserIOBlockEntity extends MatterInfuserBaseBlockEntity{
    public MatterInfuserIOBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MATTER_INFUSER_IO_BE.get(), pos, state, 100, 10);
    }
}
