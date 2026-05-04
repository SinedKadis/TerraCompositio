package net.sinedkadis.terracompositio.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.sinedkadis.terracompositio.block.behaviours.CFEHandlerBehaviour;
import net.sinedkadis.terracompositio.api.behaviors.blockentity.IBEBehaviour;
import net.sinedkadis.terracompositio.registries.TCBlockEntities;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CFETrashCanBlockEntity extends TCBlockEntity {

    public CFETrashCanBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(TCBlockEntities.CFE_TRASH_CAN_BE.get(),pPos, pBlockState);
    }

    @Override
    public void addBEBehaviours(@NotNull List<IBEBehaviour> list) {
        list.add(new CFEHandlerBehaviour(this)
            .priority(Integer.MAX_VALUE)
                .maxCFE(Integer.MAX_VALUE)
                .range(10)
        );
    }
}
