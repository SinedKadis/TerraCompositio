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
import net.sinedkadis.terracompositio.util.FunctionSide;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class FlowCedarCasingBlock extends TCBaseEntityBlock {
    public static final BooleanProperty INFUSED = TCBlockStateProperties.INFUSED;
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;
    protected static final BooleanProperty FUNCTION_SIDE = TCBlockStateProperties.FUNCTION_SIDE;
    protected static final BooleanProperty WAXED = TCBlockStateProperties.WAXED;

    public FlowCedarCasingBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(AXIS, FUNCTION_SIDE, INFUSED, WAXED);
    }

    public static boolean isPortAttached(Level level, BlockState blockState, BlockPos pos) {
        Direction facing = FunctionSide.getDirectionByFunctionSide(blockState);
        if (facing != Direction.DOWN){
            return level.getBlockState(pos.relative(facing)).is(TCBlocks.MATTER_INFUSER_PORT.get());
        }
        return false;
    }

    @SuppressWarnings("unused")
    public static boolean isUnitAttached(Level level, BlockState blockState, BlockPos pos) {
        Direction facing = FunctionSide.getDirectionByFunctionSide(blockState);
        if (facing != Direction.DOWN){
            return level.getBlockState(pos.relative(facing)).is(TCBlocks.MATTER_INFUSER_IO.get());
        }
        return false;
    }


    @Override
    protected BlockEntityType<? extends TCBlockEntity> getBlockEntityType() {
        return TCBlockEntities.FLOW_CEDAR_CASING_BE.get();
    }
}
