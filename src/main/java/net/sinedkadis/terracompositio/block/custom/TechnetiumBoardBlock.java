package net.sinedkadis.terracompositio.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.sinedkadis.terracompositio.registries.TCBlocks;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
public class TechnetiumBoardBlock extends Block implements SimpleWaterloggedBlock {
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public TechnetiumBoardBlock(Properties pProperties) {
        super(pProperties);
        registerDefaultState(defaultBlockState().setValue(WATERLOGGED,false));
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(WATERLOGGED);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void tick(BlockState pState, ServerLevel level, BlockPos pPos, RandomSource pRandom) {
        super.tick(pState, level, pPos, pRandom);
        List<Entity> entities = level.getEntities(null, new AABB(
                pPos.above().relative(Direction.EAST).relative(Direction.SOUTH),
                pPos.above(2).relative(Direction.WEST).relative(Direction.NORTH)));
        if (entities.isEmpty()) {
            BlockState replaceState =
                    Blocks.AIR.defaultBlockState();
            level.setBlockAndUpdate(pPos, replaceState);
        } else {
            level.scheduleTick(pPos, TCBlocks.TECHNETIUM_BLOCK.get(), 100);
        }
    }


    @SuppressWarnings("deprecation")
    public @NotNull BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
        if (pState.getValue(WATERLOGGED)) {
            pLevel.scheduleTick(pCurrentPos, Fluids.WATER, Fluids.WATER.getTickDelay(pLevel));
        }

        return super.updateShape(pState,pFacing,pFacingState,pLevel,pCurrentPos,pFacingPos);
    }

    @SuppressWarnings("deprecation")
    public @NotNull FluidState getFluidState(BlockState pState) {
        return pState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(pState);
    }
    @SuppressWarnings("deprecation")
    @Override
    public @NotNull VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return Block.box(0,12,0,16,16,16);
    }

    @Override
    public void fallOn(Level pLevel, BlockState pState, BlockPos pPos, Entity pEntity, float pFallDistance) {

    }
}
