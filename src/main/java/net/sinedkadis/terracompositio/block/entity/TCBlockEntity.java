package net.sinedkadis.terracompositio.block.entity;

import javax.annotation.ParametersAreNonnullByDefault;

import lombok.Getter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.sinedkadis.terracompositio.api.behaviors.blockentity.IBEBehaviour;
import net.sinedkadis.terracompositio.api.behaviors.blockentity.IBECFEBehaviour;
import net.sinedkadis.terracompositio.api.behaviors.blockentity.IBEItemBehaviour;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class TCBlockEntity extends BlockEntity {
    @Getter
    List<IBEBehaviour> behaviours = new ArrayList<>();
    public TCBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        addBehaviours(behaviours);
        behaviours.forEach(IBEBehaviour::init);
    }

    abstract void addBehaviours(List<IBEBehaviour> list);

    public Optional<IBECFEBehaviour> getCfeBehaviour() {
        return behaviours.stream().map(iBehaviour -> iBehaviour instanceof IBECFEBehaviour IBECFEBehaviour ? IBECFEBehaviour : null)
                .filter(Objects::nonNull)
                .findAny();
    }
    public Optional<IBEItemBehaviour> getItemBehaviour() {
        return behaviours.stream().map(iBehaviour -> iBehaviour instanceof IBEItemBehaviour iitemBehaviour ? iitemBehaviour : null)
                .filter(Objects::nonNull)
                .findAny();
    }


    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        Optional<? extends LazyOptional<?>> behaviourCap = behaviours.stream()
                .map(iBehaviour -> iBehaviour.getCapability(cap, side))
                .filter(Objects::nonNull)
                .findAny();
        return behaviourCap.<LazyOptional<T>>map(LazyOptional::cast).orElseGet(() -> super.getCapability(cap, side));
    }

    public void tick(Level pLevel, BlockPos pPos, BlockState pState) {
        if (level instanceof ServerLevel)
            behaviours.forEach(IBEBehaviour::tick);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        behaviours.forEach(IBEBehaviour::onChunkLoad);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        behaviours.forEach(IBEBehaviour::onRemoved);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        behaviours.forEach(IBEBehaviour::onInvalidateCaps);
    }

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        super.saveAdditional(pTag);
        behaviours.forEach(iBehaviour -> iBehaviour.onSave(pTag));
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        behaviours.forEach(iBehaviour -> iBehaviour.onLoad(pTag));
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }
}