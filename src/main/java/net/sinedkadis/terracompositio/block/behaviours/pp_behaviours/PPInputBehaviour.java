package net.sinedkadis.terracompositio.block.behaviours.pp_behaviours;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.sinedkadis.terracompositio.api.TerraCompositioAPI;
import net.sinedkadis.terracompositio.api.behaviors.blockentity.IBEBehaviour;
import net.sinedkadis.terracompositio.api.behaviors.blockentity.IBECFEBehaviour;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetwork;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetworkMember;
import net.sinedkadis.terracompositio.api.networks.cfe.ICFEHandler;
import net.sinedkadis.terracompositio.block.behaviours.CFEHandlerBehaviour;
import net.sinedkadis.terracompositio.block.entity.PathPointerBlockEntity;
import net.sinedkadis.terracompositio.block.entity.TCBlockEntity;
import net.sinedkadis.terracompositio.cfe.CFEContainer;
import net.sinedkadis.terracompositio.cfe.burst.CFEBurstProjectileEntity;
import net.sinedkadis.terracompositio.util.TCUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public abstract class PPInputBehaviour extends AbstractPPBehaviour{
    IBECFEBehaviour thisCFEBehaviour;
    IBECFEBehaviour endPointCFEBehaviour;

    public PPInputBehaviour(PathPointerBlockEntity blockEntity) {
        super(blockEntity);
    }

    protected void sendCFE() {
        Optional<SenderBehaviour> senderOpt = blockEntity.getBehaviours().stream()
                .map(ibeBehaviour -> ibeBehaviour instanceof SenderBehaviour senderBehaviour ? senderBehaviour : null)
                .filter(Objects::nonNull)
                .findAny();
        if (senderOpt.isPresent()) {
            int added = TCUtil.tryCFETransfer(endPointCFEBehaviour.getBlockEntity().getBlockPos(),
                    thisCFEBehaviour,
                    endPointCFEBehaviour.getCfeHandler().getFreeSpace());
            endPointCFEBehaviour.getCfeHandler().addToQueue(added);
        }
    }

    protected void collectCFE() {
        CFENetwork cfeNetwork = TerraCompositioAPI.instance().getCFENetworkInstance();
        CFENetworkMember source = cfeNetwork.getClosestSourceWithCFE(blockEntity.getBlockPos(),
                blockEntity.getLevel(),
                thisCFEBehaviour.getLimit() * 2,
                thisCFEBehaviour.getPriority());
        if (source != null) {
            TCUtil.tryCFETransfer(thisCFEBehaviour, source, thisCFEBehaviour.getCfeHandler().getFreeSpace());
        }
    }

    protected void updateMaxCFE() {
        if (!thisCFEBehaviour.equals(endPointCFEBehaviour))
            thisCFEBehaviour.getCfeHandler().setMaxCFE(endPointCFEBehaviour.getCfeHandler().getFreeSpace());
    }

    protected void validateCFEBehaviour() {
        if (blockEntity.cfeBehaviour() == null) {
            addInputCFEBehaviour();
        } else {
            addInputOutputCFEBehaviour();
        }
    }

    protected boolean invalidBehaviours() {
        if (thisCFEBehaviour == null) {
            Optional<IBECFEBehaviour> behaviour = TCBlockEntity.getBehaviours(blockEntity).stream()
                    .map(ibeBehaviour -> ibeBehaviour instanceof IBECFEBehaviour ibecfeBehaviour ? ibecfeBehaviour : null)
                    .filter(Objects::nonNull)
                    .findAny();
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
            Optional<IBECFEBehaviour> behaviour = TCBlockEntity.getBehaviours(blockEntity, endpoint).stream()
                    .map(ibeBehaviour -> ibeBehaviour instanceof IBECFEBehaviour ibecfeBehaviour ? ibecfeBehaviour : null)
                    .filter(Objects::nonNull)
                    .findAny();
            if (behaviour.isPresent()) {
                endPointCFEBehaviour = behaviour.get();
            } else {
                return true;
            }
        }

        return false;
    }

    protected void addInputCFEBehaviour() {
        List<IBEBehaviour> list = blockEntity.getBehaviours();
        list.set(2,new CFEHandlerBehaviour(blockEntity) {
            @Override
            public void init() {
                this.setCfeHandler(new CFEContainer(this){
                    @Override
                    public int sendCFE(int cfe, @NotNull ICFEHandler target, boolean simulate) {
                        int freeSpace = target.getFreeSpace();
                        int available = this.getCFE();
                        int added = Mth.clamp(cfe, 0, Math.min(available, freeSpace));
                        if (added < 1)
                            return 0;

                        if (!simulate) {
                            CFEBurstProjectileEntity entity = CFEBurstProjectileEntity.sendBurst(this, target, added, target.getCfeTravelSpeed());
                            if (entity != null)
                                target.addToQueue(added);
                        }
                        return added;
                    }
                }.setCfeTravelSpeed((float) 5 / 20));
            }

            @Override
            public int getPriority() {
                return 100;
            }

            @Override
            public int getLimit() {
                return 5;
            }

            @Override
            public void onCFENetworkMemberUpdate() {
                if (blockEntity.getLevel() == null) return;
                list.forEach(IBEBehaviour::onUpdate);
            }
        });
    }
}
