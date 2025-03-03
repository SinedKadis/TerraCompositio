package net.sinedkadis.terracompositio.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
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
import net.sinedkadis.terracompositio.block.entity.FlowInfuserBlockEntity;
import net.sinedkadis.terracompositio.block.entity.FlowPortBlockEntity;
import net.sinedkadis.terracompositio.block.entity.ModCFEBlockEntity;
import net.sinedkadis.terracompositio.block.entity.ModBlockEntities;
import net.sinedkadis.terracompositio.util.IBE;
import net.sinedkadis.terracompositio.util.ModGameRules;
import net.sinedkadis.terracompositio.util.ModTags;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static net.sinedkadis.terracompositio.util.TCUtil.getNearBlocks;

public class FlowInfuserBlock extends FlowCedarLikeBaseEntityBlock {

    public FlowInfuserBlock(Properties pProperties, Supplier<Block> stripPair) {
        super(pProperties, stripPair);
    }


    @Override
    public @NotNull InteractionResult use(@NotNull BlockState pState, Level pLevel, @NotNull BlockPos pPos, @NotNull Player pPlayer, @NotNull InteractionHand pHand, @NotNull BlockHitResult pHit) {
        //if (!pLevel.isClientSide()){
        BlockEntity entity = pLevel.getBlockEntity(pPos);
        if(entity instanceof FlowInfuserBlockEntity && pState.getValue(INFUSED)){
            ItemStack itemstack = pPlayer.getItemInHand(pHand);
            ItemStack outputSlot = ModBlockEntities.FLOW_INFUSER_BE.get().getBlockEntity(pLevel,pPos).getOutputSlot();
            ItemStack inputSlot = ModBlockEntities.FLOW_INFUSER_BE.get().getBlockEntity(pLevel,pPos).getInputSlot();
            if (outputSlot.isEmpty() && inputSlot.isEmpty()){
                if(!itemstack.isEmpty()) {
                    ModBlockEntities.FLOW_INFUSER_BE.get().getBlockEntity(pLevel,pPos).addItemInSlot(0,itemstack,1);
                    pLevel.playSound(pPlayer,pPos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS);
                }
            }else if (!outputSlot.isEmpty()){
                if (!pPlayer.addItem(outputSlot)) {
                    pPlayer.drop(outputSlot, false);
                }
                ModBlockEntities.FLOW_INFUSER_BE.get().getBlockEntity(pLevel,pPos).setSlotEmpty(1);
                pLevel.playSound(pPlayer,pPos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS);
            } else if (!inputSlot.isEmpty()){
                if (!pPlayer.addItem(inputSlot)) {
                    pPlayer.drop(inputSlot, false);
                }
                ModBlockEntities.FLOW_INFUSER_BE.get().getBlockEntity(pLevel,pPos).setSlotEmpty(0);
                pLevel.playSound(pPlayer,pPos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS);
            }
        }
        //}
        return pState.getValue(INFUSED) ? InteractionResult.sidedSuccess(pLevel.isClientSide()) : InteractionResult.PASS;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return ModBlockEntities.FLOW_INFUSER_BE.get().create(blockPos, blockState).markVirtual();
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level pLevel, @NotNull BlockState pState, @NotNull BlockEntityType<T> pBlockEntityType) {
        if (pLevel.isClientSide()) {
            return null;
        }
        return createTickerHelper(pBlockEntityType, ModBlockEntities.FLOW_INFUSER_BE.get(),
                (pLevel1, pPos, pState1, pBlockEntity) -> pBlockEntity.tick(pLevel1,pPos,pState1));
    }
}
