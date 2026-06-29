package net.sinedkadis.terracompositio.block.behaviours;

import lombok.Data;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.sinedkadis.terracompositio.api.IHaveKnowledge;
import net.sinedkadis.terracompositio.api.TerraCompositioAPI;
import net.sinedkadis.terracompositio.api.helpers.ECFHelper;
import net.sinedkadis.terracompositio.api.helpers.TooltipHelper;
import net.sinedkadis.terracompositio.api.networks.NetworkAction;
import net.sinedkadis.terracompositio.api.networks.ecf.ECFNetwork;
import net.sinedkadis.terracompositio.api.networks.ecf.ECFNetworkMember;
import net.sinedkadis.terracompositio.api.networks.ecf.IECFHandler;
import net.sinedkadis.terracompositio.api.registries.TCCapabilities;
import net.sinedkadis.terracompositio.block.entity.TCBlockEntity;
import net.sinedkadis.terracompositio.config.TCCommonConfigs;
import net.sinedkadis.terracompositio.config.TCInnerConfig;
import net.sinedkadis.terracompositio.ecf.PPECFMemberProxy;
import net.sinedkadis.terracompositio.util.IEntityInstance;
import net.sinedkadis.terracompositio.util.behaviors.blockentity.IBEECFBehaviour;
import net.sinedkadis.terracompositio.util.helpers.ECFHelperInternal;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

