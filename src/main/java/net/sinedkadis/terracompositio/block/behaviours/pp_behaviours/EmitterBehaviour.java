package net.sinedkadis.terracompositio.block.behaviours.pp_behaviours;

import net.sinedkadis.terracompositio.block.entity.PathPointerBlockEntity;

public class EmitterBehaviour extends PPOutputBehaviour{

    public EmitterBehaviour(PathPointerBlockEntity blockEntity) {
        super(blockEntity);
    }

    @Override
    public void init() {
        validateCFEBehaviour();
    }
}