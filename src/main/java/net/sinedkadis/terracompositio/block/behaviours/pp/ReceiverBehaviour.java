package net.sinedkadis.terracompositio.block.behaviours.pp;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import net.sinedkadis.terracompositio.block.entity.PathPointerBlockEntity;
import net.sinedkadis.terracompositio.compat.jade.JadeTerraCompositioPlugin;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import java.util.HashSet;
import java.util.Set;

public class ReceiverBehaviour extends AbstractPPBehaviour {

    public ReceiverBehaviour(PathPointerBlockEntity blockEntity) {
        super(blockEntity);
    }

    @Override
    public boolean isActive() {
        return blockEntity.parts.contains(PathPointerBlockEntity.PPPart.RECEIVER);
    }


    public static boolean validAngle(PathPointerBlockEntity be, Vec3 burstDir) {

        float yaw = be.rotationYaw;
        float pitch = be.rotationPitch;

        // куда смотрит блок
        Vec3 lookDir = new Vec3(0, 0, 1)
                .yRot(yaw)
                .xRot(pitch)
                .normalize();
        //Todo: fix
        double dot = burstDir.dot(lookDir);
        //return dot > 0;
        return true;
    }

    @Override
    public void onAppendServerData(CompoundTag compoundTag) {
        super.onAppendServerData(compoundTag);
        PathPointerBlockEntity.saveFromSetToTag(compoundTag,PathPointerBlockEntity.SENDER_POSES_TAG,blockEntity.getSenderPoses());
    }

    @Override
    public void onAppendTooltip(ITooltip iTooltip, CompoundTag serverData, IPluginConfig iPluginConfig) {
        super.onAppendTooltip(iTooltip, serverData, iPluginConfig);
        if (iPluginConfig.get(JadeTerraCompositioPlugin.debugConfigRL())) {
            Set<BlockPos> senderPoses = new HashSet<>();
            PathPointerBlockEntity.loadFromTagToSet(serverData,PathPointerBlockEntity.SENDER_POSES_TAG, senderPoses);
            int i = 1;
            for (BlockPos pos : senderPoses) {
                iTooltip.add(Component.literal("SenderPos " + i + ": " + pos.getX() + " " + pos.getY() + " " + pos.getZ()));
                i = i + 1;
            }


        }
    }
}
