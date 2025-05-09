package net.sinedkadis.terracompositio.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.ToolAction;
import net.sinedkadis.terracompositio.registries.ModBlocks;
import net.sinedkadis.terracompositio.registries.ModItems;
import net.sinedkadis.terracompositio.util.TCUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class FlowCedarTankBlock extends Block {
    public static final IntegerProperty STAGE = IntegerProperty.create("stage",0,4);
    public FlowCedarTankBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(STAGE);
    }



    @Override
    public @Nullable BlockState getToolModifiedState(BlockState state, UseOnContext context, ToolAction toolAction, boolean simulate) {
        Integer stage = state.getValue(STAGE);
        if (context.getItemInHand().getItem() instanceof AxeItem && (stage.equals(0) || stage.equals(1))) {
            if (stage.equals(0)){
                TCUtil.flowLeak(state,context.getLevel(),context.getClickedPos(),false);
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
    public @NotNull InteractionResult use(@NotNull BlockState pState, @NotNull Level pLevel, @NotNull BlockPos pPos, Player pPlayer, @NotNull InteractionHand pHand, @NotNull BlockHitResult pHit) {
        ItemStack item = pPlayer.getItemInHand(pHand);
        if (item.is(Items.GLASS) && pState.getValue(STAGE).equals(2)){
            TCUtil.handleInWorldBlockCraft(pState,pState.setValue(STAGE,3),pLevel,pPos,item,1);
            return InteractionResult.SUCCESS;
        }
        return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
    }

    @Override
    public @NotNull List<ItemStack> getDrops(BlockState pState, LootParams.@NotNull Builder pParams) {
        List<ItemStack> drops = new ArrayList<>();
        switch (pState.getValue(STAGE)){
            case 0,1,2 -> {
                drops.add(new ItemStack(ModBlocks.FLOW_CEDAR_LOG.get()));
                drops.add(new ItemStack(ModItems.GOLD_ROD.get(),4));
                drops.add(new ItemStack(ModItems.INFUSED_IRON_ROD.get(),8));
            }
            case 3,4 -> {
                drops.add(new ItemStack(ModBlocks.FLOW_CEDAR_LOG.get()));
                drops.add(new ItemStack(ModItems.GOLD_ROD.get(),4));
                drops.add(new ItemStack(ModItems.INFUSED_IRON_ROD.get(),8));
                drops.add(Items.GLASS.getDefaultInstance());
            }
        }
        return drops;
    }
}
