package net.sinedkadis.terracompositio.block.behaviours.pp_behaviours;

import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.sinedkadis.terracompositio.api.TerraCompositioAPI;
import net.sinedkadis.terracompositio.api.behaviors.blockentity.IBEBehaviour;
import net.sinedkadis.terracompositio.api.dummies.DummyBehaviour;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetworkMember;
import net.sinedkadis.terracompositio.block.behaviours.CFEHandlerBehaviour;
import net.sinedkadis.terracompositio.block.entity.PathPointerBlockEntity;
import net.sinedkadis.terracompositio.cfe.CFEContainer;

import java.util.ArrayList;
import java.util.List;

public abstract class PPOutputBehaviour extends AbstractPPBehaviour {
    @Getter
    List<BlockPos> toNotify = new ArrayList<>();
    public PPOutputBehaviour(PathPointerBlockEntity blockEntity) {
        super(blockEntity);
    }

    @Override
    protected void addCFEBehaviour() {
        List<IBEBehaviour> list = blockEntity.getBehaviours();
        while (list.size()<3) list.add(DummyBehaviour.instance);
        list.set(2, new OutputCfeBehaviour(list));
    }

    protected void validateCFEBehaviour() {
        if (blockEntity.getBehaviours().stream().noneMatch(ibeBehaviour -> ibeBehaviour instanceof OutputCfeBehaviour)) {
            addCFEBehaviour();
        }
    }

    protected class OutputCfeBehaviour extends CFEHandlerBehaviour {
        private final List<IBEBehaviour> list;

        public OutputCfeBehaviour(List<IBEBehaviour> list) {
            super(PPOutputBehaviour.this.blockEntity);
            this.list = list;
        }

        @Override
        public void init() {
            this.setCfeHandler(new CFEContainer(this).setCfeTravelSpeed((float) 5 / 20));
            setMaxCFE(100);
        }

        @Override
        public int getPriority() {
            return -100;
        }

        @Override
        public int getLimit() {
            return 5;
        }

        @Override
        public void scheduleMemberUpdate() {
            super.scheduleMemberUpdate();
            toNotify.forEach(blockPos -> {
                CFENetworkMember memberAt = TerraCompositioAPI.instance().getCFENetworkInstance().getMemberAt(blockEntity.getLevel(), blockPos);
                if (memberAt != null) {
                    memberAt.scheduleMemberUpdate();
                }
            });
        }

        @Override
        public void onCFENetworkMemberUpdate() {
            if (blockEntity.getLevel() == null) return;
            list.forEach(IBEBehaviour::onUpdate);
        }
    }
}
