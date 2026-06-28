package net.sinedkadis.terracompositio.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BottleItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.sinedkadis.terracompositio.api.registries.TCBlockStateProperties;
import net.sinedkadis.terracompositio.registries.TCFluids;
import net.sinedkadis.terracompositio.registries.TCItems;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractDesorberBlock extends TCBaseEntityBlock implements SimpleWaterloggedBlock {
    protected static final BooleanProperty INFUSED;
    protected static final BooleanProperty WATERLOGGED;

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(INFUSED,WATERLOGGED);
    }



    @SuppressWarnings("deprecation")
    public @NotNull FluidState getFluidState(BlockState pState) {
        return pState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(pState);
    }

    protected AbstractDesorberBlock(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(this.defaultBlockState().setValue(WATERLOGGED,false));
    }

    @Override
    public @NotNull InteractionResult use(
            @NotNull BlockState pState,
            @NotNull Level pLevel,
            @NotNull BlockPos pPos,
            @NotNull Player pPlayer,
            @NotNull InteractionHand pHand,
            @NotNull BlockHitResult pHit
    ) {

        ItemStack heldItem = pPlayer.getItemInHand(pHand);

        BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
        if (blockEntity == null) {
            return InteractionResult.PASS;
        }

        IFluidHandler fluidHandlerBlock = blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER, Direction.DOWN).resolve().orElse(null);
        if (!(fluidHandlerBlock instanceof FluidTank tank)) {
            return InteractionResult.PASS;
        }

        if (pPlayer.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }

        IFluidHandlerItem fluidHandlerItem = heldItem.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).resolve().orElse(null);
        if (fluidHandlerItem == null) {
            FluidStack fluidStack = new FluidStack(TCFluids.FLOW_FLUID.source.get().getSource(), 250);
            if (heldItem.is(TCItems.FLOW_BOTTLE.get())){
                int filled = tank.fill(fluidStack, IFluidHandler.FluidAction.SIMULATE);
                if (filled == 250){
                    tank.fill(fluidStack, IFluidHandler.FluidAction.EXECUTE);
                    heldItem.shrink(1);
                    pPlayer.getInventory().add(new ItemStack(Items.GLASS_BOTTLE));
                    if (pLevel instanceof ServerLevel level){
                        level.playSound(null,pPos,SoundEvents.BOTTLE_EMPTY,SoundSource.BLOCKS);
                    }
                    return InteractionResult.SUCCESS;
                }
            }
            if (heldItem.getItem() instanceof BottleItem) {
                FluidStack drained = tank.drain(fluidStack, IFluidHandler.FluidAction.SIMULATE);
                if (drained.isFluidStackIdentical(fluidStack)){
                    tank.drain(fluidStack, IFluidHandler.FluidAction.EXECUTE);
                    heldItem.shrink(1);
                    pPlayer.getInventory().add(new ItemStack(TCItems.FLOW_BOTTLE.get()));
                    if (pLevel instanceof ServerLevel level){
                        level.playSound(null,pPos,SoundEvents.BUCKET_FILL,SoundSource.BLOCKS);
                    }
                    return InteractionResult.SUCCESS;
                }
            }

            return InteractionResult.PASS;
        }

        if (!pLevel.isClientSide()) {

            FluidStack transferred = FluidUtil.tryFluidTransfer(tank, fluidHandlerItem, Integer.MAX_VALUE, true);

            if (!transferred.isEmpty()) {

                pPlayer.setItemInHand(pHand, fluidHandlerItem.getContainer());
                pLevel.playSound(
                        null,
                        pPos,
                        SoundEvents.BUCKET_EMPTY,
                        SoundSource.BLOCKS,
                        1.0F,
                        1.0F
                );
                return InteractionResult.SUCCESS;
            }


            transferred = FluidUtil.tryFluidTransfer(fluidHandlerItem, tank, Integer.MAX_VALUE, true);

            if (!transferred.isEmpty()) {
                pPlayer.setItemInHand(pHand, fluidHandlerItem.getContainer());
                pLevel.playSound(
                        null,
                        pPos,
                        SoundEvents.BUCKET_FILL,
                        SoundSource.BLOCKS,
                        1.0F,
                        1.0F
                );
                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.sidedSuccess(pLevel.isClientSide());
    }

    static {
        INFUSED = TCBlockStateProperties.INFUSED;
        WATERLOGGED = BlockStateProperties.WATERLOGGED;
    }
}
