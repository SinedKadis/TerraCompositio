package net.sinedkadis.terracompositio.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.sinedkadis.terracompositio.block.behaviours.CFEHandlerBehaviour;
import net.sinedkadis.terracompositio.api.behaviors.blockentity.IBEBehaviour;
import net.sinedkadis.terracompositio.cfe.CFEContainer;
import net.sinedkadis.terracompositio.registries.TCBlockEntities;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CreativeCFESourceBlockEntity extends TCBlockEntity{

    public CreativeCFESourceBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.CREATIVE_CFE_SOURCE_BE.get(), pos, state);

    }

    @Override
    public void addBEBehaviours(@NotNull List<IBEBehaviour> list) {
        list.add(new CFEHandlerBehaviour(this){
            @Override
            public void init() {
                setCfeHandler(new CFEContainer(this) {
                    @Override
                    public int getCFE() {
                        return Integer.MAX_VALUE;
                    }
                });
                setPriority(-100);
            }
        });
    }
}
