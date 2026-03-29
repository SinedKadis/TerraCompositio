package net.sinedkadis.terracompositio.block.entity;

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
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;


@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class TCBlockEntity extends BlockEntity{
    @Getter
    protected List<IBEBehaviour> behaviours = new ArrayList<>();

    public TCBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        addBEBehaviours(behaviours);
    }

    abstract void addBEBehaviours(List<IBEBehaviour> behaviourList);

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
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
        behaviours.forEach(IBEBehaviour::onRemoved);
        super.setRemoved();
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

    public void onAppendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig){
        CompoundTag serverData = blockAccessor.getServerData();
        getBehaviours().forEach(ibeBehaviour -> ibeBehaviour.onAppendTooltip(iTooltip,serverData,iPluginConfig));
    }

    public void onAppendServerData(CompoundTag compoundTag){
        getBehaviours().forEach(ibeBehaviour -> ibeBehaviour.onAppendServerData(compoundTag));
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }


}