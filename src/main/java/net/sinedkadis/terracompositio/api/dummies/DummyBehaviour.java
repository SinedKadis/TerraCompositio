package net.sinedkadis.terracompositio.api.dummies;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.sinedkadis.terracompositio.api.behaviors.blockentity.IBEBehaviour;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DummyBehaviour implements IBEBehaviour {
    public static final DummyBehaviour instance = new DummyBehaviour();

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
    public void onSave(CompoundTag compoundTag) {

    }

    @Override
    public void onLoad(CompoundTag compoundTag) {

    }

    @Override
    public <T extends BlockEntity> T getBlockEntity() {
        return null;
    }
}
