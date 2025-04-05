package net.sinedkadis.terracompositio.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.sinedkadis.terracompositio.block.entity.MatterInfuserPortBlockEntity;
import net.sinedkadis.terracompositio.registries.ModBlockStateProperties;
import net.sinedkadis.terracompositio.registries.ModBlocks;
import net.sinedkadis.terracompositio.registries.ModBlockEntities;
import net.sinedkadis.terracompositio.registries.ModItems;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.sinedkadis.terracompositio.block.custom.FlowCedarCasingBlock.*;
import static net.sinedkadis.terracompositio.registries.ModBlockStateProperties.LEFT_CONNECTION;


public class MatterInfuserPortBlock extends MatterInfuserBaseBaseEntityBlock {
    private final static BooleanProperty UP_CONNECTION;
    private final static BooleanProperty RIGHT_CONNECTION;

    public MatterInfuserPortBlock(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(this.defaultBlockState()
                .setValue(UP_CONNECTION,false)
                .setValue(RIGHT_CONNECTION,false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING,UP_CONNECTION,RIGHT_CONNECTION);
    }

    @Override
    public void onRemove(@NotNull BlockState pState, @NotNull Level pLevel, @NotNull BlockPos pPos, @NotNull BlockState pNewState, boolean pIsMoving) {
        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
        if (pState.getBlock() != pNewState.getBlock()) {
            if (pState.getValue(RIGHT_CONNECTION)) {
                pLevel.addFreshEntity(new ItemEntity(pLevel, pPos.getX(), pPos.getY(), pPos.getZ(), new ItemStack(ModItems.INFUSED_IRON_ROD.get(), 2)));
                BlockPos rightPos = pPos.relative(pState.getValue(FACING).getCounterClockWise());
                BlockState rightState = pLevel.getBlockState(rightPos);
                pLevel.setBlock(rightPos, rightState.setValue(LEFT_CONNECTION,false),3);
            }
        }
    }

    @Override
    protected int slotCount() {
        return 1;
    }


    @Override
    public @NotNull InteractionResult use(@NotNull BlockState pState, Level pLevel, @NotNull BlockPos pPos, @NotNull Player pPlayer, @NotNull InteractionHand pHand, @NotNull BlockHitResult pHit) {
        ItemStack item = pPlayer.getItemInHand(pHand);
        Direction facing = pState.getValue(FACING);
        BlockPos casingPos = pPos.relative(facing.getOpposite());
        BlockState casingState = pLevel.getBlockState(casingPos);
        BlockPos rightPos = pPos.relative(facing.getCounterClockWise());
        BlockState rightState = pLevel.getBlockState(rightPos);
        if (item.is(ModItems.INFUSED_IRON_ROD.get())){
            if (!pState.getValue(RIGHT_CONNECTION) && rightState.is(ModBlocks.MATTER_INFUSER_IO.get())){
                if (item.getCount() >= 2){
                    pLevel.setBlock(pPos,pState.setValue(RIGHT_CONNECTION,true),3);
                    pLevel.setBlock(rightPos,rightState.setValue(LEFT_CONNECTION, true),3);
                    item.shrink(2);
                    return InteractionResult.sidedSuccess(pLevel.isClientSide);
                }
            }
        }
        return ((FlowCedarCasingBlock) casingState.getBlock()).use(casingState,pLevel,casingPos,pPlayer,pHand,pHit);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
        return ModBlockEntities.MATTER_INFUSER_PORT_BE.get().create(blockPos, blockState);
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
        UP_CONNECTION = ModBlockStateProperties.UP_CONNECTION;
        RIGHT_CONNECTION = ModBlockStateProperties.RIGHT_CONNECTION;
    }
}
