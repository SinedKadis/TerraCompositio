package net.sinedkadis.terracompositio.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.sinedkadis.terracompositio.block.behaviours.ECFHandlerBehaviour;
import net.sinedkadis.terracompositio.registries.TCBlockEntities;
import net.sinedkadis.terracompositio.util.behaviors.blockentity.IBEBehaviour;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ECFTrashCanBlockEntity extends TCBlockEntity {

    public ECFTrashCanBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(TCBlockEntities.ECF_TRASH_CAN_BE.get(),pPos, pBlockState);
    }

    @Override
    public void addBEBehaviours(@NotNull List<IBEBehaviour> list) {
        list.add(new ECFHandlerBehaviour(this)
            .priority(Integer.MAX_VALUE)
                .maxECF(Integer.MAX_VALUE)
                .range(10)
        );
    }
}
