package net.sinedkadis.terracompositio.block.behaviours.pp;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.LazyOptional;
import net.sinedkadis.terracompositio.api.TCCapabilities;
import net.sinedkadis.terracompositio.api.networks.cfe.ICFEHandler;
import net.sinedkadis.terracompositio.block.entity.PathPointerBlockEntity;

import java.util.Optional;

public abstract class PPInputBehaviour extends AbstractPPBehaviour{
    ICFEHandler thisCFEBehaviour;
    ICFEHandler endPointCFEBehaviour;

    public PPInputBehaviour(PathPointerBlockEntity blockEntity) {
        super(blockEntity);
    }

    protected void updateMaxCFE() {
        if (!thisCFEBehaviour.equals(endPointCFEBehaviour))
            thisCFEBehaviour.setMaxCFE(endPointCFEBehaviour.getFreeSpace());
    }

    protected boolean invalidBehaviours() {
        if (thisCFEBehaviour == null) {
            Optional<ICFEHandler> behaviour = blockEntity.getCapability(TCCapabilities.CFE).resolve();
            if (behaviour.isPresent()) {
                thisCFEBehaviour = behaviour.get();
            } else {
                return true;
            }
        }
        if (endPointCFEBehaviour == null) {
            BlockPos endpoint = getEndpoint();
            if (endpoint.equals(blockEntity.getBlockPos())){
                endPointCFEBehaviour = thisCFEBehaviour;
                return false;
            }

            if (blockEntity.getLevel() != null) {
                BlockEntity endPointBE = blockEntity.getLevel().getBlockEntity(endpoint);
                if (endPointBE != null) {
                    LazyOptional<ICFEHandler> behaviour = endPointBE.getCapability(TCCapabilities.CFE);
                    if (behaviour.isPresent()) {
                        behaviour.ifPresent(icfeHandler -> endPointCFEBehaviour = icfeHandler);
                    } else return true;
                } else return true;
            } else return true;
        }

        return false;
    }
}
