package net.sinedkadis.terracompositio.block.behaviours.pp;

import net.sinedkadis.terracompositio.block.entity.PathPointerBlockEntity;

public class CollectorBehaviour extends PPInputBehaviour{

    public CollectorBehaviour(PathPointerBlockEntity blockEntity) {
        super(blockEntity);
    }

    @Override
    public void init() {
        validateCFEBehaviour();
    }

    @Override
    public void onUpdate() {
        if (invalidBehaviours()) return;
        updateMaxCFE();
        collectCFE();
        sendCFE();
    }


}
