package net.sinedkadis.terracompositio.compat.patchouli;

import com.google.common.base.Suppliers;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.sinedkadis.terracompositio.registries.TCBlocks;
import vazkii.patchouli.api.IMultiblock;
import vazkii.patchouli.api.IStateMatcher;
import vazkii.patchouli.api.PatchouliAPI;
import vazkii.patchouli.api.TriPredicate;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TCMultiblocks {
    public static final Supplier<IMultiblock> MATTER_INFUSER_MB = Suppliers.memoize(() -> {

        var casing = new AxisMatcher(TCBlocks.FLOW_CEDAR_CASING.get(), Direction.Axis.X);
        var io = new DirectionMatcher(TCBlocks.MATTER_INFUSER_IO.get(), Direction.get(Direction.AxisDirection.POSITIVE,Direction.Axis.Z));
        var port = new DirectionMatcher(TCBlocks.MATTER_INFUSER_PORT.get(), Direction.get(Direction.AxisDirection.POSITIVE,Direction.Axis.Z));

        return PatchouliAPI.get().makeMultiblock(
                new String[][] {
                        {
                            "C0",
                            "CI",
                            "CI"
                        }
                },
                'C', casing,
                'I', io,
                '0', port
        );
    });

    record AxisMatcher(Block defaultBlock, Direction.Axis displayedRotation) implements IStateMatcher {
        @Override
        public BlockState getDisplayedState(long ticks) {


            BlockState block = defaultBlock.defaultBlockState();


            return block.hasProperty(BlockStateProperties.AXIS)
                    ? block.setValue(BlockStateProperties.AXIS, displayedRotation())
                    : block;
        }

        @Override
        public TriPredicate<BlockGetter, BlockPos, BlockState> getStatePredicate() {
            return (blockGetter, pos, state) -> state.is(AxisMatcher.this.defaultBlock);
        }
    }

    record DirectionMatcher(Block defaultBlock, Direction displayedRotation) implements IStateMatcher {
        @Override
        public BlockState getDisplayedState(long ticks) {


            BlockState block = defaultBlock.defaultBlockState();


            return block.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
                    ? block.setValue(BlockStateProperties.HORIZONTAL_FACING, displayedRotation())
                    : block;
        }

        @Override
        public TriPredicate<BlockGetter, BlockPos, BlockState> getStatePredicate() {
            return (blockGetter, pos, state) -> state.is(this.defaultBlock);

        }
    }
}