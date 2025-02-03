package net.sinedkadis.terracompositio.block.custom;


import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.network.NetworkHooks;
import net.sinedkadis.terracompositio.block.ModBlockStateProperties;
import net.sinedkadis.terracompositio.block.ModBlocks;
import net.sinedkadis.terracompositio.block.entity.FlowPortBlockEntity;
import net.sinedkadis.terracompositio.block.entity.ModBlockEntities;
import net.sinedkadis.terracompositio.util.ModGameRules;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.world.level.block.RotatedPillarBlock.AXIS;
import static net.sinedkadis.terracompositio.block.custom.FlowCedarLikeBlock.getNearBlocks;



public class FlowPortBlock extends BaseEntityBlock {
    public FlowPortBlock(Properties pProperties) {
        super(pProperties);
    }
    public static final BooleanProperty INFUSED;

    @Override
    public @Nullable BlockState getToolModifiedState(BlockState state, UseOnContext context, ToolAction toolAction, boolean simulate) {
        if(context.getItemInHand().getItem() instanceof AxeItem){
            if(state.is(ModBlocks.FLOW_PORT.get())){
                return ModBlocks.STRIPPED_FLOW_CEDAR_LOG.get().defaultBlockState().setValue(INFUSED,state.getValue(INFUSED));
            }

        }
        return super.getToolModifiedState(state, context,toolAction,simulate);
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(INFUSED);
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        if (pState.getBlock() != pNewState.getBlock()){
            BlockEntity blockEntity =pLevel.getBlockEntity(pPos);
            if (blockEntity instanceof FlowPortBlockEntity){
                ((FlowPortBlockEntity) blockEntity).drops();
            }
        }
        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
        if (pNewState.is(Blocks.AIR) || pIsMoving) {
            if (!pLevel.getGameRules().getBoolean(ModGameRules.DISABLE_FLOW_LEAKING)) {
                BlockPos fpos = pPos.relative(pState.getValue(AXIS),1);
                BlockPos bpos = pPos.relative(pState.getValue(AXIS),-1);
                List<BlockPos> toReplace = new ArrayList<>(getNearBlocks(fpos));
                toReplace.addAll(getNearBlocks(bpos));
                toReplace.stream()
                        .filter(pos -> pos != pPos)
                        .forEach(pos -> pLevel.setBlockAndUpdate(pos,pLevel.getBlockState(pos).setValue(INFUSED,false)));
            }
        }
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        //if (!pLevel.isClientSide()){
            BlockEntity entity = pLevel.getBlockEntity(pPos);
            if(entity instanceof  FlowPortBlockEntity){
                ItemStack itemstack = pPlayer.getItemInHand(pHand);
                ItemStack outputSlot = ModBlockEntities.FLOW_PORT_BE.get().getBlockEntity(pLevel,pPos).getOutputSlot();
                ItemStack inputSlot = ModBlockEntities.FLOW_PORT_BE.get().getBlockEntity(pLevel,pPos).getInputSlot();
                if (outputSlot.isEmpty() && inputSlot.isEmpty()){
                    if(!itemstack.isEmpty()) {
                        ModBlockEntities.FLOW_PORT_BE.get().getBlockEntity(pLevel,pPos).addItemInSlot(0,itemstack,1);
                     pLevel.playSound(pPlayer,pPos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS);
                    }
                }else if (!outputSlot.isEmpty()){
                    if (!pPlayer.addItem(outputSlot)) {
                        pPlayer.drop(outputSlot, false);
                    }
                    ModBlockEntities.FLOW_PORT_BE.get().getBlockEntity(pLevel,pPos).setSlotEmpty(1);
                    pLevel.playSound(pPlayer,pPos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS);
                } else if (!inputSlot.isEmpty()){
                    if (!pPlayer.addItem(inputSlot)) {
                        pPlayer.drop(inputSlot, false);
                    }
                    ModBlockEntities.FLOW_PORT_BE.get().getBlockEntity(pLevel,pPos).setSlotEmpty(0);
                    pLevel.playSound(pPlayer,pPos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS);
                }  else  {
                    NetworkHooks.openScreen(((ServerPlayer) pPlayer), ((FlowPortBlockEntity) entity),pPos);
                }

            }
        //}

        return InteractionResult.sidedSuccess(pLevel.isClientSide());
    }


    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new FlowPortBlockEntity(pPos,pState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        if (pLevel.isClientSide()) {
            return null;
        }
        return createTickerHelper(pBlockEntityType, ModBlockEntities.FLOW_PORT_BE.get(),
                (pLevel1, pPos, pState1, pBlockEntity) -> pBlockEntity.tick(pLevel1,pPos,pState1));
    }

    static {
        INFUSED = ModBlockStateProperties.INFUSED;
    }
}