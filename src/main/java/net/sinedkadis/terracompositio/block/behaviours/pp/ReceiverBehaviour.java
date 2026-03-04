package net.sinedkadis.terracompositio.block.behaviours.pp;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.sinedkadis.terracompositio.block.entity.PathPointerBlockEntity;
import net.sinedkadis.terracompositio.compat.jade.JadeTerraCompositioPlugin;
import net.sinedkadis.terracompositio.util.TCUtil;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import java.util.ArrayList;
import java.util.List;

public class ReceiverBehaviour extends AbstractPPBehaviour {

    private static final String sendersTag = "senders";

    public ReceiverBehaviour(PathPointerBlockEntity blockEntity) {
        super(blockEntity);
    }

    public static boolean validAngle(PathPointerBlockEntity blockEntity,BlockPos senderPos) {
        float rotationYaw = blockEntity.rotationYaw;
        float rotationPitch = blockEntity.rotationPitch;
        BlockPos currentPos = blockEntity.getBlockPos();

        Vec3 senderVec = currentPos.subtract(senderPos).getCenter().normalize();
        Vec3 currentVec = new Vec3(0, 0, -1).normalize().yRot(rotationYaw).xRot(rotationPitch);

        double angle = Math.acos(senderVec.dot(currentVec) / senderVec.length() / currentVec.length());

        return angle < Math.PI / 2;
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

        return new ArrayList<>(){

            @Override
            public BlockPos get(int index) {
                ListTag list = persistentData.getList(sendersTag, Tag.TAG_COMPOUND);
                return TCUtil.loadBlockPos((CompoundTag) list.get(index));
            }

            @Override
            public void clear() {
                persistentData.put(sendersTag, new CompoundTag());
            }

            @Override
            public boolean add(BlockPos blockPos) {
                ListTag list = persistentData.getList(sendersTag, Tag.TAG_COMPOUND);


                CompoundTag posTag = TCUtil.saveBlockPos(blockPos);
                if (list.contains(posTag)) return false;
                list.add(posTag);

                persistentData.put(sendersTag, list);
                return true;
            }



            @Override
            public BlockPos remove(int index) {
                ListTag list = persistentData.getList(sendersTag, Tag.TAG_COMPOUND);
                Tag remove = list.remove(index);
                persistentData.put(sendersTag,list);
                return TCUtil.loadBlockPos(((CompoundTag) remove));
            }

            @Override
            public boolean remove(Object o) {
                ListTag list = persistentData.getList(sendersTag, Tag.TAG_COMPOUND);
                boolean remove = list.remove(TCUtil.saveBlockPos(((BlockPos) o)));
                persistentData.put(sendersTag,list);
                return remove;
            }
        };
    }
}
