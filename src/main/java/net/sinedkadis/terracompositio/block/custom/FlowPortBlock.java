package net.sinedkadis.terracompositio.block.custom;


import mekanism.api.annotations.ParametersAreNotNullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import net.sinedkadis.terracompositio.block.entity.FlowPortBlockEntity;
import net.sinedkadis.terracompositio.registries.ModBlockEntities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;


public class FlowPortBlock extends FlowCedarLikeBaseEntityBlock {
    public static final DirectionProperty FACING;

    public FlowPortBlock(Properties pProperties, Supplier<Block> stripPair) {
        super(pProperties, stripPair);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING,AXIS,INFUSED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        Direction direction = pContext.getClickedFace();
        if (direction.getAxis().isHorizontal()){
            return this.defaultBlockState().setValue(AXIS,direction.getAxis());
        }else {
            return this.defaultBlockState().setValue(AXIS,Direction.Axis.Y).setValue(FACING,pContext.getHorizontalDirection().getOpposite());
        }
    }

    @Override
    public @NotNull InteractionResult use(@NotNull BlockState pState, @NotNull Level pLevel, @NotNull BlockPos pPos, @NotNull Player pPlayer, @NotNull InteractionHand pHand, @NotNull BlockHitResult pHit) {
        //if (!pLevel.isClientSide()){
            BlockEntity entity = pLevel.getBlockEntity(pPos);
            if(entity instanceof  FlowPortBlockEntity blockEntity){
                ItemStack itemstack = pPlayer.getItemInHand(pHand);
                ItemStack outputSlot = blockEntity.getOutputSlot();
                ItemStack inputSlot = blockEntity.getInputSlot();
                if (outputSlot.isEmpty() && inputSlot.isEmpty()){
                    if(!itemstack.isEmpty()) {
                        ItemStack remaining = blockEntity.insertItemStack(0, itemstack);
                        if (remaining.getCount() != itemstack.getCount()) {
                            itemstack.setCount(remaining.getCount());
                            pLevel.playSound(pPlayer, pPos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS);
                        }
                    }
                }else if (!outputSlot.isEmpty()){
                    if (!pPlayer.addItem(outputSlot)) {
                        pPlayer.drop(outputSlot, false);
                    }
                    blockEntity.setSlotEmpty(1);
                    pLevel.playSound(pPlayer,pPos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS);
                } else if (!inputSlot.isEmpty()){
                    if (!pPlayer.addItem(inputSlot)) {
                        pPlayer.drop(inputSlot, false);
                    }
                    blockEntity.setSlotEmpty(0);
                    pLevel.playSound(pPlayer,pPos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS);
                }  else  {
                    NetworkHooks.openScreen(((ServerPlayer) pPlayer), ((FlowPortBlockEntity) entity),pPos);
                }

            }
        //}

        return InteractionResult.sidedSuccess(pLevel.isClientSide());
    }

    @Override
    public void onRemove(@NotNull BlockState pState, @NotNull Level pLevel, @NotNull BlockPos pPos, @NotNull BlockState pNewState, boolean pIsMoving) {
        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
        if (pState.getBlock() != pNewState.getBlock()){
            FlowPortBlockEntity blockEntity = (FlowPortBlockEntity) pLevel.getBlockEntity(pPos);
            if (blockEntity != null) {
                blockEntity.drops();
            }
        }
    }

    @Nullable
    @Override
    @ParametersAreNotNullByDefault
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new FlowPortBlockEntity(pPos,pState);
    }

    @Nullable
    @Override
    @ParametersAreNotNullByDefault
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        if (pLevel.isClientSide()) {
            return null;
        }
        return createTickerHelper(pBlockEntityType, ModBlockEntities.FLOW_PORT_BE.get(),
                (pLevel1, pPos, pState1, pBlockEntity) -> pBlockEntity.tick(pLevel1,pPos,pState1));
    }

    static {
        FACING = BlockStateProperties.HORIZONTAL_FACING;
    }
}