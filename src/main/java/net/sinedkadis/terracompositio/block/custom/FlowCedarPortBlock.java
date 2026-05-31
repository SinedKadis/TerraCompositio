package net.sinedkadis.terracompositio.block.custom;


import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.ToolAction;
import net.sinedkadis.terracompositio.block.entity.TCBlockEntity;
import net.sinedkadis.terracompositio.item.custom.WrenchAxeItem;
import net.sinedkadis.terracompositio.registries.TCBlockEntities;
import net.sinedkadis.terracompositio.registries.TCBlockStateProperties;
import net.sinedkadis.terracompositio.registries.TCBlocks;
import net.sinedkadis.terracompositio.registries.TCItems;
import net.sinedkadis.terracompositio.util.helpers.BlockPosHelper;
import net.sinedkadis.terracompositio.util.helpers.WorldHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

import static net.sinedkadis.terracompositio.util.helpers.WorldHelper.flowLeak;


public class FlowCedarPortBlock extends TCBaseEntityBlock {
    public static final DirectionProperty FACING;
    public static final BooleanProperty INFUSED;
    private final Supplier<Block> stripPair;

    public FlowCedarPortBlock(Properties pProperties, Supplier<Block> stripPair) {
        super(pProperties);
        this.stripPair = stripPair;
        this.registerDefaultState(this.defaultBlockState().setValue(INFUSED,false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING,INFUSED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState().setValue(FACING,pContext.getHorizontalDirection().getOpposite());
    }

    @Override
    public boolean isFlammable(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return !state.getValue(INFUSED);
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(INFUSED) ? 0 : 5;
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(INFUSED) ? 0 : 5;
    }

    @Override
    public @NotNull InteractionResult use(@NotNull BlockState pState, @NotNull Level pLevel, @NotNull BlockPos pPos, @NotNull Player pPlayer, @NotNull InteractionHand pHand, @NotNull BlockHitResult pHit) {
        ItemStack item = pPlayer.getItemInHand(InteractionHand.MAIN_HAND);
        ItemStack item2 = pPlayer.getItemInHand(InteractionHand.OFF_HAND);
        if (item.is(TCItems.GOLD_ROD.get())
                && item.getCount() > 4
                && item2.getItem() instanceof WrenchAxeItem) {
            if (WrenchAxeItem.getMode(item2).equals(WrenchAxeItem.WrenchMode.WRENCH)) {
                return WorldHelper.handleInWorldBlockCraft(pState, TCBlocks.FLOW_CEDAR_CASING.get().defaultBlockState(), pLevel, pPos, item, 4);
            }
            return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
        } else if (item.is(TCItems.FLOW_INFUSER_KIT.get())
                && item2.is(ItemTags.AXES)) {
            return WorldHelper.handleInWorldBlockCraft(pState, TCBlocks.FLOW_INFUSER.get().defaultBlockState(), pLevel, pPos, item, 1);
        }
        return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
    }

    @Override
    public void onRemove(@NotNull BlockState pState, @NotNull Level pLevel, @NotNull BlockPos pPos, @NotNull BlockState pNewState, boolean pIsMoving) {
        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
        if (pState.getBlock() != pNewState.getBlock() && WorldHelper.onRemoveHandlerBlacklist(pNewState,
                Blocks.STRUCTURE_VOID,
                TCBlocks.FLOW_CEDAR_CASING.get(),
                TCBlocks.FLOW_CEDAR_PORT.get())) {
            flowLeak(pState, pLevel, pPos,false);
        }
    }

    @Override
    public @Nullable BlockState getToolModifiedState(BlockState state, UseOnContext context, ToolAction toolAction, boolean simulate) {
        if(context.getItemInHand().getItem() instanceof AxeItem && stripPair != null){
            return stripPair.get().defaultBlockState()
                    .setValue(INFUSED,state.getValue(INFUSED));
        }
        return super.getToolModifiedState(state, context,toolAction,simulate);
    }

    @Override
    protected @NotNull BlockEntityType<? extends TCBlockEntity> getBlockEntityType() {
        return TCBlockEntities.FLOW_PORT_BE.get();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void tick(BlockState pState, @NotNull ServerLevel pLevel, @NotNull BlockPos pPos, @NotNull RandomSource pRandom) {
        if (pState.getValue(INFUSED)) {
            for (BlockPos blockPos : BlockPosHelper.getNearBlocks(pPos)) {
                if (blockPos.getX() != pPos.getX()
                        && blockPos.getY() != pPos.getY()
                        && blockPos.getZ() != pPos.getZ()) {
                    if (pLevel.getBlockState(blockPos).hasProperty(INFUSED)) {
                        if (!pLevel.getBlockState(blockPos).getValue(INFUSED) && pRandom.nextFloat() > 0.99f)
                            pLevel.setBlockAndUpdate(blockPos, pLevel.getBlockState(blockPos).setValue(INFUSED, true));
                    }
                }
            }
        }
    }

    static {
        FACING = BlockStateProperties.HORIZONTAL_FACING;
        INFUSED = TCBlockStateProperties.INFUSED;
    }
}