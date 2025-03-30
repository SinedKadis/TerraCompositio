package net.sinedkadis.terracompositio.block.custom;

import mekanism.api.annotations.ParametersAreNotNullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.sinedkadis.terracompositio.block.entity.ModItemIOCFEBlockEntity;
import org.jetbrains.annotations.NotNull;

public abstract class ModIOBaseEntityBlock extends Block implements EntityBlock {


    public ModIOBaseEntityBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    @ParametersAreNotNullByDefault
    public void onRemove(BlockState pState,Level pLevel,BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        if (pState.getBlock() != pNewState.getBlock()){
            BlockEntity blockEntity =pLevel.getBlockEntity(pPos);
            if (blockEntity instanceof ModItemIOCFEBlockEntity entity){
                entity.drops();
            }
        }
        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
    }
    @ParametersAreNotNullByDefault
    public boolean triggerEvent(BlockState pState, Level pLevel, BlockPos pPos, int pId, int pParam) {
        super.triggerEvent(pState, pLevel, pPos, pId, pParam);
        BlockEntity $$5 = pLevel.getBlockEntity(pPos);
        return $$5 != null && $$5.triggerEvent(pId, pParam);
    }

    @javax.annotation.Nullable
    @ParametersAreNotNullByDefault
    public MenuProvider getMenuProvider(BlockState pState, Level pLevel, BlockPos pPos) {
        BlockEntity $$3 = pLevel.getBlockEntity(pPos);
        return $$3 instanceof MenuProvider ? (MenuProvider)$$3 : null;
    }

    @javax.annotation.Nullable
    @SuppressWarnings("unchecked")
    protected static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(BlockEntityType<A> pServerType, BlockEntityType<E> pClientType, BlockEntityTicker<? super E> pTicker) {
        return pClientType == pServerType ? (BlockEntityTicker<A>) pTicker : null;
    }

    @Override
    public @NotNull InteractionResult use(@NotNull BlockState pState, Level pLevel, @NotNull BlockPos pPos, @NotNull Player pPlayer, @NotNull InteractionHand pHand, @NotNull BlockHitResult pHit) {
        //if (!pLevel.isClientSide()){
        BlockEntity entity = pLevel.getBlockEntity(pPos);
        if (entity instanceof ModItemIOCFEBlockEntity blockEntity) {
            ItemStack itemstack = pPlayer.getItemInHand(pHand);
            ItemStack outputSlot = blockEntity.getLastSlot();
            ItemStack inputSlot = blockEntity.getFirstSlot();
            if (outputSlot.isEmpty() && inputSlot.isEmpty()) {
                if (!itemstack.isEmpty()) {
                    blockEntity.insertItemStack(0, itemstack);
                    itemstack.shrink(1);
                    pLevel.playSound(pPlayer, pPos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS);
                }
            } else if (!outputSlot.isEmpty()) {
                if (!pPlayer.addItem(outputSlot)) {
                    pPlayer.drop(outputSlot, false);
                }
                blockEntity.setSlotEmpty(1);
                pLevel.playSound(pPlayer, pPos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS);
            } else if (!inputSlot.isEmpty()) {
                if (!pPlayer.addItem(inputSlot)) {
                    pPlayer.drop(inputSlot, false);
                }
                blockEntity.setSlotEmpty(0);
                pLevel.playSound(pPlayer, pPos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS);
            }
        }
        //}
        return InteractionResult.sidedSuccess(pLevel.isClientSide());
    }

}
