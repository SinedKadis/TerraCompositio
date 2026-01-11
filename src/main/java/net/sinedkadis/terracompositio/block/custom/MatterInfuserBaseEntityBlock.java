package net.sinedkadis.terracompositio.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.sinedkadis.terracompositio.api.behaviors.blockentity.IBEItemBehaviour;
import net.sinedkadis.terracompositio.block.entity.FlowCedarCasingBlockEntity;
import net.sinedkadis.terracompositio.registries.TCBlocks;
import net.sinedkadis.terracompositio.util.FunctionSide;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.AXIS;
import static net.sinedkadis.terracompositio.block.custom.FlowCedarCasingBlock.FUNCTION_SIDE;

@SuppressWarnings("deprecation")
public abstract class MatterInfuserBaseEntityBlock extends TCBaseEntityBlock {
    protected final static DirectionProperty FACING;
    protected static final VoxelShape NORTH_AABB;
    protected static final VoxelShape SOUTH_AABB;
    protected static final VoxelShape WEST_AABB;
    protected static final VoxelShape EAST_AABB;

    public MatterInfuserBaseEntityBlock(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.EAST));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING);
    }

    @Override
    public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
        Direction direction = pState.getValue(FACING);
        BlockPos blockpos = pPos.relative(direction.getOpposite());
        BlockState blockstate = pLevel.getBlockState(blockpos);
        if (blockstate.hasProperty(AXIS) && blockstate.is(TCBlocks.FLOW_CEDAR_CASING.get()))
            return direction.getAxis().isHorizontal() && blockstate.getValue(AXIS).equals(
                    switch (direction) {
                        case NORTH, SOUTH -> Direction.Axis.X;
                        case WEST, EAST -> Direction.Axis.Z;
                        default -> Direction.Axis.Y;
                    });
        return false;
    }

    @Override
    public void onRemove(@NotNull BlockState pState, @NotNull Level pLevel, @NotNull BlockPos pPos, @NotNull BlockState pNewState, boolean pIsMoving) {
        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
        if (pState.getBlock() != pNewState.getBlock()) {
            Direction direction = pState.getValue(FACING);
            BlockPos blockpos = pPos.relative(direction.getOpposite());
            BlockState blockstate = pLevel.getBlockState(blockpos);
            if (blockstate.is(TCBlocks.FLOW_CEDAR_CASING.get())) {
                FlowCedarCasingBlockEntity blockEntity = (FlowCedarCasingBlockEntity) Objects.requireNonNull(pLevel.getBlockEntity(blockpos));
                //noinspection OptionalGetWithoutIsPresent
                IBEItemBehaviour iItemBehaviour = blockEntity.getItemBehaviour().get();
                iItemBehaviour.drops();
                iItemBehaviour.getItemHandler().setSize(slotCount());
            }
        }
    }

    public @NotNull VoxelShape getShape(BlockState pState, @NotNull BlockGetter pLevel, @NotNull BlockPos pPos, @NotNull CollisionContext pContext) {
        return switch (pState.getValue(FACING)) {
            case WEST -> WEST_AABB;
            case SOUTH -> SOUTH_AABB;
            case NORTH -> NORTH_AABB;
            default -> EAST_AABB;
        };
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        BlockState blockstate = this.defaultBlockState();
        LevelReader levelreader = pContext.getLevel();
        BlockPos blockpos = pContext.getClickedPos();
        Direction[] adirection = pContext.getNearestLookingDirections();
        for (Direction direction : adirection) {
            if (direction.getAxis().isHorizontal()) {
                Direction direction1 = direction.getOpposite();
                blockstate = blockstate.setValue(FACING, direction1);
                if (blockstate.canSurvive(levelreader, blockpos)) {
                    return blockstate;
                }
            }
        }

        return null;
    }



    @Override
    public void setPlacedBy(@NotNull Level pLevel, @NotNull BlockPos pPos, @NotNull BlockState pState, @Nullable LivingEntity pPlacer, @NotNull ItemStack pStack) {
        super.setPlacedBy(pLevel, pPos, pState, pPlacer, pStack);
        Direction direction = pState.getValue(FACING);
        BlockPos casingPos = pPos.relative(direction.getOpposite());
        BlockState casingState = pLevel.getBlockState(casingPos);
        if (casingState.is(TCBlocks.FLOW_CEDAR_CASING.get())) {
            FunctionSide functionSideByDirection = FunctionSide.getFunctionSideByDirection(casingState, direction);
            pLevel.setBlock(casingPos, casingState.setValue(FUNCTION_SIDE, functionSideByDirection), 3);
            FlowCedarCasingBlockEntity blockEntity = (FlowCedarCasingBlockEntity) pLevel.getBlockEntity(casingPos);
            if (blockEntity != null) {
                //noinspection OptionalGetWithoutIsPresent
                IBEItemBehaviour iItemBehaviour = blockEntity.getItemBehaviour().get();
                iItemBehaviour.drops();
                iItemBehaviour.getItemHandler().setSize(slotCount());
            }
        }
    }

    protected int slotCount() {
        return 2;
    }

    static {
         FACING = BlockStateProperties.HORIZONTAL_FACING;
         SOUTH_AABB = Block.box(3, 3, 0, 13, 13, 1);
         NORTH_AABB = Block.box(3, 3, 15, 13, 13, 16);
         EAST_AABB = Block.box(0, 3, 3, 1, 13, 13);
         WEST_AABB = Block.box(15, 3, 3, 16, 13, 13);
    }
}
