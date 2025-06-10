package net.sinedkadis.terracompositio.block.custom;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.sinedkadis.terracompositio.registries.TCBlocks;
import net.sinedkadis.terracompositio.worldgen.tree.FlowCedarTreeGrower;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FlowCedarSaplingBlock extends SaplingBlock {
    public FlowCedarSaplingBlock(FlowCedarTreeGrower flowCedarTreeGrower, BlockBehaviour.Properties copy) {
        super(flowCedarTreeGrower,copy);
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        ItemStack itemInHand = pPlayer.getItemInHand(pHand);
        if (itemInHand.is(Items.BONE_MEAL)){
            BlockState blockState = pLevel.getBlockState(pPos.above());
            if (blockState.is(TCBlocks.FLOW_CEDAR_TANK.get())){
                pLevel.setBlockAndUpdate(pPos, TCBlocks.FLOW_CEDAR_PEDESTAL.get().defaultBlockState());
                pLevel.setBlockAndUpdate(pPos.above(),blockState.setValue(FlowCedarTankBlock.STAGE,4));
                itemInHand.shrink(1);
                spawnFertilizeParticles(pLevel,pPos,10);
                playFertilizeSound(pLevel,pPos);
                return InteractionResult.SUCCESS;
            }
        }
        return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
    }

    public static void playFertilizeSound(Level level, BlockPos pos) {
        if (!level.isClientSide) {
            level.playSound(
                    null,
                    pos.getX() + 0.5,
                    pos.getY() + 0.5,
                    pos.getZ() + 0.5,
                    SoundEvents.COMPOSTER_READY,
                    SoundSource.BLOCKS,
                    1.0F,
                    level.random.nextFloat() * 0.1F + 0.9F
            );
        }
    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pMovedByPiston) {
        super.onRemove(pState, pLevel, pPos, pNewState, pMovedByPiston);
        BlockState blockState = pLevel.getBlockState(pPos.above());
        if (blockState.hasProperty(FlowCedarTankBlock.STAGE)){
            pLevel.setBlockAndUpdate(pPos.above(),blockState.setValue(STAGE,3));
        }
    }

    public static void spawnFertilizeParticles(Level level, BlockPos pos, int count) {
        if (level.isClientSide) return; // Выполняем только на сервере

        Vec3 center = Vec3.atCenterOf(pos); // Центр блока
        double radius = 0.5; // Радиус разброса частиц

        // Отправляем пакет частиц всем игрокам в радиусе
        ((ServerLevel)level).sendParticles(
                ParticleTypes.HAPPY_VILLAGER, // Тип частиц
                center.x, center.y + 0.5, center.z, // Позиция (центр блока + немного выше)
                count, // Количество частиц
                radius, radius, radius, // Разброс по осям XYZ
                0.1 // Базовая скорость
        );
    }
}
