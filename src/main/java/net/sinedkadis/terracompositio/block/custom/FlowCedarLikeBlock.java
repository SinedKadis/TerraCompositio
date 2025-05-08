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
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.ToolAction;
import net.sinedkadis.terracompositio.registries.ModBlockStateProperties;
import net.sinedkadis.terracompositio.registries.ModBlocks;
import net.sinedkadis.terracompositio.registries.ModItems;
import net.sinedkadis.terracompositio.item.custom.WrenchAxeItem;
import net.sinedkadis.terracompositio.util.TCUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

import static net.sinedkadis.terracompositio.util.TCUtil.getNearBlocks;
import static net.sinedkadis.terracompositio.util.TCUtil.handleInWorldBlockCraft;


public class FlowCedarLikeBlock extends RotatedPillarBlock {
    public static final BooleanProperty INFUSED;
    @Nullable
    private final Supplier<Block> stripPair;

    public FlowCedarLikeBlock(Properties pProperties, @Nullable Supplier<Block> stripPair) {
        super(pProperties);
        this.registerDefaultState(this.defaultBlockState().setValue(INFUSED, false));
        this.stripPair = stripPair;
    }
    public FlowCedarLikeBlock(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(this.defaultBlockState().setValue(INFUSED, false));
        this.stripPair = null;
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(AXIS,INFUSED);
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
    public @Nullable BlockState getToolModifiedState(BlockState state, UseOnContext context, ToolAction toolAction, boolean simulate) {
//        Map<BlockState,BlockState> stripPair = new HashMap<>();
//
//        stripPair.put(
//                ModBlocks.FLOW_CEDAR_LOG.get().defaultBlockState(),
//                ModBlocks.STRIPPED_FLOW_CEDAR_LOG.get().defaultBlockState());
//        stripPair.put(
//                ModBlocks.FLOW_CEDAR_WOOD.get().defaultBlockState(),
//                ModBlocks.STRIPPED_FLOW_CEDAR_WOOD.get().defaultBlockState());
//        stripPair.put(
//                ModBlocks.FLOW_PORT.get().defaultBlockState(),
//                ModBlocks.STRIPPED_FLOW_CEDAR_WOOD.get().defaultBlockState());
//        stripPair.put(
//                ModBlocks.FLOW_CEDAR_LOG.get().defaultBlockState().setValue(INFUSED,true),
//                ModBlocks.STRIPPED_FLOW_CEDAR_LOG.get().defaultBlockState().setValue(INFUSED,true));
//        stripPair.put(
//                ModBlocks.FLOW_CEDAR_WOOD.get().defaultBlockState().setValue(INFUSED,true),
//                ModBlocks.STRIPPED_FLOW_CEDAR_WOOD.get().defaultBlockState().setValue(INFUSED,true));
//        stripPair.put(
//                ModBlocks.FLOW_PORT.get().defaultBlockState().setValue(INFUSED,true),
//                ModBlocks.STRIPPED_FLOW_CEDAR_WOOD.get().defaultBlockState().setValue(INFUSED,true));

        if(context.getItemInHand().getItem() instanceof AxeItem && stripPair != null){
            return stripPair.get().defaultBlockState()
                    .setValue(AXIS, state.getValue(AXIS))
                    .setValue(INFUSED,state.getValue(INFUSED));
        }
        return super.getToolModifiedState(state, context,toolAction,simulate);
    }

    @Override
    public @NotNull InteractionResult use(@NotNull BlockState pState, @NotNull Level pLevel, @NotNull BlockPos pPos, Player pPlayer, @NotNull InteractionHand pHand, @NotNull BlockHitResult pHit) {
        ItemStack item = pPlayer.getItemInHand(InteractionHand.MAIN_HAND);
        ItemStack item2 = pPlayer.getItemInHand(InteractionHand.OFF_HAND);
        if (this.getClass() == FlowCedarLikeBlock.class) {
            if (item.is(ModItems.GOLD_ROD.get())
                    && item.getCount() > 4
                    && item2.getItem() instanceof WrenchAxeItem) {
                if (WrenchAxeItem.getMode(item2).equals(WrenchAxeItem.WrenchMode.WRENCH)) {
                    return handleInWorldBlockCraft(pState, ModBlocks.FLOW_CEDAR_CASING.get().defaultBlockState(), pLevel, pPos, item, 4);
                }
                return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
            } else if (item.is(ModItems.FLOW_INFUSER_KIT.get())
                    && item2.is(ItemTags.AXES)) {
                return handleInWorldBlockCraft(pState, ModBlocks.FLOW_INFUSER.get().defaultBlockState(), pLevel, pPos, item, 1);
            }
        }
        return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
    }

    @Override
    public void onRemove(@NotNull BlockState pState, @NotNull Level pLevel, @NotNull BlockPos pPos, @NotNull BlockState pNewState, boolean pIsMoving) {
        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
        if (pState.getBlock() != pNewState.getBlock() && TCUtil.onRemoveHandlerBlacklist(pNewState,
                Blocks.STRUCTURE_VOID,
                ModBlocks.FLOW_CEDAR_CASING.get(),
                ModBlocks.FLOW_CEDAR_PORT.get())) {
            TCUtil.flowLeak(pState, pLevel, pPos,false);
        }
    }


    @Override
    public void tick(BlockState pState, @NotNull ServerLevel pLevel, @NotNull BlockPos pPos, @NotNull RandomSource pRandom) {
        if (pState.getValue(INFUSED)) {
            for (BlockPos blockPos : getNearBlocks(pPos)) {
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

    @Override
    public boolean isRandomlyTicking(@NotNull BlockState pState) {
        return true;
    }

    static {
        INFUSED = ModBlockStateProperties.INFUSED;
    }
}
