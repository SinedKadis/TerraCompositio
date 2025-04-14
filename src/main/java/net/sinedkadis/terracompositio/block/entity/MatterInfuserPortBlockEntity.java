package net.sinedkadis.terracompositio.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.sinedkadis.terracompositio.registries.ModBlockEntities;


public class MatterInfuserPortBlockEntity extends MatterInfuserBaseBlockEntity {
    public MatterInfuserPortBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.MATTER_INFUSER_PORT_BE.get(),pPos, pBlockState);
    }
}
