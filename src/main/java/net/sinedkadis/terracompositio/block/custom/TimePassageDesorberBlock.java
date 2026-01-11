package net.sinedkadis.terracompositio.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.sinedkadis.terracompositio.block.entity.TCBlockEntity;
import net.sinedkadis.terracompositio.registries.TCBlockEntities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

public class TimePassageDesorberBlock extends AbstractDesorberBlock{

    public TimePassageDesorberBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, @Nullable Direction direction) {
        return true;
    }
    @SuppressWarnings("deprecation")
    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState pState, @NotNull BlockGetter pLevel, @NotNull BlockPos pPos, @NotNull CollisionContext pContext) {
        return Stream.of(
                Block.box(0, 0, 0, 16, 2, 16),
                Block.box(0, 6, 0, 16, 8, 2),
                Block.box(0, 6, 14, 16, 8, 16),
                Block.box(0, 6, 2, 2, 8, 14),
                Block.box(14, 6, 2, 16, 8, 14),
                Block.box(1, 5, 1, 2, 6, 15),
                Block.box(14, 5, 1, 15, 6, 15),
                Block.box(2, 5, 1, 14, 6, 2),
                Block.box(2, 5, 14, 14, 6, 15),
                Block.box(0, 3, 0, 16, 5, 2),
                Block.box(0, 3, 14, 16, 5, 16),
                Block.box(0, 3, 2, 2, 5, 14),
                Block.box(14, 3, 2, 16, 5, 14),
                Block.box(1, 2, 1, 2, 3, 15),
                Block.box(14, 2, 1, 15, 3, 15),
                Block.box(2, 2, 1, 14, 3, 2),
                Block.box(2, 2, 14, 14, 3, 15)
        ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();
    }

    @Override
    protected @NotNull BlockEntityType<? extends TCBlockEntity> getBlockEntityType() {
        return TCBlockEntities.TIME_PASSAGE_DESORBER_BE.get();
    }

}
