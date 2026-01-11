package net.sinedkadis.terracompositio.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.sinedkadis.terracompositio.block.entity.TCBlockEntity;
import net.sinedkadis.terracompositio.registries.TCBlockEntities;
import net.sinedkadis.terracompositio.registries.TCBlockStateProperties;
import net.sinedkadis.terracompositio.registries.TCBlocks;
import net.sinedkadis.terracompositio.registries.TCItems;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;
import static net.sinedkadis.terracompositio.block.custom.FlowCedarCasingBlock.*;

public class MatterInfuserIOBlock extends MatterInfuserBaseEntityBlock {
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
                BlockPos rightPos = pPos.relative(pState.getValue(FACING).getCounterClockWise());
                pLevel.setBlock(rightPos,pLevel.getBlockState(rightPos).setValue(LEFT_CONNECTION,false),3);
            }
            if (pState.getValue(LEFT_CONNECTION)) {
                BlockPos leftPos = pPos.relative(pState.getValue(FACING).getClockWise());
                pLevel.setBlock(leftPos,pLevel.getBlockState(leftPos).setValue(RIGHT_CONNECTION,false),3);
            }
            BlockPos blockpos = pPos.relative(pState.getValue(HORIZONTAL_FACING).getOpposite());
            BlockState blockState = pLevel.getBlockState(blockpos);
            if (pState.getValue(UP_CONNECTION)) {
                if (blockState.is(TCBlocks.FLOW_CEDAR_CASING.get())) {
                    pLevel.setBlock(blockpos, blockState.setValue(INPUT_BUS_CONNECTION,false),3);
                }
            }
            if (pState.getValue(DOWN_CONNECTION)) {
                if (blockState.is(TCBlocks.FLOW_CEDAR_CASING.get())) {
                    pLevel.setBlock(blockpos, blockState.setValue(OUTPUT_BUS_CONNECTION, false), 3);
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public @NotNull List<ItemStack> getDrops(@NotNull BlockState pState, LootParams.@NotNull Builder pParams) {
        List<ItemStack> drops = super.getDrops(pState, pParams);
        if (pState.getValue(RIGHT_CONNECTION)) {
            drops.add(new ItemStack(TCItems.INFUSED_IRON_ROD.get(), 2));
        }
        if (pState.getValue(LEFT_CONNECTION)) {
            drops.add(new ItemStack(TCItems.INFUSED_IRON_ROD.get(), 2));
        }
        if (pState.getValue(UP_CONNECTION)) {
            drops.add(TCItems.INFUSED_IRON_ROD.get().getDefaultInstance());
        }
        if (pState.getValue(DOWN_CONNECTION)) {
            drops.add(TCItems.INFUSED_IRON_ROD.get().getDefaultInstance());
        }
        return drops;
    }

    @Override
    public @NotNull InteractionResult use(@NotNull BlockState pState, @NotNull Level pLevel, @NotNull BlockPos pPos, @NotNull Player pPlayer, @NotNull InteractionHand pHand, @NotNull BlockHitResult pHit) {
        ItemStack item = pPlayer.getItemInHand(pHand);
        Direction facing = pState.getValue(FACING);
        BlockPos casingPos = pPos.relative(facing.getOpposite());
        BlockState casingState = pLevel.getBlockState(casingPos);
        BlockPos rightPos = pPos.relative(facing.getCounterClockWise());
        BlockState rightState = pLevel.getBlockState(rightPos);
        BlockPos leftPos = pPos.relative(facing.getClockWise());
        BlockState leftState = pLevel.getBlockState(leftPos);
        Block casing = TCBlocks.FLOW_CEDAR_CASING.get();
        Block io = TCBlocks.MATTER_INFUSER_IO.get();
        if (item.is(TCItems.INFUSED_IRON_ROD.get())){
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
                if (leftState.is(io) || leftState.is(TCBlocks.MATTER_INFUSER_PORT.get())) {
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

    @Override
    protected @NotNull BlockEntityType<? extends TCBlockEntity> getBlockEntityType() {
        return TCBlockEntities.MATTER_INFUSER_IO_BE.get();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING,UP_CONNECTION,RIGHT_CONNECTION,DOWN_CONNECTION,LEFT_CONNECTION);
    }
    @Override
    public @NotNull VoxelShape getShape(BlockState pState, @NotNull BlockGetter pLevel, @NotNull BlockPos pPos, @NotNull CollisionContext pContext) {
        return switch (pState.getValue(FACING)) {
            case WEST -> WEST_AABB;
            case SOUTH -> SOUTH_AABB;
            case NORTH -> NORTH_AABB;
            default -> EAST_AABB;
        };
    }

    static {
        UP_CONNECTION = TCBlockStateProperties.UP_CONNECTION;
        RIGHT_CONNECTION = TCBlockStateProperties.RIGHT_CONNECTION;
        DOWN_CONNECTION = TCBlockStateProperties.DOWN_CONNECTION;
        LEFT_CONNECTION = TCBlockStateProperties.LEFT_CONNECTION;
        SOUTH_AABB = Block.box(4, 3, 0, 12, 13, 1);
        NORTH_AABB = Block.box(4, 3, 15, 12, 13, 16);
        EAST_AABB = Block.box(0, 3, 4, 1, 13, 12);
        WEST_AABB = Block.box(15, 3, 4, 16, 13, 12);
    }
}
