package net.sinedkadis.terracompositio.block.behaviours.pp_behaviours;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.sinedkadis.terracompositio.block.entity.PathPointerBlockEntity;
import net.sinedkadis.terracompositio.util.TCUtil;

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
        if (level != null && bindPos != null) {
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

    @Override
    public void onSave(CompoundTag compoundTag) {
        super.onSave(compoundTag);
        compoundTag.put("bindpos", TCUtil.saveBlockPos(bindPos));
    }

    @Override
    public void onLoad(CompoundTag compoundTag) {
        super.onLoad(compoundTag);
        bindPos = TCUtil.loadBlockPos(compoundTag.getCompound("bindpos"));
    }

    @Override
    public void onTagUpdate(CompoundTag compoundTag) {
        super.onTagUpdate(compoundTag);
        bindPos = TCUtil.loadBlockPos(compoundTag.getCompound("bindpos"));
    }
}
