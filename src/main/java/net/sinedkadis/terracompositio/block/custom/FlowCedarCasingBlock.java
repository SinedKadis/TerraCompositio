package net.sinedkadis.terracompositio.block.custom;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.sinedkadis.terracompositio.block.entity.TCBlockEntity;
import net.sinedkadis.terracompositio.registries.TCBlockEntities;
import net.sinedkadis.terracompositio.registries.TCBlockStateProperties;
import net.sinedkadis.terracompositio.registries.TCBlocks;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class FlowCedarCasingBlock extends TCBaseEntityBlock {
    public static final BooleanProperty INFUSED = TCBlockStateProperties.INFUSED;
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;
    protected static final BooleanProperty WAXED = TCBlockStateProperties.WAXED;

    public FlowCedarCasingBlock(Properties pProperties) {
        super(pProperties);
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
        return isBlockAttached(level, blockState, pos, TCBlocks.MATTER_INFUSER_IO.get());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(AXIS, INFUSED, WAXED);
    }


    @Override
    protected BlockEntityType<? extends TCBlockEntity> getBlockEntityType() {
        return TCBlockEntities.FLOW_CEDAR_CASING_BE.get();
    }
}
