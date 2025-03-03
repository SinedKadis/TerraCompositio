package net.sinedkadis.terracompositio.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.sinedkadis.terracompositio.block.ModBlocks;
import net.sinedkadis.terracompositio.block.entity.ModBlockEntities;
import net.sinedkadis.terracompositio.item.ModItems;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.AXIS;



public class MatterInfuserPortBlock extends ModIOBaseEntityBlock {
    private final static DirectionProperty FACING;
    private final static IntegerProperty STAGE;
    protected static final VoxelShape NORTH_AABB;
    protected static final VoxelShape SOUTH_AABB;
    protected static final VoxelShape WEST_AABB;
    protected static final VoxelShape EAST_AABB;

    public MatterInfuserPortBlock(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.EAST).setValue(STAGE,0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING,STAGE);
    }

    @Override
    public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
        Direction direction = pState.getValue(FACING);
        BlockPos blockpos = pPos.relative(direction.getOpposite());
        BlockState blockstate = pLevel.getBlockState(blockpos);
        if (blockstate.hasProperty(AXIS))
            return direction.getAxis().isHorizontal() && blockstate.getValue(AXIS).equals(
                switch (direction) {
                    case NORTH, SOUTH -> Direction.Axis.X;
                    case WEST, EAST -> Direction.Axis.Z;
                    default -> Direction.Axis.Y;
                });
        return false;
    }

    public VoxelShape getShape(BlockState pState, @NotNull BlockGetter pLevel, @NotNull BlockPos pPos, @NotNull CollisionContext pContext) {
        return switch (pState.getValue(FACING)) {
            default -> EAST_AABB;
            case WEST -> WEST_AABB;
            case SOUTH -> SOUTH_AABB;
            case NORTH -> NORTH_AABB;
        };
    }



    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        BlockState blockstate = this.defaultBlockState().setValue(STAGE,0);
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
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
        Direction direction = pState.getValue(FACING);
        BlockPos blockpos = pPos.relative(direction.getOpposite());
        BlockState blockstate = pLevel.getBlockState(blockpos);
        if (blockstate.hasProperty(FlowCedarCasingBlock.PARTS) && pState.getBlock() != pNewState.getBlock()) {
            if (blockstate.getValue(FlowCedarCasingBlock.PARTS) == pState.getValue(FACING) || (blockstate.getValue(FlowCedarCasingBlock.PARTS) == Direction.UP)) {
                pLevel.setBlock(blockpos, blockstate.setValue(FlowCedarCasingBlock.PARTS, Direction.UP), 3);
                if (blockstate.getValue(FlowCedarCasingBlock.PARTS) != Direction.UP) {
                    pLevel.addFreshEntity(new ItemEntity(pLevel, pPos.getX(), pPos.getY(), pPos.getZ(), new ItemStack(ModItems.INFUSED_IRON_ROD.get())));
                }
                return;
            }
            if (blockstate.getValue(FlowCedarCasingBlock.PARTS) == pState.getValue(FACING).getOpposite())
                return;
            if (blockstate.hasProperty(FlowCedarCasingBlock.PARTS))
                pLevel.setBlock(blockpos, blockstate.setValue(FlowCedarCasingBlock.PARTS, Direction.DOWN), 3);
        }
        if (pState.getValue(STAGE).equals(2)) {
            pLevel.addFreshEntity(new ItemEntity(pLevel, pPos.getX(), pPos.getY(), pPos.getZ(), new ItemStack(ModItems.INFUSED_IRON_ROD.get(), 2)));
        }
    }

    @Override
    public @NotNull InteractionResult use(@NotNull BlockState pState, Level pLevel, @NotNull BlockPos pPos, @NotNull Player pPlayer, @NotNull InteractionHand pHand, @NotNull BlockHitResult pHit) {
        ItemStack item = pPlayer.getItemInHand(pHand);
        BlockPos casingPos = pPos.relative(pState.getValue(FACING).getOpposite());
        BlockState casingState = pLevel.getBlockState(casingPos);
        int stage = pState.getValue(STAGE);
        if (item.is(ModItems.INFUSED_IRON_ROD.get())){
            if (stage == 0 && casingState.is(ModBlocks.FLOW_CEDAR_CASING.get()) && casingState.getValue(FlowCedarCasingBlock.PARTS).equals(Direction.UP)){
                pLevel.setBlock(pPos,pState.setValue(STAGE,1),3);
                pLevel.setBlock(casingPos,casingState.setValue(FlowCedarCasingBlock.PARTS, pState.getValue(FACING)),3);
                item.shrink(1);
                return InteractionResult.sidedSuccess(pLevel.isClientSide);
            } else if (stage == 1) {
                if (item.getCount() >= 2){
                    pLevel.setBlock(pPos,pState.setValue(STAGE,2),3);
                    item.shrink(2);
                    return InteractionResult.sidedSuccess(pLevel.isClientSide);
                }
                return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
            }
            return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
        }
        return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
        return Objects.requireNonNull(ModBlockEntities.MATTER_INFUSER_PORT_BE.get().create(blockPos, blockState)).markVirtual();
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level pLevel, @NotNull BlockState pState, @NotNull BlockEntityType<T> pBlockEntityType) {
        if (pLevel.isClientSide()) {
            return null;
        }
        return createTickerHelper(pBlockEntityType, ModBlockEntities.MATTER_INFUSER_PORT_BE.get(),
                (pLevel1, pPos, pState1, pBlockEntity) -> pBlockEntity.tick(pLevel1,pPos,pState1));
    }

    static {
        FACING = BlockStateProperties.HORIZONTAL_FACING;
        STAGE = IntegerProperty.create("stage",0,2);
        SOUTH_AABB = Block.box(3, 3, 0, 13, 13, 1);
        NORTH_AABB = Block.box(3, 3, 15, 13, 13, 16);
        EAST_AABB = Block.box(0, 3, 3, 1, 13, 13);
        WEST_AABB = Block.box(15, 3, 3, 16, 13, 13);
    }
}
