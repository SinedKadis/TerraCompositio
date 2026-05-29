package net.sinedkadis.terracompositio.compat.patchouli;

import com.google.common.base.Suppliers;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
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
}