@Data
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ECFHandlerBehaviour implements IBEECFBehaviour, IHaveKnowledge {
    private final TCBlockEntity blockEntity;

    protected int range;
    protected int priority;
    protected IECFHandler ecfHandler;
    protected LazyOptional<IECFHandler> lazyCFEOptional = LazyOptional.empty();

    protected boolean scheduledUpdate = false;
    protected int scheduledMembersUpdate = -1;
    protected Set<ECFNetworkMember> scheduledMembers = new HashSet<>();

    public ECFHandlerBehaviour(TCBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
        ecfHandler = TerraCompositioAPI.instance().getECFNetworkInstance().createDefaultECFHandler(IEntityInstance.wrap(blockEntity));
        this.range = 5;
    }

    public ECFHandlerBehaviour maxECF(int maxCFE) {
        this.ecfHandler.setMaxECF(maxCFE);
        return this;
    }
    public ECFHandlerBehaviour range(int range) {
        this.range = range;
        return this;
    }
    public ECFHandlerBehaviour priority(int priority) {
        this.priority = priority;
        return this;
    }

    public ECFHandlerBehaviour ecfHandler(Function<ECFHandlerBehaviour, IECFHandler> ecfHandler) {
        this.ecfHandler = ecfHandler.apply(this);
        return this;
    }

    @Override
    public void tick() {
        ECFNetwork ECFNetworkInstance = TerraCompositioAPI.INSTANCE.getECFNetworkInstance();
        Level pLevel = blockEntity.getLevel();
        if (pLevel == null) return;
        if (!pLevel.isClientSide && range != 0) {
            boolean inNetwork = ECFNetworkInstance.isIn(pLevel, this);
            if (!inNetwork && !blockEntity.isRemoved()) {
                ECFNetworkInstance.fireECFNetworkEvent(this, NetworkAction.ADD);
            }
        }
        updateIfScheduled();
    }

    @Override
    public void onChunkLoad() {
        lazyCFEOptional = LazyOptional.of(() -> ecfHandler);
        scheduleMemberUpdate();
    }

    @Override
    public void onECFNetworkMemberUpdate() {
        if (getMainHandler().getECF() > 0) {
            ECFNetwork ECFNetwork = TerraCompositioAPI.instance().getECFNetworkInstance();
            Set<ECFNetworkMember> targets = ECFNetwork.getAvailableNetworkTargets(this);
            targets.forEach(target -> {
                if (target.getMainHandler().getFreeSpace() > TCCommonConfigs.ECF_PER_BURST_TRANSFER_LIMIT.get())
                    scheduleMemberUpdate(target);
                ECFHelper.newTransfer().targetAndSource(target, this).build();
            });
        }
    }

    @Override
    public void onECFNetworkMemberUpdate(ECFNetworkMember updated) {
        if (getMainHandler().getECF() > 0 && isValidMember(updated)) {
            if (updated.getMainHandler().getFreeSpace() > TCCommonConfigs.ECF_PER_BURST_TRANSFER_LIMIT.get()) {
                if (updated instanceof PPECFMemberProxy proxy && ((IEntityInstance) proxy.target()).tc$isEntity()) {
                    if (updated.getEntityInstance().tc$getBlockPos().closerThan(proxy.proxy().getOutputPos(), getRange()))
                        scheduleMemberUpdate(updated);
                } else scheduleMemberUpdate(updated);
            }
            ECFHelper.newTransfer().targetAndSource(updated, this).build();
        } else onECFNetworkMemberUpdate();
    }

    public boolean isValidMember(ECFNetworkMember updated) {
        return ECFHelper.validMember(updated) || ECFHelperInternal.validPPProxy(updated);
    }

    @Override
    public @Nullable LazyOptional<?> getCapability(Capability<?> cap, @Nullable Direction side) {
        if (cap == TCCapabilities.ECF){
            return lazyCFEOptional.cast();
        }
        return null;
    }

    @Override
    public void onRemoved() {
        TerraCompositioAPI.INSTANCE.getECFNetworkInstance().fireECFNetworkEvent(this, NetworkAction.REMOVE);
    }

    @Override
    public void onInvalidateCaps() {
        lazyCFEOptional.invalidate();
    }

    @Override
    public void onSave(CompoundTag tag) {
        ecfHandler.writeToNBT(tag);
    }

    @Override
    public void onLoad(CompoundTag tag) {
        ecfHandler.readFromNBT(tag);
    }


    @Override
    public IECFHandler getMainHandler() {
        return ecfHandler;
    }

    @Override
    public void updateIfScheduled() {
        if (scheduledUpdate) {
            this.scheduledUpdate = false;
            this.onECFNetworkMemberUpdate();
        }
        if (scheduledMembersUpdate == 0) {
            scheduledMembersUpdate = -1;
            Set<ECFNetworkMember> scheduledMembers1 = Set.copyOf(this.scheduledMembers);
            this.scheduledMembers.clear();
            scheduledMembers1.forEach(this::onECFNetworkMemberUpdate);
        } else if (scheduledMembersUpdate > 0)
            scheduledMembersUpdate--;

    }

    @Override
    public void scheduleMemberUpdate() {
        this.scheduledUpdate = true;
    }

    @Override
    public void scheduleMemberUpdate(ECFNetworkMember updated) {
        this.scheduledMembers.add(updated);
        if (scheduledMembersUpdate < 0) scheduledMembersUpdate = TCCommonConfigs.TICKS_BETWEEN_BURSTS.get();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o.getClass().isInstance(this))) return false;
        ECFHandlerBehaviour that = (ECFHandlerBehaviour) o;
        return Objects.equals(blockEntity, that.blockEntity);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(blockEntity);
    }

    @Override
    public void collectKnowledgeData(CompoundTag data) {

        data.putInt(TooltipHelper.Keys.ECF.toData(), ecfHandler.getECF());

        if (TCCommonConfigs.DEBUG.get()) {
            data.putInt(TooltipHelper.Keys.MAX_ECF.toData(), ecfHandler.getMaxECF());
            data.putInt(TooltipHelper.Keys.QUEUED.toData(), ecfHandler.getQueued());
        }
        data.putInt(TooltipHelper.Keys.PRIORITY.toData(), this.getPriority());
        data.putInt(TooltipHelper.Keys.RANGE.toData(), this.getRange());
    }

    @Override
    public void addTooltipLines(CompoundTag data, List<Component> tooltip, boolean isShifting) {

        TooltipHelper.addWithHeader(TooltipHelper.Headers.BLOCK, tooltip, t -> {
            if (TCCommonConfigs.DEBUG.get()) {
                TooltipHelper.addIfExist(TooltipHelper.Keys.PRIORITY, t, data);
            }
            if (isShifting)
                TooltipHelper.addIfExist(TooltipHelper.Keys.RANGE, TooltipHelper.Units.BLOCKS, t, data);
            if (data.contains(TooltipHelper.Keys.PRIORITY.toData())) {
                int priority = data.getInt(TooltipHelper.Keys.PRIORITY.toData());
                if (priority == TCInnerConfig.DEFAULT_CONSUMER_PRIORITY)
                    TooltipHelper.addWithNoArg(TooltipHelper.Keys.TYPE, TooltipHelper.Units.CONSUMER, t);
                if (priority == TCInnerConfig.DEFAULT_SOURCE_PRIORITY)
                    TooltipHelper.addWithNoArg(TooltipHelper.Keys.TYPE, TooltipHelper.Units.SOURCE, t);
            }
        });


        TooltipHelper.addWithHeader(TooltipHelper.Headers.ECF, tooltip, t -> {
            TooltipHelper.addIfExist(TooltipHelper.Keys.ECF, t, data);
            TooltipHelper.addIfExist(TooltipHelper.Keys.MAX_ECF, t, data);
            TooltipHelper.addIfExist(TooltipHelper.Keys.QUEUED, t, data);
        });




    }

    @Override
    public IEntityInstance getEntityInstance() {
        return IEntityInstance.wrap(blockEntity);
    }
}
