package net.sinedkadis.terracompositio.block.behaviours.pp;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.sinedkadis.terracompositio.block.entity.PathPointerBlockEntity;
import net.sinedkadis.terracompositio.compat.jade.JadeTerraCompositioPlugin;
import net.sinedkadis.terracompositio.util.BehaviourCapabilities;
import net.sinedkadis.terracompositio.util.TCUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

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
                pathPointerBlockEntity.getCapability(BehaviourCapabilities.RECEIVER).ifPresent(receiverBehaviour ->
                        receiverBehaviour.getSenderPoses().remove(blockEntity.getBlockPos()));
            }
        }
        super.onRemoved();
    }

    @Override
    public void onSave(CompoundTag compoundTag) {
        super.onSave(compoundTag);
        getBlockEntity().getPersistentData().put("bindpos", TCUtil.saveBlockPos(bindPos));
    }

    @Override
    public void onLoad(CompoundTag compoundTag) {
        super.onLoad(compoundTag);
        bindPos = TCUtil.loadBlockPos(getBlockEntity().getPersistentData().getCompound("bindpos"));
    }

    @Override
    public @Nullable LazyOptional<?> getCapability(@NotNull Capability<?> cap, @Nullable Direction side) {
        if (cap == BehaviourCapabilities.SENDER)
            return LazyOptional.of(() -> this);
        return super.getCapability(cap,side);
    }

    @Override
    public void onAppendServerData(CompoundTag compoundTag) {
        super.onAppendServerData(compoundTag);
        compoundTag.put("bindPos",TCUtil.saveBlockPos(bindPos));
    }

    @Override
    public void onAppendTooltip(ITooltip iTooltip, CompoundTag serverData, IPluginConfig iPluginConfig) {
        super.onAppendTooltip(iTooltip, serverData, iPluginConfig);
        if (serverData.contains("bindPos")  && iPluginConfig.get(JadeTerraCompositioPlugin.debugConfigRL())) {
            BlockPos pos = TCUtil.loadBlockPos(serverData.getCompound("bindPos"));
            iTooltip.add(Component.literal("BindPos: " + pos.getX() + " " + pos.getY() + " " + pos.getZ()));
        }
    }
}
