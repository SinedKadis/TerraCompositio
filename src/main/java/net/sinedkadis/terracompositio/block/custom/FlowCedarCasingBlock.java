package net.sinedkadis.terracompositio.block.custom;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.sinedkadis.terracompositio.block.IFluidApplicable;
import net.sinedkadis.terracompositio.block.entity.TCBlockEntity;
import net.sinedkadis.terracompositio.registries.TCBlockEntities;
import net.sinedkadis.terracompositio.registries.TCBlockStateProperties;
import net.sinedkadis.terracompositio.registries.TCBlocks;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import static net.sinedkadis.terracompositio.util.helpers.WorldHelper.handleInWorldBlockCraft;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class FlowCedarCasingBlock extends TCBaseEntityBlock implements IFluidApplicable {
    public static final BooleanProperty INFUSED = TCBlockStateProperties.INFUSED;
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;
    protected static final BooleanProperty WAXED = TCBlockStateProperties.WAXED;
    public static final DirectionProperty ATTACHED_DIR = BlockStateProperties.FACING;

    public FlowCedarCasingBlock(Properties pProperties) {
        super(pProperties);
        registerDefaultState(defaultBlockState()
                .setValue(INFUSED, false)
                .setValue(WAXED, false)
                .setValue(ATTACHED_DIR, Direction.DOWN));
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        ItemStack item = pPlayer.getItemInHand(InteractionHand.MAIN_HAND);
        if (item.getItem() instanceof BlockItem blockItem) {

            BlockPlaceContext context = new BlockPlaceContext(
                    pPlayer,
                    pHand,
                    item,
                    pHit
            );

            if (blockItem.canPlace(context, blockItem.getBlock().defaultBlockState())) {
                return InteractionResult.PASS;
            }
        }
        if (item.is(Items.HONEYCOMB) && !pState.getValue(WAXED)) {
            return handleInWorldBlockCraft(pState, pState.setValue(WAXED, true), pLevel, pPos, item, 1, ParticleTypes.WAX_ON, SoundEvents.HONEYCOMB_WAX_ON);
        }
        if (item.getItem() instanceof AxeItem && pState.getValue(WAXED)) {
            item.hurtAndBreak(1, pPlayer, player1 -> player1.broadcastBreakEvent(InteractionHand.MAIN_HAND));
            return handleInWorldBlockCraft(pState, pState.setValue(WAXED, false), pLevel, pPos, item, 0, ParticleTypes.WAX_OFF, SoundEvents.AXE_WAX_OFF);
        }
        return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
    }

    //returns null if no port, true if clockwise, false if counterclockwise
    public static @Nullable Boolean isBlockAttached(Level level, BlockState blockState, BlockPos pos, Block... blocks) {
        Direction.Axis axis = blockState.getValue(AXIS);
        if (axis.isVertical()) return null;

        for (Block block : blocks) {
            if (level.getBlockState(pos.relative(Direction.get(Direction.AxisDirection.POSITIVE, axis).getClockWise()))
                    .is(block))
                return true;
            if (level.getBlockState(pos.relative(Direction.get(Direction.AxisDirection.POSITIVE, axis).getCounterClockWise()))
                    .is(block))
                return false;
        }

        return null;
    }

    public static @Nullable Boolean isPortAttached(Level level, BlockState blockState, BlockPos pos) {
        return isBlockAttached(level, blockState, pos, TCBlocks.MATTER_INFUSER_PORT.get());
    }

    @SuppressWarnings("unused")
    public static @Nullable Boolean isUnitAttached(Level level, BlockState blockState, BlockPos pos) {
        return isBlockAttached(level, blockState, pos, TCBlocks.MATTER_INFUSER_UNIT.get());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(AXIS, INFUSED, WAXED, ATTACHED_DIR);
    }

    @Override
    public @org.jetbrains.annotations.Nullable BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState().setValue(AXIS, pContext.getClickedFace().getAxis());
    }

    @Override
    protected BlockEntityType<? extends TCBlockEntity> getBlockEntityType() {
        return TCBlockEntities.FLOW_CEDAR_CASING_BE.get();
    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {

        Direction attachedDir = pState.getValue(ATTACHED_DIR);
        if (!pState.getBlock().equals(pNewState.getBlock())
                && attachedDir.getAxis().isHorizontal()) {
            pLevel.destroyBlock(pPos.relative(attachedDir), true);
        }

        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
    }


    @SuppressWarnings("deprecation")
    @Override
    public BlockState rotate(BlockState pState, Rotation pRotation) {
        return pState.setValue(AXIS,pRotation.rotate(Direction.get(Direction.AxisDirection.POSITIVE,pState.getValue(AXIS))).getAxis());
    }
}
