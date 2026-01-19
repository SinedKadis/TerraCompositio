package net.sinedkadis.terracompositio.block.custom;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.sinedkadis.terracompositio.block.entity.TCBlockEntity;
import net.sinedkadis.terracompositio.registries.TCBlockEntities;
import net.sinedkadis.terracompositio.registries.TCBlockStateProperties;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CedarGearboxBlock extends TCBaseEntityBlock {
    public CedarGearboxBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    protected BlockEntityType<? extends TCBlockEntity> getBlockEntityType() {
        return TCBlockEntities.CEDAR_GEARBOX_BE.get();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(TCBlockStateProperties.INFUSED, BlockStateProperties.AXIS);
    }
}
