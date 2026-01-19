package net.sinedkadis.terracompositio.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.sinedkadis.terracompositio.api.behaviors.blockentity.IBEBehaviour;
import net.sinedkadis.terracompositio.registries.TCBlockEntities;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CedarGearboxBlockEntity extends TCBlockEntity {

    public CedarGearboxBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(TCBlockEntities.CEDAR_GEARBOX_BE.get(),pPos, pBlockState);
    }

    @Override
    void addBehaviours(@NotNull List<IBEBehaviour> list) {

    }





}
