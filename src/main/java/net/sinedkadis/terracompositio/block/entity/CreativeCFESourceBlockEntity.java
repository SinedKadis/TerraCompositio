package net.sinedkadis.terracompositio.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.sinedkadis.terracompositio.cfe.LimitlessCFEContainer;
import net.sinedkadis.terracompositio.registries.TCBlockEntities;

public class CreativeCFESourceBlockEntity extends TCCFEBlockEntity{

    public CreativeCFESourceBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.CREATIVE_CFE_SOURCE_BE.get(), pos, state, Integer.MAX_VALUE, 5, BlockMode.SOURCE);
        setCfeContainer(new LimitlessCFEContainer(this){
            @Override
            public int getCFE() {
                return Integer.MAX_VALUE;
            }
        });
    }

}
