package net.sinedkadis.terracompositio.block.behaviours.pp;

import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.sinedkadis.terracompositio.api.behaviors.blockentity.IBEBehaviour;
import net.sinedkadis.terracompositio.block.entity.PathPointerBlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class AbstractPPBehaviour implements IBEBehaviour {

    @Getter
    protected PathPointerBlockEntity blockEntity;

    public AbstractPPBehaviour(PathPointerBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    @Override
    public void tick() {

    }

    @Override
    public void onChunkLoad() {

    }

    @Override
    public @Nullable LazyOptional<?> getCapability(@NotNull Capability<?> cap, @Nullable Direction side) {
        return null;
    }

    @Override
    public void onRemoved() {

    }

    @Override
    public void onInvalidateCaps() {

    }

    @Override
    public void onLoad(CompoundTag compoundTag) {

    }

    @Override
    public void onSave(CompoundTag compoundTag) {

    }

    protected BlockPos getEndpoint() {
        Level level = blockEntity.getLevel();
        BlockPos blockPos = blockEntity.getBlockPos();

        if (level == null) return blockPos;

        Queue<BlockPos> queue = new LinkedList<>();
        queue.add(blockPos);


        while (true) {
            BlockPos currentPos = queue.poll();
            BlockEntity currentBE = level.getBlockEntity(currentPos);
            if (currentBE instanceof PathPointerBlockEntity currentPPBE) {
                var bindPos = SenderBehaviour.getBindPos(currentPPBE);
                if (bindPos != null)
                    queue.add(bindPos);
            }
            if (queue.isEmpty()) {
                return currentPos;
            }
        }
    }


}
