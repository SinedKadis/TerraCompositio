package net.sinedkadis.terracompositio.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.sinedkadis.terracompositio.block.custom.CFESaturatedAirBlock;
import net.sinedkadis.terracompositio.registries.TCBlockEntities;
import net.sinedkadis.terracompositio.registries.TCBlockStateProperties;

public class AirSaturatorBlockEntity extends TCCFEBlockEntity{
    public AirSaturatorBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.AIR_SATURATOR_BE.get(), pos, state, 100, 5, BlockMode.CONSUMER);
    }

    private boolean wasActivated = false;
    private int timer = 0;
    @Override
    public void tick(Level pLevel, BlockPos pPos, BlockState pState) {
        super.tick(pLevel, pPos, pState);
        boolean isActivated = pLevel.hasNeighborSignal(pPos);
        if (isActivated && !wasActivated) {
            scheduleMemberUpdate();
        }
        int cfe = cfeContainer.getCFE();
        if (isActivated && cfe > 0) {
            BlockPos toPlace = pPos.relative(pState.getValue(BlockStateProperties.FACING));
            if (!pLevel.getBlockState(toPlace).isAir()) return;
            if (pState.getValue(TCBlockStateProperties.INFUSED)) {
                cfe = CFESaturatedAirBlock.placeCFECloud(pLevel, toPlace, cfe);
                cfeContainer.takeCFE(cfe,false);
                scheduleMemberUpdate();
                pLevel.playSound(null,toPlace, SoundEvents.WOOL_PLACE, SoundSource.BLOCKS,0.5f,1f);
            } else if (timer <= 0){
                int toSaturate = cfeContainer.takeCFE(10,true);
                toSaturate = CFESaturatedAirBlock.placeCFECloud(pLevel, toPlace, toSaturate);
                cfeContainer.takeCFE(toSaturate,false);
                scheduleMemberUpdate();
                timer = 20;
                pLevel.playSound(null,toPlace, SoundEvents.WOOL_PLACE, SoundSource.BLOCKS,0.5f,1f);
            } else timer--;
        }
        wasActivated = isActivated;
    }
}
