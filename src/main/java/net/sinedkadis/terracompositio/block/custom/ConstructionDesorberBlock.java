package net.sinedkadis.terracompositio.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.sinedkadis.terracompositio.block.entity.TCBlockEntity;
import net.sinedkadis.terracompositio.registries.TCBlockEntities;
import org.jetbrains.annotations.NotNull;

public class ConstructionDesorberBlock extends AbstractDesorberBlock{

    public ConstructionDesorberBlock(Properties pProperties) {
        super(pProperties);
    }

    @SuppressWarnings("deprecation")
    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState pState, @NotNull BlockGetter pLevel, @NotNull BlockPos pPos, @NotNull CollisionContext pContext) {

        VoxelShape base = Block.box(0, 0, 0, 16, 2, 16);
        VoxelShape northWall = Block.box(0, 3, 0, 16, 5, 2);
        VoxelShape southWall = Block.box(0, 3, 14, 16, 5, 16);
        VoxelShape westWall = Block.box(0, 3, 2, 2, 5, 14);
        VoxelShape eastWall = Block.box(14, 3, 2, 16, 5, 14);
        VoxelShape pillar = Block.box(6, 2, 6, 10, 8, 10);
        VoxelShape edge1 = Block.box(1, 2, 1, 2, 3, 15);
        VoxelShape edge2 = Block.box(14, 2, 1, 15, 3, 15);
        VoxelShape edge3 = Block.box(2, 2, 1, 14, 3, 2);
        VoxelShape edge4 = Block.box(2, 2, 14, 14, 3, 15);

        return Shapes.or(
                base,
                northWall,
                southWall,
                westWall,
                eastWall,
                pillar,
                edge1,
                edge2,
                edge3,
                edge4
        );
    }

    @Override
    @NotNull
    public BlockEntityType<? extends TCBlockEntity> getBlockEntityType() {
        return TCBlockEntities.CONSTRUCTION_DESORBER_BE.get();
    }
}
