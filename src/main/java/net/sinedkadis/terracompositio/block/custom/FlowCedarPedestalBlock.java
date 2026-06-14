package net.sinedkadis.terracompositio.block.custom;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.PlantType;
import net.sinedkadis.terracompositio.registries.TCBlocks;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FlowCedarPedestalBlock extends Block implements IPlantable {
    public FlowCedarPedestalBlock(Properties pProperties) {
        super(pProperties);
    }

    @SuppressWarnings("deprecation")
    public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
        BlockPos blockpos = pPos.below();
        return pState.getBlock() == this ? pLevel.getBlockState(blockpos).canSustainPlant(pLevel, blockpos, Direction.UP, this) : pState.is(BlockTags.DIRT) || pState.is(Blocks.FARMLAND);
    }

    @SuppressWarnings("deprecation")
    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        ItemStack itemInHand = pPlayer.getItemInHand(pHand);
        if (itemInHand.is(Items.BONE_MEAL)) {
            BlockState blockState = pLevel.getBlockState(pPos.above());
            if (blockState.is(TCBlocks.FLOW_CEDAR_TANK.get()) && blockState.getValue(FlowCedarTankBlock.STAGE).equals(3)) {
                pLevel.setBlockAndUpdate(pPos, TCBlocks.FLOW_CEDAR_PEDESTAL.get().defaultBlockState());
                pLevel.setBlockAndUpdate(pPos.above(), blockState.setValue(FlowCedarTankBlock.STAGE, 4));
                itemInHand.shrink(1);
                FlowCedarSaplingBlock.spawnFertilizeParticles(pLevel, pPos, 10);
                FlowCedarSaplingBlock.playFertilizeSound(pLevel, pPos);
                return InteractionResult.SUCCESS;
            }
        }
        return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
        return true;
    }

    @Override
    public BlockState getPlant(BlockGetter world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        return state.getBlock() != this ? this.defaultBlockState() : state;
    }

    @Override
    public PlantType getPlantType(BlockGetter level, BlockPos pos) {
        return PlantType.PLAINS;
    }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, @Nullable Direction direction) {
        return true;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pMovedByPiston) {
        super.onRemove(pState, pLevel, pPos, pNewState, pMovedByPiston);
        BlockState blockState = pLevel.getBlockState(pPos.above());
        if (!pState.equals(pNewState) && blockState.hasProperty(FlowCedarTankBlock.STAGE)) {
            pLevel.setBlockAndUpdate(pPos.above(), blockState.setValue(FlowCedarTankBlock.STAGE, 3));
        }
    }
}
