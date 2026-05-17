package net.sinedkadis.terracompositio.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;
import net.sinedkadis.terracompositio.api.behaviors.blockentity.IBEBehaviour;
import net.sinedkadis.terracompositio.block.behaviours.TwoSlotItemHandlerBehaviour;
import net.sinedkadis.terracompositio.registries.TCBlockEntities;
import org.jetbrains.annotations.NotNull;

import java.util.List;


public class MatterInfuserPortBlockEntity extends MatterInfuserBaseBlockEntity {
    public MatterInfuserPortBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(TCBlockEntities.MATTER_INFUSER_PORT_BE.get(),pPos, pBlockState);
    }

    @Override
    protected ItemStackHandler getItemHandler() {
        FlowCedarCasingBlockEntity casingBE = getCasingBE();
        if (casingBE != null)
            return ((TwoSlotItemHandlerBehaviour) casingBE.getBehaviours().get(1)).getItemHandler();
        return null;
    }

    @Override
    public void addBEBehaviours(@NotNull List<IBEBehaviour> list) {

    }
}
