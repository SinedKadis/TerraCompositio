package net.sinedkadis.terracompositio.block.behaviours.pp;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.sinedkadis.terracompositio.api.TCCapabilities;
import net.sinedkadis.terracompositio.block.entity.PathPointerBlockEntity;
import net.sinedkadis.terracompositio.compat.jade.JadeTerraCompositioPlugin;
import net.sinedkadis.terracompositio.util.TCUtil;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import java.util.List;
import java.util.Objects;

public class ReceiverBehaviour extends AbstractPPBehaviour {

    private static final String sendersTag = "senders";

    public ReceiverBehaviour(PathPointerBlockEntity blockEntity) {
        super(blockEntity);
    }

    @Override
    public void onUpdate() {
        pullCFE();
    }

    @Override
    public boolean isActive() {
        return blockEntity.parts.contains(PathPointerBlockEntity.PPPart.RECEIVER);
    }

    protected void pullCFE() {
        Level level = blockEntity.getLevel();
        if (level == null) return;

        getSenderPoses(blockEntity).stream()
                .map(level::getBlockEntity)
                .filter(Objects::nonNull)
                .forEach(blockEntity1 -> {
                    if (blockEntity1 instanceof PathPointerBlockEntity pathPointerBlockEntity) {
                        tryPullFrom(pathPointerBlockEntity);
                    }
                });

    }

    private void tryPullFrom(PathPointerBlockEntity senderBE) {
        senderBE.getCapability(TCCapabilities.CFE).ifPresent(senderHandler -> {
            if (senderHandler.getCFE() > 0) {
                blockEntity.getCapability(TCCapabilities.CFE).ifPresent(thisHandler ->
                                TCUtil.tryCFETransfer(thisHandler,senderHandler));
            }
        });
    }

    public static boolean validAngle(PathPointerBlockEntity be, Vec3 burstDir) {

        float yaw = be.rotationYaw;
        float pitch = be.rotationPitch;

        // куда смотрит блок
        Vec3 lookDir = new Vec3(0, 0, 1)
                .yRot(yaw)
                .xRot(pitch)
                .normalize();

        double dot = burstDir.dot(lookDir);
        return dot > 0;
    }

    @Override
    public void onRemoved() {
        Level level = blockEntity.getLevel();
        if (level != null) {
            getSenderPoses(blockEntity).forEach(blockPos -> {
                BlockEntity senderEntity = level.getBlockEntity(blockPos);
                if (senderEntity instanceof PathPointerBlockEntity pathPointerBlockEntity) {
                    SenderBehaviour.setBindPos(pathPointerBlockEntity,null);
                }
            });
        }
        super.onRemoved();
    }

    @Override
    public void onAppendServerData(CompoundTag compoundTag) {
        super.onAppendServerData(compoundTag);
        CompoundTag persistentData = blockEntity.getPersistentData();
        if (persistentData.contains(sendersTag)){
            ListTag senders = persistentData.getList(sendersTag, Tag.TAG_COMPOUND);
            compoundTag.put(sendersTag,senders);
        }
    }

    @Override
    public void onAppendTooltip(ITooltip iTooltip, CompoundTag serverData, IPluginConfig iPluginConfig) {
        super.onAppendTooltip(iTooltip, serverData, iPluginConfig);
        if (iPluginConfig.get(JadeTerraCompositioPlugin.debugConfigRL())) {
            ListTag senders = serverData.getList(sendersTag, Tag.TAG_COMPOUND);
            for (int i = 0; i < senders.size(); i++) {
                CompoundTag tag = ((CompoundTag) senders.get(i));
                BlockPos pos = TCUtil.loadBlockPos(tag);
                iTooltip.add(Component.literal("SenderPos " + i + ": " + pos.getX() + " " + pos.getY() + " " + pos.getZ()));
            }

        }
    }

    public static List<BlockPos> getSenderPoses(BlockEntity blockEntity) {
        CompoundTag persistentData = blockEntity.getPersistentData();
        ListTag list = persistentData.getList(sendersTag, Tag.TAG_COMPOUND);
        return list.stream()
                .map(tag -> TCUtil.loadBlockPos(((CompoundTag) tag)))
                .toList();
    }

    public static void setSenderPoses(BlockEntity blockEntity, List<BlockPos> list) {
        CompoundTag persistentData = blockEntity.getPersistentData();
        ListTag listTag = new ListTag();
        listTag.addAll(list.stream().map(TCUtil::saveBlockPos).toList());
        persistentData.put(sendersTag, listTag);
    }
}
