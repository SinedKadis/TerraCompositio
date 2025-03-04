package net.sinedkadis.terracompositio.block.custom;

import mekanism.api.annotations.ParametersAreNotNullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.sinedkadis.terracompositio.block.ModBlocks;
import net.sinedkadis.terracompositio.item.ModItems;
import org.jetbrains.annotations.Nullable;

import static net.sinedkadis.terracompositio.util.TCUtil.handleInWorldBlockCraft;

public class FlowCedarCasingBlock extends FlowCedarLikeBlock implements EntityBlock {
    public static final DirectionProperty PARTS = DirectionProperty.create("parts");
    public FlowCedarCasingBlock(Properties pProperties) {
        super(pProperties);
        registerDefaultState(defaultBlockState().setValue(PARTS,Direction.DOWN));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState().setValue(AXIS, pContext.getClickedFace().getAxis()).setValue(PARTS,pContext.getClickedFace());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(AXIS, PARTS, INFUSED);
    }

    @Override
    @ParametersAreNotNullByDefault
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        ItemStack item = pPlayer.getItemInHand(InteractionHand.MAIN_HAND);
        if (item.is(ModItems.INPUT_BUS.get()) && pState.getValue(AXIS) != Direction.Axis.Y) {
            return handleInWorldBlockCraft(pState, ModBlocks.FLOW_CEDAR_CASING.get().defaultBlockState().setValue(PARTS, Direction.UP), pLevel, pPos, item, 1);
        }
        return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
        if (hasInputBus(pState) && !hasInputBus(pNewState)){
            pLevel.addFreshEntity(new ItemEntity(pLevel, pPos.getX(),pPos.getY(),pPos.getZ(), new ItemStack(ModItems.INPUT_BUS.get())));
            if (pNewState.getBlock() == pState.getBlock()){
                pLevel.setBlock(pPos,pNewState.setValue(PARTS,Direction.DOWN),3);
            }
            if (!pState.getValue(PARTS).equals(Direction.UP)){
                pLevel.addFreshEntity(new ItemEntity(pLevel, pPos.getX(),pPos.getY(),pPos.getZ(), new ItemStack(ModItems.INFUSED_IRON_ROD.get())));
            }
        }

    }

    public static boolean hasInputBus(BlockState blockState){
        if (blockState.hasProperty(AXIS) && blockState.hasProperty(PARTS)) {
            switch (blockState.getValue(AXIS)) {
                case X -> {
                    if (blockState.getValue(PARTS).equals(Direction.NORTH)
                            || blockState.getValue(PARTS).equals(Direction.SOUTH)){
                        return true;
                    }
                }
                case Z -> {
                    if (blockState.getValue(PARTS).equals(Direction.WEST)
                            || blockState.getValue(PARTS).equals(Direction.EAST)){
                        return true;
                    }
                }
                case Y -> {
                    return false;
                }
            }
            return blockState.getValue(PARTS).equals(Direction.UP);
        }
        return false;
    }

    @Nullable
    @Override
    @ParametersAreNotNullByDefault
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return null;
    }
}
