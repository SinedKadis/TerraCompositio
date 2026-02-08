package net.sinedkadis.terracompositio.block.behaviours.pp_behaviours;

import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.sinedkadis.terracompositio.block.entity.PathPointerBlockEntity;
import net.sinedkadis.terracompositio.util.TCUtil;

import java.util.ArrayList;
import java.util.List;

public class ReceiverBehaviour extends AbstractPPBehaviour{
    @Getter
    private final List<BlockPos> senderPoses = new ArrayList<>();
    public ReceiverBehaviour(PathPointerBlockEntity blockEntity) {
        super(blockEntity);
    }

    public boolean validAngle(BlockPos senderPos) {
        float rotationYaw = blockEntity.rotationYaw;
        float rotationPitch = blockEntity.rotationPitch;
        BlockPos currentPos = blockEntity.getBlockPos();

        Vec3 senderVec = currentPos.subtract(senderPos).getCenter().normalize();
        Vec3 currentVec = new Vec3(0, 0, -1).normalize().yRot(rotationYaw).xRot(rotationPitch);

        double angle = Math.acos(senderVec.dot(currentVec) / senderVec.length() / currentVec.length());

        return angle < Math.PI/2;
    }

    @Override
    public void onRemoved() {
        Level level = blockEntity.getLevel();
        if (level != null) {
            senderPoses.forEach(blockPos -> {
                BlockEntity senderEntity = level.getBlockEntity(blockPos);
                if (senderEntity instanceof PathPointerBlockEntity pathPointerBlockEntity) {
                    pathPointerBlockEntity.getBehaviours().forEach(ibeBehaviour -> {
                        if (ibeBehaviour instanceof SenderBehaviour senderBehaviour) {
                            senderBehaviour.setBindPos(null);
                        }
                    });
                }
            });
        }
        super.onRemoved();
    }

    @Override
    public void onSave(CompoundTag compoundTag) {
        super.onSave(compoundTag);

        int size = senderPoses.size();
        getBlockEntity().getPersistentData().putInt("SenderCount",size);
        for (int i = 0; i < size; i++) {
            BlockPos blockPos = senderPoses.get(i);
            getBlockEntity().getPersistentData().put("SenderPos_"+i, TCUtil.saveBlockPos(blockPos));
        }
    }

    @Override
    public void onLoad(CompoundTag compoundTag) {
        super.onLoad(compoundTag);
        int size = getBlockEntity().getPersistentData().getInt("SenderCount");
        for (int i = 0; i < size; i++) {
            senderPoses.add(TCUtil.loadBlockPos(getBlockEntity().getPersistentData().getCompound("SenderPos_"+i)));
        }
    }

    @Override
    public void onTagUpdate(CompoundTag compoundTag) {
        super.onTagUpdate(compoundTag);
        int size = getBlockEntity().getPersistentData().getInt("SenderCount");
        for (int i = 0; i < size; i++) {
            senderPoses.add(TCUtil.loadBlockPos(getBlockEntity().getPersistentData().getCompound("SenderPos_"+i)));
        }
    }
}
