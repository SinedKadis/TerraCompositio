package net.sinedkadis.terracompositio.compat.patchouli;

import com.google.common.base.Suppliers;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.sinedkadis.terracompositio.registries.TCBlockStateProperties;
import net.sinedkadis.terracompositio.registries.TCBlocks;
import vazkii.patchouli.api.IMultiblock;
import vazkii.patchouli.api.PatchouliAPI;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TCMultiblocks {
    public static final Supplier<IMultiblock> MATTER_INFUSER_MB = Suppliers.memoize(() -> {

        var casing = TCBlocks.FLOW_CEDAR_CASING.get().defaultBlockState()
                .setValue(BlockStateProperties.AXIS, Direction.Axis.X)
                .setValue(TCBlockStateProperties.INFUSED,true);
        var io = TCBlocks.MATTER_INFUSER_UNIT.get().defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING,Direction.get(Direction.AxisDirection.POSITIVE,Direction.Axis.Z));
        var port = TCBlocks.MATTER_INFUSER_PORT.get().defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING,Direction.get(Direction.AxisDirection.POSITIVE,Direction.Axis.Z));

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
    public static final Supplier<IMultiblock> CREATION_ALTAR_MB = Suppliers.memoize(() -> {

        var altar = TCBlocks.FLOW_CEDAR_ALTAR.get().defaultBlockState()
                .setValue(TCBlockStateProperties.INFUSED, true);
        var pedestal = TCBlocks.FLOW_CEDAR_PEDESTAL.get().defaultBlockState();
        var mud = Blocks.MUD_BRICKS.defaultBlockState();
        BlockState stairs = Blocks.MUD_BRICK_STAIRS.defaultBlockState();
        BlockState[] mud_stairs = {
                stairs.setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH),
                stairs.setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST),
                stairs.setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH),
                stairs.setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.WEST)
        };
        var moss = Blocks.MOSS_BLOCK.defaultBlockState();

        return PatchouliAPI.get().makeMultiblock(
                new String[][]{
                        {
                                "   ",
                                " a ",
                                "   "
                        },
                        {
                                "   ",
                                " p ",
                                "   "
                        },
                        {
                                "MeM",
                                "s0n",
                                "MwM"
                        }
                },
                'M', mud,
                'n', mud_stairs[0],
                'e', mud_stairs[1],
                's', mud_stairs[2],
                'w', mud_stairs[3],
                'p', pedestal,
                'a', altar,
                '0', moss
        ).setSymmetrical(true);
    });
}