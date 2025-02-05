package net.sinedkadis.terracompositio.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraftforge.common.ToolAction;
import net.sinedkadis.terracompositio.block.ModBlockStateProperties;
import net.sinedkadis.terracompositio.block.ModBlocks;
import net.sinedkadis.terracompositio.util.ModGameRules;
import net.sinedkadis.terracompositio.util.ModTags;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.sinedkadis.terracompositio.util.TCUtil.getNearBlocks;


public class FlowCedarLikeBlock extends RotatedPillarBlock {
    public static final BooleanProperty INFUSED;

    public FlowCedarLikeBlock(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState((BlockState)this.defaultBlockState().setValue(INFUSED, false));

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
        Map<BlockState,BlockState> stripPair = new HashMap<>();

        stripPair.put(
                ModBlocks.FLOW_CEDAR_LOG.get().defaultBlockState(),
                ModBlocks.STRIPPED_FLOW_CEDAR_LOG.get().defaultBlockState());
        stripPair.put(
                ModBlocks.FLOW_CEDAR_WOOD.get().defaultBlockState(),
                ModBlocks.STRIPPED_FLOW_CEDAR_WOOD.get().defaultBlockState());
        stripPair.put(
                ModBlocks.FLOW_PORT.get().defaultBlockState(),
                ModBlocks.STRIPPED_FLOW_CEDAR_WOOD.get().defaultBlockState());
        stripPair.put(
                ModBlocks.FLOW_CEDAR_LOG.get().defaultBlockState().setValue(INFUSED,true),
                ModBlocks.STRIPPED_FLOW_CEDAR_LOG.get().defaultBlockState().setValue(INFUSED,true));
        stripPair.put(
                ModBlocks.FLOW_CEDAR_WOOD.get().defaultBlockState().setValue(INFUSED,true),
                ModBlocks.STRIPPED_FLOW_CEDAR_WOOD.get().defaultBlockState().setValue(INFUSED,true));
        stripPair.put(
                ModBlocks.FLOW_PORT.get().defaultBlockState().setValue(INFUSED,true),
                ModBlocks.STRIPPED_FLOW_CEDAR_WOOD.get().defaultBlockState().setValue(INFUSED,true));

        if(context.getItemInHand().getItem() instanceof AxeItem){
            if (stripPair.containsKey(state))
                return stripPair.get(state)
                    .setValue(AXIS, state.getValue(AXIS))
                    .setValue(INFUSED,state.getValue(INFUSED));
        }
        return super.getToolModifiedState(state, context,toolAction,simulate);
    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
        if (pNewState.is(Blocks.AIR)) {
            flowLeak(pState, pLevel, pPos,false);
        }
    }

    public static void flowLeak(BlockState pState, Level pLevel, BlockPos pPos,boolean chained) {
        if (pState.hasProperty(INFUSED) && pState.getValue(INFUSED)&&!pLevel.getGameRules().getBoolean(ModGameRules.DISABLE_FLOW_LEAKING)) {

                BlockPos fpos;
                BlockPos bpos;
                if (pState.hasProperty(AXIS)) {
                    fpos = pPos.relative(pState.getValue(AXIS), 1);
                    bpos = pPos.relative(pState.getValue(AXIS), -1);
                } else {
                    fpos = pPos.relative(Direction.Axis.Y, 1);
                    bpos = pPos.relative(Direction.Axis.Y, -1);
                }
                getNearBlocks(fpos,chained ? 3 : 1).stream()
                        .filter(pos -> pos != pPos)
                        .filter(pos -> pLevel.getBlockState(pos).is(ModTags.Blocks.FLOW_LEAKABLE))
                        .forEach(pos -> pLevel.setBlockAndUpdate(pos, pLevel.getBlockState(pos).setValue(INFUSED, false)));
                getNearBlocks(bpos,chained ? 3 : 1).stream()
                        .filter(pos -> pos != pPos)
                        .filter(pos -> pLevel.getBlockState(pos).is(ModTags.Blocks.FLOW_LEAKABLE))
                        .forEach(pos -> pLevel.setBlockAndUpdate(pos, pLevel.getBlockState(pos).setValue(INFUSED, false)));

        }
    }



    @Override
    public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
        if (pState.getValue(INFUSED)) {
            for (BlockPos blockPos : getNearBlocks(pPos)) {
                if (blockPos.getX() != pPos.getX()
                        && blockPos.getY() != pPos.getY()
                        && blockPos.getZ() != pPos.getZ()) {
                    if (pLevel.getBlockState(blockPos).is(ModTags.Blocks.FLOW_LEAKABLE)) {
                        if (!pLevel.getBlockState(blockPos).getValue(INFUSED) && pRandom.nextFloat() > 0.99f)
                            pLevel.setBlockAndUpdate(blockPos, pLevel.getBlockState(blockPos).setValue(INFUSED, true));
                    }
                }
            }
        }
    }

    @Override
    public boolean isRandomlyTicking(BlockState pState) {
        return true;
    }

    static {
        INFUSED = ModBlockStateProperties.INFUSED;
    }
}
