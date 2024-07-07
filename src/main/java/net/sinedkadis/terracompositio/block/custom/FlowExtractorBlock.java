package net.sinedkadis.terracompositio.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.sinedkadis.terracompositio.block.entity.FlowExtractorBlockEntity;
import net.sinedkadis.terracompositio.block.entity.ModBlockEntities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FlowExtractorBlock extends BaseEntityBlock {
    public FlowExtractorBlock(Properties properties) {
        super(properties);
    }
    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return Shapes.join(
                Shapes.join(
                Block.box(0,2,0,2,16,16),
                Block.box(2,2,14,16,16,16),
                BooleanOp.OR),
                Shapes.join(
                        Shapes.join(
                                Block.box(14,2,2,16,16,14),
                                Block.box(2,2,0,16,16,2),
                                BooleanOp.OR),
                        Block.box(2,2,2,14,4,14),
                        BooleanOp.OR),
                BooleanOp.OR);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pPos, @NotNull BlockState pState) {
        return ModBlockEntities.FLOW_EXTRACTOR_BE.get().create(pPos, pState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level pLevel, @NotNull BlockState pState, @NotNull BlockEntityType<T> pBlockEntityType) {
        if (pLevel.isClientSide()) {
            return null;
        }
        return createTickerHelper(pBlockEntityType, ModBlockEntities.FLOW_EXTRACTOR_BE.get(),
                (pLevel1, pPos, pState1, pBlockEntity) -> pBlockEntity.tick(pLevel1,pPos,pState1));
    }

    @SuppressWarnings("deprecation")
    @Override
    public @NotNull InteractionResult use(@NotNull BlockState pState, @NotNull Level pLevel, @NotNull BlockPos pPos, @NotNull Player pPlayer, @NotNull InteractionHand pHand, @NotNull BlockHitResult pHit) {
        LazyOptional<IFluidHandlerItem> fluidHandler = pPlayer.getItemInHand(InteractionHand.MAIN_HAND).getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM);
        FluidTank fluidTank = ModBlockEntities.FLOW_EXTRACTOR_BE.get().getBlockEntity(pLevel,pPos).getFluidTank();
        fluidHandler.ifPresent(iFluidHandlerItem -> {
            if(!fluidTank.getFluid().isFluidEqual(iFluidHandlerItem.getFluidInTank(0)))
                return;

            int amountToDrain = fluidTank.getCapacity() - fluidTank.getFluidAmount();
            int amount = iFluidHandlerItem.drain(amountToDrain, IFluidHandler.FluidAction.SIMULATE).getAmount();
            if(amount > 0) {
                fluidTank.fill(iFluidHandlerItem.drain(amountToDrain, IFluidHandler.FluidAction.EXECUTE), IFluidHandler.FluidAction.EXECUTE);

                if(amount <= amountToDrain) {
                    pPlayer.setItemInHand(InteractionHand.MAIN_HAND,iFluidHandlerItem.getContainer());
                }
            }
        });

        return super.use(pState,pLevel,pPos,pPlayer,pHand,pHit);
    }
}
