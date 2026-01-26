package net.sinedkadis.terracompositio.block.behaviours.pp_behaviours;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.sinedkadis.terracompositio.block.entity.PathPointerBlockEntity;

public class SenderBehaviour extends AbstractPPBehaviour{

    @Getter
    @Setter
    private BlockPos bindPos;

    public SenderBehaviour(PathPointerBlockEntity blockEntity) {
        super(blockEntity);
    }

    @Override
    public void onRemoved() {
        Level level = blockEntity.getLevel();
        if (level != null) {
            BlockEntity receiverEntity = level.getBlockEntity(bindPos);
            if (receiverEntity instanceof PathPointerBlockEntity pathPointerBlockEntity) {
                pathPointerBlockEntity.getBehaviours().forEach(ibeBehaviour -> {
                    if (ibeBehaviour instanceof ReceiverBehaviour receiverBehaviour) {
                        receiverBehaviour.getSenderPoses().remove(blockEntity.getBlockPos());
                    }
                });
            }
        }
        super.onRemoved();
    }
}
