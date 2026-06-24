package net.sinedkadis.terracompositio.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BottleItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.sinedkadis.terracompositio.block.entity.TCBlockEntity;
import net.sinedkadis.terracompositio.item.custom.WrenchAxeItem;
import net.sinedkadis.terracompositio.registries.*;
import net.sinedkadis.terracompositio.util.helpers.WorldHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class FlowCedarTankBlock extends TCBaseEntityBlock{
    public static final IntegerProperty STAGE = IntegerProperty.create("stage",0,4);
    public FlowCedarTankBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(STAGE);
    }


    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, @Nullable Direction direction) {
        return true;
    }

    @Override
    public @Nullable BlockState getToolModifiedState(BlockState state, UseOnContext context, ToolAction toolAction, boolean simulate) {
        Integer stage = state.getValue(STAGE);
        if (context.getItemInHand().getItem() instanceof AxeItem && (stage.equals(0) || stage.equals(1))) {
            if (stage.equals(0)){
                WorldHelper.flowLeak(state, context.getLevel(), context.getClickedPos(), false);
            }
            return state.setValue(STAGE, 2);
        }
        return super.getToolModifiedState(state, context, toolAction, simulate);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(@NotNull BlockPlaceContext pContext) {
        BlockState stateForPlacement = super.getStateForPlacement(pContext);
        return stateForPlacement != null ? stateForPlacement.setValue(STAGE, 3) : null;
    }

    @Override
    public @NotNull InteractionResult use(@NotNull BlockState pState, @NotNull Level pLevel, @NotNull BlockPos pPos, @NotNull Player pPlayer, @NotNull InteractionHand pHand, @NotNull BlockHitResult pHit) {
        ItemStack heldItem = pPlayer.getItemInHand(InteractionHand.MAIN_HAND);
        ItemStack item2 = pPlayer.getItemInHand(InteractionHand.OFF_HAND);

        if (heldItem.is(Items.GLASS) && pState.getValue(STAGE).equals(2)
                && (item2.is(TCTags.Items.WRENCHES) || item2.is(TCItems.WRENCH_AXE.get()))) {
            if (!item2.is(TCItems.WRENCH_AXE.get()) || WrenchAxeItem.getWrenchMode(item2).equals(WrenchAxeItem.WrenchMode.WRENCH)) {
                WorldHelper.handleInWorldBlockCraft(pState, pState.setValue(STAGE, 3), pLevel, pPos, heldItem, 1);
                    return InteractionResult.SUCCESS;
            }
        }

        BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
        if (blockEntity == null) {
            return InteractionResult.PASS;
        }

        IFluidHandler fluidHandlerBlock = blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER).resolve().orElse(null);
        if (!(fluidHandlerBlock instanceof FluidTank tank)) {
            return InteractionResult.PASS;
        }

        if (tank.getSpace() <= 0){
            return InteractionResult.SUCCESS;
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
                        level.playSound(null,pPos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS);
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



            FluidStack transferred = FluidUtil.tryFluidTransfer(tank, fluidHandlerItem, Integer.MAX_VALUE, false);

            if (!transferred.isEmpty()) {
                if (!pPlayer.getAbilities().instabuild) {
                    FluidUtil.tryFluidTransfer(tank, fluidHandlerItem, Integer.MAX_VALUE, true);
                    pPlayer.setItemInHand(pHand, fluidHandlerItem.getContainer());
                } else {
                    tank.fill(transferred, IFluidHandler.FluidAction.EXECUTE);
                }
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


            transferred = FluidUtil.tryFluidTransfer(fluidHandlerItem, tank, Integer.MAX_VALUE, false);

            if (!transferred.isEmpty()) {
                if (!pPlayer.getAbilities().instabuild) {
                    FluidUtil.tryFluidTransfer(fluidHandlerItem, tank, Integer.MAX_VALUE, true);
                    pPlayer.setItemInHand(pHand, fluidHandlerItem.getContainer());
                } else {
                    tank.drain(transferred, IFluidHandler.FluidAction.EXECUTE);
                }
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


        return InteractionResult.PASS;
    }

    @SuppressWarnings("deprecation")
    @Override
    public @NotNull List<ItemStack> getDrops(@NotNull BlockState pState, LootParams.@NotNull Builder pParams) {
        List<ItemStack> drops = new ArrayList<>();
        ItemStack inHand = pParams.getParameter(LootContextParams.TOOL);
        if (inHand.getItem() instanceof AxeItem){
            return super.getDrops(pState,pParams);
        }
        switch (pState.getValue(STAGE)){
            case 0,1,2 -> {
                drops.add(new ItemStack(TCBlocks.FLOW_CEDAR_LOG.get()));
                drops.add(new ItemStack(TCItems.GOLD_ROD.get(),4));
                drops.add(new ItemStack(TCItems.INFUSED_IRON_ROD.get(),8));
            }
            case 3,4 -> {
                drops.add(new ItemStack(TCBlocks.FLOW_CEDAR_LOG.get()));
                drops.add(new ItemStack(TCItems.GOLD_ROD.get(),4));
                drops.add(new ItemStack(TCItems.INFUSED_IRON_ROD.get(),8));
                drops.add(Items.GLASS.getDefaultInstance());
            }
        }
        return drops;
    }

    @Override
    @NotNull
    public BlockEntityType<? extends TCBlockEntity> getBlockEntityType() {
        return TCBlockEntities.FLOW_CEDAR_TANK_BE.get();
    }
}
