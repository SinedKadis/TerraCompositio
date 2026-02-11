package net.sinedkadis.terracompositio.block.behaviours.pp;

import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
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
                    pathPointerBlockEntity.getCapability(BehaviourCapabilities.SENDER).ifPresent(senderBehaviour ->
                            senderBehaviour.setBindPos(null));
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
    public @Nullable LazyOptional<?> getCapability(@NotNull Capability<?> cap, @Nullable Direction side) {
        if (cap == BehaviourCapabilities.RECEIVER)
            return LazyOptional.of(() -> this);
        return super.getCapability(cap,side);
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
    public void onAppendServerData(CompoundTag compoundTag) {
        super.onAppendServerData(compoundTag);
        for (int i = 0; i < senderPoses.size(); i++) {
            BlockPos bindPos = senderPoses.get(i);
            compoundTag.put("senderPos_"+i, TCUtil.saveBlockPos(bindPos));
        }
    }

    @Override
    public void onAppendTooltip(ITooltip iTooltip, CompoundTag serverData, IPluginConfig iPluginConfig) {
        super.onAppendTooltip(iTooltip, serverData, iPluginConfig);
        if (iPluginConfig.get(JadeTerraCompositioPlugin.debugConfigRL())){
            for (int i = 0;;i++) {
                if (!serverData.contains("senderPos_"+i)) break;
                BlockPos pos = TCUtil.loadBlockPos(serverData.getCompound("senderPos_"+i));
                iTooltip.add(Component.literal("SenderPos "+ i +": " + pos.getX() + " " + pos.getY() + " " + pos.getZ()));
            }
        }
    }
}
