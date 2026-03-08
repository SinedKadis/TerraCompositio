package net.sinedkadis.terracompositio.block.behaviours.pp;

import net.sinedkadis.terracompositio.block.entity.PathPointerBlockEntity;

public class CollectorBehaviour extends PPInputBehaviour{

    public CollectorBehaviour(PathPointerBlockEntity blockEntity) {
        super(blockEntity);
    }

    @Override
    public void onUpdate() {
        if (invalidBehaviours()) return;
        updateMaxCFE();
    }

    @Override
    public boolean isActive() {
        return blockEntity.parts.contains(PathPointerBlockEntity.PPPart.COLLECTOR);
    }
}
