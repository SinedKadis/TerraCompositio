package net.sinedkadis.terracompositio.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.sinedkadis.terracompositio.block.behaviours.ECFHandlerBehaviour;
import net.sinedkadis.terracompositio.api.behaviors.blockentity.IBEBehaviour;
import net.sinedkadis.terracompositio.ecf.ECFContainer;
import net.sinedkadis.terracompositio.config.TCInnerConfig;
import net.sinedkadis.terracompositio.registries.TCBlockEntities;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CreativeECFSourceBlockEntity extends TCBlockEntity{

    public CreativeECFSourceBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.CREATIVE_ECF_SOURCE_BE.get(), pos, state);

    }

    @Override
    public void addBEBehaviours(@NotNull List<IBEBehaviour> list) {
        list.add(new ECFHandlerBehaviour(this)
                .cfeHandler(cfeHandlerBehaviour -> new ECFContainer(cfeHandlerBehaviour) {
                        @Override
                        public int getCFE() {
                            return Integer.MAX_VALUE;
                        }
                    })
                .priority(TCInnerConfig.DEFAULT_SOURCE_PRIORITY));
    }
}
