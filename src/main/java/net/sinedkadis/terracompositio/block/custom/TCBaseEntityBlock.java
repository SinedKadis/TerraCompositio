package net.sinedkadis.terracompositio.block.custom;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.sinedkadis.terracompositio.api.helpers.ItemHelper;
import net.sinedkadis.terracompositio.block.entity.TCBlockEntity;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@SuppressWarnings("deprecation")
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class TCBaseEntityBlock extends Block implements EntityBlock {


    public TCBaseEntityBlock(Properties pProperties) {
        super(pProperties);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        if (pLevel.isClientSide()) {
            return null;
        }
        return createTickerHelper(pBlockEntityType, getBlockEntityType(),
                (pLevel1, pPos, pState1, pBlockEntity) -> pBlockEntity.tick(pLevel1,pPos,pState1));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return getBlockEntityType().create(pPos, pState);
    }

    public abstract BlockEntityType<? extends TCBlockEntity> getBlockEntityType();

    @Override
    public void onRemove(BlockState pState,  Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {

        if (pState.getBlock() != pNewState.getBlock()){
            TCBlockEntity blockEntity = (TCBlockEntity) pLevel.getBlockEntity(pPos);
            if (blockEntity != null){
                ItemHelper.dropContents(blockEntity);
            }
        }
        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
    }
    public boolean triggerEvent(BlockState pState,Level pLevel, BlockPos pPos, int pId, int pParam) {
        super.triggerEvent(pState, pLevel, pPos, pId, pParam);
        BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
        return blockEntity != null && blockEntity.triggerEvent(pId, pParam);
    }

    @Nullable
    public MenuProvider getMenuProvider(BlockState pState, Level pLevel, BlockPos pPos) {
        BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
        return blockEntity instanceof MenuProvider ? (MenuProvider)blockEntity : null;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    protected static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(BlockEntityType<A> pServerType, BlockEntityType<E> pClientType, BlockEntityTicker<? super E> pTicker) {
        return pClientType == pServerType ? (BlockEntityTicker<A>) pTicker : null;
    }

    @Override
    public InteractionResult use(BlockState pState,Level pLevel, BlockPos pPos,Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        BlockEntity entity =  pLevel.getBlockEntity(pPos);
        if (entity instanceof TCBlockEntity tcBlockEntity) {
            boolean match = tcBlockEntity.getBehaviours().stream()
                    .map(ibeBehaviour -> ibeBehaviour.onUse(pPlayer, pHand, pHit))
                    .anyMatch(interactionResult -> interactionResult.equals(InteractionResult.SUCCESS));
            return match ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
        //return super.use(pState,pLevel,pPos,pPlayer,pHand,pHit);
        return InteractionResult.PASS;
    }

}
