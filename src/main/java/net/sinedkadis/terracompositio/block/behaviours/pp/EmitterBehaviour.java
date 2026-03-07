package net.sinedkadis.terracompositio.block.behaviours.pp;

import net.sinedkadis.terracompositio.block.entity.PathPointerBlockEntity;

public class EmitterBehaviour extends PPOutputBehaviour{

    public EmitterBehaviour(PathPointerBlockEntity blockEntity) {
        super(blockEntity);
    }

    @Override
    public boolean isActive() {
        return blockEntity.parts.contains(PathPointerBlockEntity.PPPart.EMITTER);
    }
}