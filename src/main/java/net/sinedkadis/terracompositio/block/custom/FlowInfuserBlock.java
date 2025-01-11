package net.sinedkadis.terracompositio.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.sinedkadis.terracompositio.block.entity.FlowInfuserBlockEntity;
import net.sinedkadis.terracompositio.block.entity.ModCFEBlockEntity;
import net.sinedkadis.terracompositio.block.entity.ModBlockEntities;
import net.sinedkadis.terracompositio.util.IBE;
import org.jetbrains.annotations.Nullable;

public class FlowInfuserBlock extends BaseEntityBlock implements IBE<FlowInfuserBlockEntity> {
    public FlowInfuserBlock(Properties pProperties) {
        super(pProperties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return ModBlockEntities.FLOW_INFUSER_BE.get().create(blockPos, blockState).markVirtual();
    }

    @Override
    public Class<FlowInfuserBlockEntity> getBlockEntityClass() {
        return FlowInfuserBlockEntity.class;
    }
}
