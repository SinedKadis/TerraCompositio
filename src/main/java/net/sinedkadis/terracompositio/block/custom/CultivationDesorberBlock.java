package net.sinedkadis.terracompositio.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.sinedkadis.terracompositio.registries.ModBlockEntities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CultivationDesorberBlock extends AbstractDesorberBlock{

    public CultivationDesorberBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState pState, @NotNull BlockGetter pLevel, @NotNull BlockPos pPos, @NotNull CollisionContext pContext) {

        VoxelShape base = Block.box(0, 0, 0, 16, 2, 16);
        VoxelShape northWall = Block.box(0, 3, 0, 16, 5, 2);
        VoxelShape southWall = Block.box(0, 3, 14, 16, 5, 16);
        VoxelShape westWall = Block.box(0, 3, 2, 2, 5, 14);
        VoxelShape eastWall = Block.box(14, 3, 2, 16, 5, 14);
        VoxelShape edge1 = Block.box(1, 2, 1, 2, 3, 15);
        VoxelShape edge2 = Block.box(14, 2, 1, 15, 3, 15);
        VoxelShape edge3 = Block.box(2, 2, 1, 14, 3, 2);
        VoxelShape edge4 = Block.box(2, 2, 14, 14, 3, 15);
        VoxelShape pillar1 = Block.box(2, 2, 2, 4, 14, 4);
        VoxelShape pillar2 = Block.box(12, 2, 2, 14, 14, 4);
        VoxelShape pillar3 = Block.box(12, 2, 12, 14, 14, 14);
        VoxelShape pillar4 = Block.box(2, 2, 12, 4, 14, 14);

        return Shapes.or(
                base,
                northWall,
                southWall,
                westWall,
                eastWall,
                edge1,
                edge2,
                edge3,
                edge4,
                pillar1,
                pillar2,
                pillar3,
                pillar4
        );
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
        return ModBlockEntities.CULTIVATION_DESORBER_BE.get().create(blockPos,blockState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level pLevel, @NotNull BlockState pState, @NotNull BlockEntityType<T> pBlockEntityType) {
        if (pLevel.isClientSide()) {
            return null;
        }
        return createTickerHelper(pBlockEntityType, ModBlockEntities.CULTIVATION_DESORBER_BE.get(),
                (pLevel1, pPos, pState1, pBlockEntity) -> pBlockEntity.tick(pLevel1,pPos,pState1));
    }
}
