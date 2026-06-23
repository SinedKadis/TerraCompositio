package net.sinedkadis.terracompositio.block.behaviours;

import lombok.Data;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.sinedkadis.terracompositio.api.IHaveKnowledge;
import net.sinedkadis.terracompositio.api.TCCapabilities;
import net.sinedkadis.terracompositio.api.TerraCompositioAPI;
import net.sinedkadis.terracompositio.api.behaviors.blockentity.IBEECFBehaviour;
import net.sinedkadis.terracompositio.api.helpers.TooltipHelper;
import net.sinedkadis.terracompositio.api.networks.NetworkAction;
import net.sinedkadis.terracompositio.api.networks.cfe.ECFNetwork;
import net.sinedkadis.terracompositio.api.networks.cfe.ECFNetworkMember;
import net.sinedkadis.terracompositio.api.networks.cfe.ECFNetworkMemberEntity;
import net.sinedkadis.terracompositio.api.networks.cfe.IECFHandler;
import net.sinedkadis.terracompositio.block.entity.TCBlockEntity;
import net.sinedkadis.terracompositio.ecf.ECFContainer;
import net.sinedkadis.terracompositio.ecf.PPECFMemberProxy;
import net.sinedkadis.terracompositio.config.TCCommonConfigs;
import net.sinedkadis.terracompositio.config.TCInnerConfig;
import net.sinedkadis.terracompositio.util.helpers.ECFHelper;
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
    protected IECFHandler cfeHandler = new ECFContainer(this);
    protected LazyOptional<IECFHandler> lazyCFEOptional = LazyOptional.empty();

    protected boolean scheduledUpdate = false;
    protected int scheduledMembersUpdate = -1;
    protected Set<ECFNetworkMember> scheduledMembers = new HashSet<>();

    public ECFHandlerBehaviour(TCBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
        this.range = 5;
    }
    public ECFHandlerBehaviour maxCFE(int maxCFE) {
        this.cfeHandler.setMaxCFE(maxCFE);
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
    public ECFHandlerBehaviour cfeHandler(Function<ECFHandlerBehaviour, IECFHandler> cfeHandler) {
        this.cfeHandler = cfeHandler.apply(this);
        return this;
    }


    @Override
    public BlockEntity getEntity() {
        return blockEntity;
    }

    @Override
    public void tick() {
        ECFNetwork ECFNetworkInstance = TerraCompositioAPI.INSTANCE.getECFNetworkInstance();
        Level pLevel = blockEntity.getLevel();
        if (pLevel == null) return;
        if (!pLevel.isClientSide && range != 0) {
            boolean inNetwork = ECFNetworkInstance.isIn(pLevel, this);
            if (!inNetwork && !blockEntity.isRemoved()) {
                ECFNetworkInstance.fireCFENetworkEvent(this, NetworkAction.ADD);
            }
        }
        updateIfScheduled();
    }

    @Override
    public void onChunkLoad() {
        lazyCFEOptional = LazyOptional.of(() -> cfeHandler);
        scheduleMemberUpdate();
    }

    @Override
    public void onCFENetworkMemberUpdate() {
        if (getMainHandler().getCFE() > 0){
            ECFNetwork ECFNetwork = TerraCompositioAPI.instance().getECFNetworkInstance();
            Set<ECFNetworkMember> targets = ECFNetwork.getAvailableNetworkTargets(this);
            targets.forEach(target -> {
                if (target.getMainHandler().getFreeSpace() > TCCommonConfigs.CFE_PER_BURST_TRANSFER_LIMIT.get())
                    scheduleMemberUpdate(target);
                ECFHelper.CFETransferBuilder cfeTransferBuilder = ECFHelper.newTransfer().targetAndSource(target, this);
                if (target.getEntity() instanceof Player) {
                    cfeTransferBuilder.instant();
                }
                cfeTransferBuilder.build();
            });
        }
    }

    @Override
    public void onCFENetworkMemberUpdate(ECFNetworkMember updated) {
        if (getMainHandler().getCFE() > 0 && ECFHelper.validMember(updated)) {
            if (updated.getMainHandler().getFreeSpace() > TCCommonConfigs.CFE_PER_BURST_TRANSFER_LIMIT.get()) {
                if (updated instanceof PPECFMemberProxy proxy && proxy.target() instanceof ECFNetworkMemberEntity) {
                    if (updated.getPos().closerThan(proxy.proxy().getOutputPos(),getRange()))
                        scheduleMemberUpdate(updated);
                } else scheduleMemberUpdate(updated);
            }
            ECFHelper.CFETransferBuilder cfeTransferBuilder = ECFHelper.newTransfer().targetAndSource(updated, this);
            if (updated.getEntity() instanceof Player) {
                cfeTransferBuilder.instant();
            }
            cfeTransferBuilder.build();
        } else onCFENetworkMemberUpdate();
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
        TerraCompositioAPI.INSTANCE.getECFNetworkInstance().fireCFENetworkEvent(this, NetworkAction.REMOVE);
    }

    @Override
    public void onInvalidateCaps() {
        lazyCFEOptional.invalidate();
    }

    @Override
    public void onSave(CompoundTag tag) {
        cfeHandler.writeToNBT(tag);
    }

    @Override
    public void onLoad(CompoundTag tag) {
        cfeHandler.readFromNBT(tag);
    }


    @Override
    public IECFHandler getMainHandler() {
        return cfeHandler;
    }

    @Override
    public void updateIfScheduled() {
        if (scheduledUpdate) {
            this.scheduledUpdate = false;
            this.onCFENetworkMemberUpdate();
        }
        if (scheduledMembersUpdate == 0) {
            scheduledMembersUpdate = -1;
            Set<ECFNetworkMember> scheduledMembers1 = Set.copyOf(this.scheduledMembers);
            this.scheduledMembers.clear();
            scheduledMembers1.forEach(this::onCFENetworkMemberUpdate);
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

        data.putInt(TooltipHelper.Keys.ECF.toData(), cfeHandler.getCFE());

        if (TCCommonConfigs.DEBUG.get()) {
            data.putInt(TooltipHelper.Keys.MAX_ECF.toData(), cfeHandler.getMaxCFE());
            data.putInt(TooltipHelper.Keys.QUEUED.toData(), cfeHandler.getQueued());
        }
        data.putInt(TooltipHelper.Keys.PRIORITY.toData(), this.getPriority());
        data.putInt(TooltipHelper.Keys.RANGE.toData(), this.getRange());
    }

    @Override
    public void addTooltipLines(CompoundTag data, List<Component> tooltip, boolean isShifting) {

        TooltipHelper.addHeader(TooltipHelper.Headers.BLOCK, tooltip);

        boolean added = false;
        if (TCCommonConfigs.DEBUG.get()) {
            added = TooltipHelper.addIfExist(TooltipHelper.Keys.PRIORITY, tooltip, data);
        }
        if (isShifting)
            added = TooltipHelper.addIfExist(TooltipHelper.Keys.RANGE, TooltipHelper.Units.BLOCKS, tooltip, data);

        if (!added) tooltip.remove(tooltip.size() - 1);

        TooltipHelper.addHeader(TooltipHelper.Headers.ECF, tooltip);

        TooltipHelper.addIfExist(TooltipHelper.Keys.ECF, tooltip, data);
        TooltipHelper.addIfExist(TooltipHelper.Keys.MAX_ECF, tooltip, data);
        TooltipHelper.addIfExist(TooltipHelper.Keys.QUEUED, tooltip, data);

        if (data.contains(TooltipHelper.Keys.PRIORITY.toData())) {
            int priority = data.getInt(TooltipHelper.Keys.PRIORITY.toData());
            if (priority == TCInnerConfig.DEFAULT_CONSUMER_PRIORITY)
                TooltipHelper.add(TooltipHelper.Keys.TYPE, TooltipHelper.Units.CONSUMER, tooltip);
            if (priority == TCInnerConfig.DEFAULT_SOURCE_PRIORITY)
                TooltipHelper.add(TooltipHelper.Keys.TYPE, TooltipHelper.Units.SOURCE, tooltip);
        }
    }

}
