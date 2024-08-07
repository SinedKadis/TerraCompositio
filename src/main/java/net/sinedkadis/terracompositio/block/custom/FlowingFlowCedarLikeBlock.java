package net.sinedkadis.terracompositio.block.custom;

import com.mojang.logging.LogUtils;
import lombok.SneakyThrows;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.datafix.fixes.BlockStateData;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;
import net.sinedkadis.terracompositio.block.ModBlocks;
import net.sinedkadis.terracompositio.util.BlockData;
import net.sinedkadis.terracompositio.util.ModGameRules;
import net.sinedkadis.terracompositio.util.ModTags;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FlowingFlowCedarLikeBlock extends RotatedPillarBlock {
    public FlowingFlowCedarLikeBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public @Nullable BlockState getToolModifiedState(BlockState state, UseOnContext context, ToolAction toolAction, boolean simulate) {
        if(context.getItemInHand().getItem() instanceof AxeItem){
            if(state.is(ModBlocks.FLOWING_FLOW_CEDAR_LOG.get())){
                return ModBlocks.STRIPPED_FLOW_CEDAR_LOG.get().defaultBlockState().setValue(AXIS, state.getValue(AXIS));
            }
            if(state.is(ModBlocks.FLOWING_FLOW_CEDAR_WOOD.get())){
                return ModBlocks.STRIPPED_FLOW_CEDAR_WOOD.get().defaultBlockState().setValue(AXIS, state.getValue(AXIS));
            }
        }
        return super.getToolModifiedState(state, context,toolAction,simulate);
    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
        if (pNewState.is(Blocks.AIR)) {
            if (!pLevel.getGameRules().getBoolean(ModGameRules.DISABLE_FLOW_LEAKING)) {
                List<BlockPos> totoReplace = getNearBlocks(pPos);
                List<BlockData> filtered = totoReplace.stream()
                        .filter(blockPos -> blockPos !=pPos)
                        .map(pos -> new BlockData(pos,pLevel))
                        .toList();

                filtered.stream()
                        .filter(blockData -> pLevel.getBlockState(blockData.blockPos()).is(ModTags.Blocks.FLOWING_FLOW_CEDAR_LOGS))
                        .flatMap(pos -> getNearBlocks(pos.blockPos()).stream())
                        .map(pos -> new BlockData(pos,pLevel))
                        .filter(pos -> !AnyEquals(totoReplace,pos.blockPos()))
                        .forEach(FlowingFlowCedarLikeBlock::removableNearBlocks);


                filtered.forEach(FlowingFlowCedarLikeBlock::removableNearBlocks);
            }
        }
    }

    static void removableNearBlocks(BlockData blockData){
        BlockPos blockPos = blockData.blockPos();
        Level pLevel = blockData.level();
        if (pLevel.getBlockState(blockPos).is(ModBlocks.FLOWING_FLOW_CEDAR_LOG.get())) {
            pLevel.setBlockAndUpdate(blockPos,
                    ModBlocks.FLOW_CEDAR_LOG.get().defaultBlockState().setValue(AXIS, pLevel.getBlockState(blockPos).getValue(AXIS)));
        }
        if (pLevel.getBlockState(blockPos).is(ModBlocks.FLOWING_FLOW_CEDAR_WOOD.get())) {
            pLevel.setBlockAndUpdate(blockPos,
                    ModBlocks.FLOW_CEDAR_WOOD.get().defaultBlockState().setValue(AXIS, pLevel.getBlockState(blockPos).getValue(AXIS)));
        }
        if (pLevel.getBlockState(blockPos).is(ModBlocks.FLOWING_FLOW_PORT.get())) {
            pLevel.setBlockAndUpdate(blockPos,
                    ModBlocks.FLOW_PORT.get().defaultBlockState());
        }
    }

    static @NotNull List<BlockPos> getNearBlocks(BlockPos pPos) {
        List<BlockPos> toReplace = new ArrayList<>();
        for (int x = -1;x<=1;x++){
            for (int y = -1;y<=1;y++){
                for (int z = -1;z<=1;z++){
                    toReplace.add(new BlockPos(pPos.getX() + x,
                            pPos.getY() + y,
                            pPos.getZ() + z));
                }
            }
        }
        return toReplace;
    }

    static boolean AnyEquals(List<BlockPos> massive,BlockPos blockPos){
        for (BlockPos blockPos1 : massive) {
            if (blockPos1 == blockPos) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
        for (BlockPos blockPos : getNearBlocks(pPos)){
            if (blockPos != pPos){
                if (pLevel.getBlockState(blockPos).is(ModTags.Blocks.FLOW_CEDAR_LOGS)) {
                    if (pRandom.nextFloat() > 0.99f) removableNearBlocks(new BlockData(blockPos,pLevel));
                }
            }
        }
    }

    @Override
    public boolean isRandomlyTicking(BlockState pState) {
        return true;
    }
}
