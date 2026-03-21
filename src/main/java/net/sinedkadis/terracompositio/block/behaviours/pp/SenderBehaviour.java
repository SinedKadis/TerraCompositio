package net.sinedkadis.terracompositio.block.behaviours.pp;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.sinedkadis.terracompositio.block.entity.PathPointerBlockEntity;
import net.sinedkadis.terracompositio.compat.jade.JadeTerraCompositioPlugin;
import net.sinedkadis.terracompositio.util.TCUtil;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class SenderBehaviour extends AbstractPPBehaviour {

    public SenderBehaviour(PathPointerBlockEntity blockEntity) {
        super(blockEntity);
    }

    @Override
    public boolean isActive() {
        return blockEntity.parts.contains(PathPointerBlockEntity.PPPart.SENDER);
    }

    @Override
    public void onAppendServerData(CompoundTag compoundTag) {
        super.onAppendServerData(compoundTag);
        BlockPos receiverPos = blockEntity.getReceiverPos();
        if (receiverPos != null)
            compoundTag.put("bindPos", TCUtil.saveBlockPos(receiverPos));
    }

    @Override
    public void onAppendTooltip(ITooltip iTooltip, CompoundTag serverData, IPluginConfig iPluginConfig) {
        super.onAppendTooltip(iTooltip, serverData, iPluginConfig);
        if (serverData.contains("bindPos") && iPluginConfig.get(JadeTerraCompositioPlugin.debugConfigRL())) {
            BlockPos pos = TCUtil.loadBlockPos(serverData.getCompound("bindPos"));
            iTooltip.add(Component.literal("ReceiverPos: " + pos.getX() + " " + pos.getY() + " " + pos.getZ()));
        }
    }
}
