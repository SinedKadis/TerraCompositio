package net.sinedkadis.terracompositio.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.sinedkadis.terracompositio.cfe.CFESource;

public class CreativeCFESourceBlockEntity extends ModBlockEntity implements CFESource {
    public CreativeCFESourceBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.CREATIVE_CFE_SOURCE_BE.get(),pPos, pBlockState);
    }

    public void tick(Level pLevel, BlockPos pPos, BlockState pState) {

    }

    @Override
    public Level getCFESourceLevel() {
        return getLevel();
    }

    @Override
    public BlockPos getCFESourceBlockPos() {
        return getBlockPos();
    }

    @Override
    public int getCurrentCFE() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void takeCFE(int cfe) {

    }
}
