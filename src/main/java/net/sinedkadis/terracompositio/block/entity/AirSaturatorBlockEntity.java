package net.sinedkadis.terracompositio.block.entity;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.sinedkadis.terracompositio.api.behaviors.blockentity.IBEBehaviour;
import net.sinedkadis.terracompositio.api.networks.cfe.ICFEHandler;
import net.sinedkadis.terracompositio.block.IFluidApplicable;
import net.sinedkadis.terracompositio.block.behaviours.CFEHandlerBehaviour;
import net.sinedkadis.terracompositio.config.TCInnerConfig;
import net.sinedkadis.terracompositio.registries.TCBlockEntities;
import net.sinedkadis.terracompositio.registries.TCBlockStateProperties;
import net.sinedkadis.terracompositio.util.helpers.CFEHelper;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AirSaturatorBlockEntity extends TCBlockEntity implements IFluidApplicable{
    public AirSaturatorBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.AIR_SATURATOR_BE.get(), pos, state);
    }

    private boolean wasActivated = false;
    private int timer = 0;

    @Override
    public void addBEBehaviours(List<IBEBehaviour> list) {
        list.add(new CFEHandlerBehaviour(this)
                .maxCFE(100)
                .priority(TCInnerConfig.DEFAULT_CONSUMER_PRIORITY)
                .range(5));

    }

    @Override
    public void tick(Level pLevel, BlockPos pPos, BlockState pState) {
        super.tick(pLevel, pPos, pState);
        boolean isActivated = pLevel.hasNeighborSignal(pPos);
        if (isActivated && !wasActivated) {
            scheduleMemberUpdate();
        }
        int cfe = cfeContainer().getCFE();
        if (isActivated && cfe > 0) {
            BlockPos toPlace = pPos.relative(pState.getValue(BlockStateProperties.FACING));
            if (!pLevel.getBlockState(toPlace).isAir()) return;
            if (pState.getValue(TCBlockStateProperties.INFUSED)) {
                CFEHelper.placeCFECloud(pLevel, toPlace, cfe);
                cfeContainer().takeCFE(cfe,false);
                scheduleMemberUpdate();
                pLevel.playSound(null,toPlace, SoundEvents.WOOL_PLACE, SoundSource.BLOCKS,0.5f,1f);
            } else if (timer <= 0){
                int toSaturate = cfeContainer().takeCFE(10,true);
                CFEHelper.placeCFECloud(pLevel, toPlace, toSaturate);
                cfeContainer().takeCFE(toSaturate,false);
                scheduleMemberUpdate();
                timer = 20;
                pLevel.playSound(null,toPlace, SoundEvents.WOOL_PLACE, SoundSource.BLOCKS,0.5f,1f);
            } else timer--;
        }
        wasActivated = isActivated;
    }

    private ICFEHandler cfeContainer() {
        return ((CFEHandlerBehaviour) behaviours.get(0)).getMainHandler();
    }

    private void scheduleMemberUpdate() {
        ((CFEHandlerBehaviour) behaviours.get(0)).scheduleMemberUpdate();
    }

}
