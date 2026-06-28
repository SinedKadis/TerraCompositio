package net.sinedkadis.terracompositio.compat.create.block.custom;

import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.registries.RegistryObject;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.block.IFluidApplicable;
import net.sinedkadis.terracompositio.compat.create.TCCreateCompat;
import net.sinedkadis.terracompositio.compat.create.block.entity.CedarGearboxBlockEntity;
import net.sinedkadis.terracompositio.api.registries.TCBlockStateProperties;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CedarGearboxBlock extends RotatedPillarKineticBlock implements IBE<CedarGearboxBlockEntity>, IFluidApplicable {
    public CedarGearboxBlock(Properties pProperties) {
        super(pProperties);
        registerDefaultState(defaultBlockState().setValue(TCBlockStateProperties.INFUSED, false));
    }

    @Override
    public Class<CedarGearboxBlockEntity> getBlockEntityClass() {
        return CedarGearboxBlockEntity.class;
    }


    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        VoxelShape SHAPE_Z = Shapes.or(
                Shapes.box(0.125, 0.125, 0.0625, 0.875, 0.875, 0.9375),

                Shapes.box(0,     0,     0, 0.125, 1, 1),
                Shapes.box(0.875, 0,     0, 1,     1, 1),

                Shapes.box(0.125, 0,     0, 0.875, 0.125, 1),
                Shapes.box(0.125, 0.875, 0, 0.875, 1,     1)
        );
        VoxelShape SHAPE_X = Shapes.or(
                Shapes.box(0.0625, 0.125, 0.125, 0.9375, 0.875, 0.875),

                Shapes.box(0, 0,     0, 1, 1, 0.125),
                Shapes.box(0, 0, 0.875, 1, 1, 1),

                Shapes.box(0, 0,     0.125, 1, 0.125, 0.875),
                Shapes.box(0, 0.875, 0.125, 1, 1,     0.875)
        );
        VoxelShape SHAPE_Y = Shapes.or(
                Shapes.box(0.125, 0.0625, 0.125, 0.875, 0.9375, 0.875),

                Shapes.box(0,     0, 0, 0.125, 1, 1),
                Shapes.box(0.875, 0, 0, 1,     1, 1),

                Shapes.box(0.125, 0,     0, 0.875, 1, 0.125),
                Shapes.box(0.125, 0, 0.875, 0.875, 1, 1)
        );
        return switch (pState.getValue(AXIS)) {
            case X -> SHAPE_X;
            case Y -> SHAPE_Y;
            case Z -> SHAPE_Z;
        };

    }

    @Override
    public BlockEntityType<? extends CedarGearboxBlockEntity> getBlockEntityType() {
        RegistryObject<BlockEntityType<CedarGearboxBlockEntity>> cedarGearboxBe =
                ((TCCreateCompat) TerraCompositio.createCompat).blockEntities.CEDAR_GEARBOX_BE;
        assert cedarGearboxBe != null;
        return cedarGearboxBe.get();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(TCBlockStateProperties.INFUSED, AXIS);
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(AXIS);
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return state.getValue(AXIS) == face.getAxis();
    }
}
