package net.sinedkadis.terracompositio.block.behaviours.pp;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.sinedkadis.terracompositio.block.entity.PathPointerBlockEntity;
import net.sinedkadis.terracompositio.compat.jade.JadeTerraCompositioPlugin;
import net.sinedkadis.terracompositio.util.TCUtil;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import java.util.ArrayList;
import java.util.List;

public class SenderBehaviour extends AbstractPPBehaviour {

    public SenderBehaviour(PathPointerBlockEntity blockEntity) {
        super(blockEntity);
    }

    @Override
    public void onUpdate() {
        scheduleBindPosPPNetworkUpdate();

    }

    private void scheduleBindPosPPNetworkUpdate() {
        BlockPos bindPos = getBindPos(blockEntity);
        if (bindPos == null) return;
        Level level = getBlockEntity().getLevel();
        if (level == null) return;
        BlockEntity target = level.getBlockEntity(bindPos);
        if (target instanceof PathPointerBlockEntity ppBE)
            ppBE.setPpUpdateScheduled(true);
    }



    @Override
    public void onRemoved() {
        Level level = blockEntity.getLevel();
        BlockPos bindPos = getBindPos(blockEntity);
        if (level != null && bindPos != null) {
            BlockEntity receiverEntity = level.getBlockEntity(bindPos);
            if (receiverEntity instanceof PathPointerBlockEntity pathPointerBlockEntity) {
                List<BlockPos> senderPoses = new ArrayList<>(ReceiverBehaviour.getSenderPoses(pathPointerBlockEntity));
                senderPoses.remove(blockEntity.getBlockPos());
                ReceiverBehaviour.setSenderPoses(pathPointerBlockEntity,
                        senderPoses);
            }
        }
        super.onRemoved();
    }

    @Override
    public boolean isActive() {
        return blockEntity.parts.contains(PathPointerBlockEntity.PPPart.SENDER);
    }

    @Override
    public void onAppendServerData(CompoundTag compoundTag) {
        super.onAppendServerData(compoundTag);
        compoundTag.put("bindPos", TCUtil.saveBlockPos(getBindPos(blockEntity)));
    }

    @Override
    public void onAppendTooltip(ITooltip iTooltip, CompoundTag serverData, IPluginConfig iPluginConfig) {
        super.onAppendTooltip(iTooltip, serverData, iPluginConfig);
        if (serverData.contains("bindPos") && iPluginConfig.get(JadeTerraCompositioPlugin.debugConfigRL())) {
            BlockPos pos = TCUtil.loadBlockPos(serverData.getCompound("bindPos"));
            iTooltip.add(Component.literal("BindPos: " + pos.getX() + " " + pos.getY() + " " + pos.getZ()));
        }
    }

    public static BlockPos getBindPos(BlockEntity blockEntity) {
        CompoundTag persistentData = blockEntity.getPersistentData();
        if (!persistentData.contains("bindPos")) return null;
        return TCUtil.loadBlockPos(persistentData.getCompound("bindPos"));
    }

    public static void setBindPos(BlockEntity blockEntity,BlockPos bindPos) {
        blockEntity.getPersistentData().put("bindPos",TCUtil.saveBlockPos(bindPos));
    }
}
