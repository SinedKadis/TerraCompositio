package net.sinedkadis.terracompositio.block.entity;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.EmptyHandler;
import net.sinedkadis.terracompositio.registries.TCBlockEntities;
import net.sinedkadis.terracompositio.util.behaviors.blockentity.IBEBehaviour;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MatterInfuserPortBlockEntity extends MatterInfuserBaseBlockEntity {
    public MatterInfuserPortBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(TCBlockEntities.MATTER_INFUSER_PORT_BE.get(),pPos, pBlockState);
    }

    @Override
    protected IItemHandlerModifiable getItemHandler() {
        FlowCedarCasingBlockEntity casingBE = getCasingBE();
        if (casingBE != null) {
            return casingBE.getItemHandler();
        }
        return ((IItemHandlerModifiable) EmptyHandler.INSTANCE);
    }

    @Override
    public void addBEBehaviours(List<IBEBehaviour> list) {

    }

    int timer = 0;
    @Override
    public void tick(Level pLevel, BlockPos pPos, BlockState pState) {
        super.tick(pLevel, pPos, pState);
        if (timer <= 0) {
            timer = 5;
            playSoundIfNeeded(pLevel, pPos);
        }
        --timer;
    }

    @Override
    protected void playSoundIfNeeded(Level level, BlockPos pos) {
        Direction direction = getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING).getCounterClockWise();
        for (BlockPos blockpos : BlockPos.betweenClosed(pos.relative(direction),pos.relative(direction,8))) {
            BlockEntity blockEntity = level.getBlockEntity(blockpos);
            if (blockEntity instanceof MatterInfuserUnitBlockEntity unitBlockEntity) {
                if (unitBlockEntity.progress > 0) {
                    level.playSound(null, blockpos, SoundEvents.AZALEA_STEP, SoundSource.BLOCKS);
                    return;
                }
            }
        }
    }

    @Override
    protected int getECF() {
        return 0;
    }
}
