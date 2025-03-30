package net.sinedkadis.terracompositio.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.sinedkadis.terracompositio.registries.ModBlockEntities;
import net.sinedkadis.terracompositio.registries.ModBlockStateProperties;
import net.sinedkadis.terracompositio.registries.ModBlocks;
import net.sinedkadis.terracompositio.registries.ModItems;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.sinedkadis.terracompositio.block.custom.FlowCedarCasingBlock.*;

public class MatterInfuserIOBlock extends MatterInfuserBaseBaseEntityBlock{
    private final static BooleanProperty UP_CONNECTION;
    private final static BooleanProperty RIGHT_CONNECTION;
    private final static BooleanProperty DOWN_CONNECTION;
    private final static BooleanProperty LEFT_CONNECTION;
    protected static final VoxelShape NORTH_AABB;
    protected static final VoxelShape SOUTH_AABB;
    protected static final VoxelShape WEST_AABB;
    protected static final VoxelShape EAST_AABB;

    public MatterInfuserIOBlock(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(this.defaultBlockState()
                .setValue(UP_CONNECTION,false)
                .setValue(RIGHT_CONNECTION,false)
                .setValue(DOWN_CONNECTION,false)
                .setValue(LEFT_CONNECTION,false));

    }

    @Override
    public void onRemove(@NotNull BlockState pState, @NotNull Level pLevel, @NotNull BlockPos pPos, @NotNull BlockState pNewState, boolean pIsMoving) {
        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
        if (pState.getBlock() != pNewState.getBlock()) {
            if (pState.getValue(RIGHT_CONNECTION)) {
                pLevel.addFreshEntity(new ItemEntity(pLevel, pPos.getX(), pPos.getY(), pPos.getZ(), new ItemStack(ModItems.INFUSED_IRON_ROD.get(), 2)));
                BlockPos rightPos = pPos.relative(pState.getValue(FACING).getCounterClockWise());
                pLevel.setBlock(rightPos,pLevel.getBlockState(rightPos).setValue(LEFT_CONNECTION,false),3);
            }
            if (pState.getValue(LEFT_CONNECTION)) {
                pLevel.addFreshEntity(new ItemEntity(pLevel, pPos.getX(), pPos.getY(), pPos.getZ(), new ItemStack(ModItems.INFUSED_IRON_ROD.get(), 2)));
                BlockPos leftPos = pPos.relative(pState.getValue(FACING).getClockWise());
                pLevel.setBlock(leftPos,pLevel.getBlockState(leftPos).setValue(RIGHT_CONNECTION,false),3);
            }
        }
    }

    @Override
    public @NotNull InteractionResult use(@NotNull BlockState pState, Level pLevel, @NotNull BlockPos pPos, @NotNull Player pPlayer, @NotNull InteractionHand pHand, @NotNull BlockHitResult pHit) {
        ItemStack item = pPlayer.getItemInHand(pHand);
        Direction facing = pState.getValue(FACING);
        BlockPos casingPos = pPos.relative(facing.getOpposite());
        BlockState casingState = pLevel.getBlockState(casingPos);
        BlockPos rightPos = pPos.relative(facing.getCounterClockWise());
        BlockState rightState = pLevel.getBlockState(rightPos);
        BlockPos leftPos = pPos.relative(facing.getClockWise());
        BlockState leftState = pLevel.getBlockState(leftPos);
        Block casing = ModBlocks.FLOW_CEDAR_CASING.get();
        Block io = ModBlocks.MATTER_INFUSER_IO.get();
        if (item.is(ModItems.INFUSED_IRON_ROD.get())){
            if (!pState.getValue(UP_CONNECTION) && casingState.is(casing) && hasInputBus(casingState)){
                pLevel.setBlock(pPos,pState.setValue(UP_CONNECTION,true),3);
                pLevel.setBlock(casingPos,casingState.setValue(INPUT_BUS_CONNECTION, true),3);
                item.shrink(1);
                return InteractionResult.sidedSuccess(pLevel.isClientSide);
            }
            if (!pState.getValue(DOWN_CONNECTION) && casingState.is(casing) && hasOutputBus(casingState)) {
                pLevel.setBlock(pPos,pState.setValue(DOWN_CONNECTION,true),3);
                pLevel.setBlock(casingPos,casingState.setValue(OUTPUT_BUS_CONNECTION, true),3);
                item.shrink(1);
                return InteractionResult.sidedSuccess(pLevel.isClientSide);
            }
            if (!pState.getValue(RIGHT_CONNECTION) && rightState.is(io)){
                if (item.getCount() >= 2){
                    pLevel.setBlock(pPos,pState.setValue(RIGHT_CONNECTION,true),3);
                    pLevel.setBlock(rightPos,rightState.setValue(LEFT_CONNECTION, true),3);
                    item.shrink(2);
                    return InteractionResult.sidedSuccess(pLevel.isClientSide);
                }
            }
            if (!pState.getValue(LEFT_CONNECTION)) {
                if (leftState.is(io) || leftState.is(ModBlocks.MATTER_INFUSER_PORT.get())) {
                    if (item.getCount() >= 2) {
                        pLevel.setBlock(pPos, pState.setValue(LEFT_CONNECTION, true), 3);
                        pLevel.setBlock(leftPos, leftState.setValue(RIGHT_CONNECTION, true), 3);
                        item.shrink(2);
                        return InteractionResult.sidedSuccess(pLevel.isClientSide);
                    }
                }
            }
        }
        return InteractionResult.PASS;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
        return ModBlockEntities.MATTER_INFUSER_IO_BE.get().create(blockPos, blockState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level pLevel, @NotNull BlockState pState, @NotNull BlockEntityType<T> pBlockEntityType) {
        if (pLevel.isClientSide()) {
            return null;
        }
        return createTickerHelper(pBlockEntityType, ModBlockEntities.MATTER_INFUSER_IO_BE.get(),
                (pLevel1, pPos, pState1, pBlockEntity) -> pBlockEntity.tick(pLevel1,pPos,pState1));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING,UP_CONNECTION,RIGHT_CONNECTION,DOWN_CONNECTION,LEFT_CONNECTION);
    }
    @Override
    public @NotNull VoxelShape getShape(BlockState pState, @NotNull BlockGetter pLevel, @NotNull BlockPos pPos, @NotNull CollisionContext pContext) {
        return switch (pState.getValue(FACING)) {
            default -> EAST_AABB;
            case WEST -> WEST_AABB;
            case SOUTH -> SOUTH_AABB;
            case NORTH -> NORTH_AABB;
        };
    }

    static {
        UP_CONNECTION = ModBlockStateProperties.UP_CONNECTION;
        RIGHT_CONNECTION = ModBlockStateProperties.RIGHT_CONNECTION;
        DOWN_CONNECTION = ModBlockStateProperties.DOWN_CONNECTION;
        LEFT_CONNECTION = ModBlockStateProperties.LEFT_CONNECTION;
        SOUTH_AABB = Block.box(4, 3, 0, 12, 13, 1);
        NORTH_AABB = Block.box(4, 3, 15, 12, 13, 16);
        EAST_AABB = Block.box(0, 3, 4, 1, 13, 12);
        WEST_AABB = Block.box(15, 3, 4, 16, 13, 12);
    }
}
