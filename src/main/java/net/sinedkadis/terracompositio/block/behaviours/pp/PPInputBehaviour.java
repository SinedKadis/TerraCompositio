package net.sinedkadis.terracompositio.block.behaviours.pp;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.sinedkadis.terracompositio.api.TCCapabilities;
import net.sinedkadis.terracompositio.api.TerraCompositioAPI;
import net.sinedkadis.terracompositio.api.behaviors.blockentity.IBEBehaviour;
import net.sinedkadis.terracompositio.api.dummies.DummyBehaviour;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetwork;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetworkMember;
import net.sinedkadis.terracompositio.api.networks.cfe.ICFEHandler;
import net.sinedkadis.terracompositio.block.behaviours.CFEHandlerBehaviour;
import net.sinedkadis.terracompositio.block.entity.PathPointerBlockEntity;
import net.sinedkadis.terracompositio.cfe.CFEContainer;
import net.sinedkadis.terracompositio.cfe.burst.CFEBurstProjectileEntity;
import net.sinedkadis.terracompositio.util.TCUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public abstract class PPInputBehaviour extends AbstractPPBehaviour{
    ICFEHandler thisCFEBehaviour;
    ICFEHandler endPointCFEBehaviour;

    public PPInputBehaviour(PathPointerBlockEntity blockEntity) {
        super(blockEntity);
    }

    protected void sendCFE() {
        if (blockEntity.parts.contains(PathPointerBlockEntity.PPPart.SENDER)) {
            int added = TCUtil.tryCFETransfer(endPointCFEBehaviour.getEntity().getBlockPos(),
                    thisCFEBehaviour.getAttachedMember(),
                    endPointCFEBehaviour.getFreeSpace());
            endPointCFEBehaviour.addToQueue(added);
        }
    }

    protected void collectCFE() {
        CFENetwork cfeNetwork = TerraCompositioAPI.instance().getCFENetworkInstance();
        CFENetworkMember source = cfeNetwork.getClosestSourceWithCFE(blockEntity.getBlockPos(),
                blockEntity.getLevel(),
                thisCFEBehaviour.getAttachedMember().getLimit() * 2,
                thisCFEBehaviour.getAttachedMember().getPriority());
        if (source != null) {
            TCUtil.tryCFETransfer(thisCFEBehaviour.getAttachedMember(), source, thisCFEBehaviour.getFreeSpace());
        }
    }

    protected void updateMaxCFE() {
        if (!thisCFEBehaviour.equals(endPointCFEBehaviour))
            thisCFEBehaviour.setMaxCFE(endPointCFEBehaviour.getFreeSpace());
    }

    protected void validateCFEBehaviour() {
        if (blockEntity.getBehaviours().stream().noneMatch(ibeBehaviour -> ibeBehaviour instanceof InputCFEBehaviour)) {
            setCFEBehaviour();
        }
    }

    @Override
    protected void setCFEBehaviour() {
        List<IBEBehaviour> list = blockEntity.getBehaviours();
        while (list.size()<3) list.add(DummyBehaviour.instance);
        list.set(2, new InputCFEBehaviour(list));
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
                    Optional<ICFEHandler> behaviour = endPointBE.getCapability(TCCapabilities.CFE).resolve();
                    if (behaviour.isPresent()) {
                        endPointCFEBehaviour = behaviour.get();
                    } else {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private class InputCFEBehaviour extends CFEHandlerBehaviour {
        private final List<IBEBehaviour> list;

        public InputCFEBehaviour(List<IBEBehaviour> list) {
            super(PPInputBehaviour.this.blockEntity);
            this.list = list;
        }

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

                @Override
                public int addCFE(int cfe, boolean simulate) {
                    int i = super.addCFE(cfe, simulate);
                    blockEntity.scheduleMemberUpdate();
                    return i;
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
    }
}
