package net.sinedkadis.terracompositio.block.behaviours.pp;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.sinedkadis.terracompositio.block.entity.PathPointerBlockEntity;
import net.sinedkadis.terracompositio.util.TCUtil;

public class CollectorBehaviour extends PPInputBehaviour{

    public CollectorBehaviour(PathPointerBlockEntity blockEntity) {
        super(blockEntity);
    }

    @Override
    public void onUpdate() {
        if (invalidBehaviours()) return;
        updateMaxCFE();
    }

    @Override
    public boolean isActive() {
        return blockEntity.parts.contains(PathPointerBlockEntity.PPPart.COLLECTOR);
    }

    public static BlockPos getEmitter(BlockEntity collector) {
        CompoundTag persistentData = collector.getPersistentData();
        if (!persistentData.contains("emitter_pos")) return null;
        return TCUtil.loadBlockPos(persistentData.getCompound("emitter_pos"));
    }

    public static void setEmitter(BlockEntity blockEntity, BlockPos emitterPos) {
        blockEntity.getPersistentData().put("emitter_pos",TCUtil.saveBlockPos(emitterPos));
    }
}
