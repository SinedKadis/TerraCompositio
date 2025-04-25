package net.sinedkadis.terracompositio.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.sinedkadis.terracompositio.registries.ModBlockEntities;
import net.sinedkadis.terracompositio.registries.ModBlockStateProperties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ConstructionDesorberBlock extends AbstractDesorberBlock{
    private static final BooleanProperty INFUSED;

    public ConstructionDesorberBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {

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

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
        return ModBlockEntities.CONSTRUCTION_DESORBER_BE.get().create(blockPos,blockState);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(INFUSED);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level pLevel, @NotNull BlockState pState, @NotNull BlockEntityType<T> pBlockEntityType) {
        if (pLevel.isClientSide()) {
            return null;
        }
        return createTickerHelper(pBlockEntityType, ModBlockEntities.CONSTRUCTION_DESORBER_BE.get(),
                (pLevel1, pPos, pState1, pBlockEntity) -> pBlockEntity.tick(pLevel1,pPos,pState1));
    }

    static {
        INFUSED = ModBlockStateProperties.INFUSED;
    }
}
