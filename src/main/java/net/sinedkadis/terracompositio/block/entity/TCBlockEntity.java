package net.sinedkadis.terracompositio.block.entity;

import lombok.Getter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
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
import net.sinedkadis.terracompositio.api.IHaveKnowledge;
import net.sinedkadis.terracompositio.api.behaviors.blockentity.IBEBehaviour;
import net.sinedkadis.terracompositio.api.behaviors.blockentity.IBEECFBehaviour;
import net.sinedkadis.terracompositio.api.behaviors.blockentity.IBEItemBehaviour;
import net.sinedkadis.terracompositio.block.custom.TCBaseEntityBlock;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;


@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class TCBlockEntity extends BlockEntity implements IHaveKnowledge {
    @Getter
    protected List<IBEBehaviour> behaviours = new ArrayList<>();

    public TCBlockEntity(BlockPos pos, BlockState state) {
        super(((TCBaseEntityBlock) state.getBlock()).getBlockEntityType(), pos, state);
        addBEBehaviours(behaviours);
    }
    public TCBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        addBEBehaviours(behaviours);
    }

    abstract void addBEBehaviours(List<IBEBehaviour> behaviourList);

    public Set<IBEItemBehaviour> getItemBehaviours() {
        Set<IBEItemBehaviour> toReturn = new HashSet<>();
        for (IBEBehaviour ibeBehaviour : behaviours) {
            if (ibeBehaviour instanceof IBEItemBehaviour ibeItemBehaviour) toReturn.add(ibeItemBehaviour);
        }
        return toReturn;
    }

    public @Nullable IBEECFBehaviour getCFEBehaviour() {
        for (IBEBehaviour ibeBehaviour : behaviours) {
            if (ibeBehaviour instanceof IBEECFBehaviour IBEECFBehaviour) return IBEECFBehaviour;
        }
        return null;
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        Optional<? extends LazyOptional<?>> behaviourCap = behaviours.stream()
                .map(iBehaviour -> iBehaviour.getCapability(cap, side))
                .filter(Objects::nonNull)
                .findAny();
        return behaviourCap.<LazyOptional<T>>map(LazyOptional::cast).orElseGet(() -> super.getCapability(cap, side));
        //return super.getCapability(cap, side);
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

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public void addTooltipLines(CompoundTag data, List<Component> tooltip, boolean isShifting) {
        for (IBEBehaviour behaviour : getBehaviours()) {
            if (behaviour instanceof IHaveKnowledge iHaveKnowledge) {
                iHaveKnowledge.addTooltipLines(data, tooltip, isShifting);
            }
        }
    }

    @Override
    public void collectKnowledgeData(CompoundTag data) {
        for (IBEBehaviour behaviour : getBehaviours()) {
            if (behaviour instanceof IHaveKnowledge iHaveKnowledge) {
                iHaveKnowledge.collectKnowledgeData(data);
            }
        }
    }
}