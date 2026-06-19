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
import net.sinedkadis.terracompositio.api.behaviors.blockentity.IBECFEBehaviour;
import net.sinedkadis.terracompositio.api.helpers.TooltipHelper;
import net.sinedkadis.terracompositio.api.networks.NetworkAction;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetwork;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetworkMember;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetworkMemberEntity;
import net.sinedkadis.terracompositio.api.networks.cfe.ICFEHandler;
import net.sinedkadis.terracompositio.block.entity.TCBlockEntity;
import net.sinedkadis.terracompositio.cfe.CFEContainer;
import net.sinedkadis.terracompositio.cfe.PPCFEMemberProxy;
import net.sinedkadis.terracompositio.config.TCCommonConfigs;
import net.sinedkadis.terracompositio.config.TCInnerConfig;
import net.sinedkadis.terracompositio.util.helpers.CFEHelper;
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
public class CFEHandlerBehaviour implements IBECFEBehaviour, IHaveKnowledge {
    private final TCBlockEntity blockEntity;

    protected int range;
    protected int priority;
    protected ICFEHandler cfeHandler = new CFEContainer(this);
    protected LazyOptional<ICFEHandler> lazyCFEOptional = LazyOptional.empty();

    protected boolean scheduledUpdate = false;
    protected int scheduledMembersUpdate = -1;
    protected Set<CFENetworkMember> scheduledMembers = new HashSet<>();

    public CFEHandlerBehaviour(TCBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
        this.range = 5;
    }
    public CFEHandlerBehaviour maxCFE(int maxCFE) {
        this.cfeHandler.setMaxCFE(maxCFE);
        return this;
    }
    public CFEHandlerBehaviour range(int range) {
        this.range = range;
        return this;
    }
    public CFEHandlerBehaviour priority(int priority) {
        this.priority = priority;
        return this;
    }
    public CFEHandlerBehaviour cfeHandler(Function<CFEHandlerBehaviour,ICFEHandler> cfeHandler) {
        this.cfeHandler = cfeHandler.apply(this);
        return this;
    }


    @Override
    public BlockEntity getEntity() {
        return blockEntity;
    }

    @Override
    public void tick() {
        CFENetwork cfeNetworkInstance = TerraCompositioAPI.INSTANCE.getCFENetworkInstance();
        Level pLevel = blockEntity.getLevel();
        if (pLevel == null) return;
        if (!pLevel.isClientSide && range != 0) {
            boolean inNetwork = cfeNetworkInstance.isIn(pLevel, this);
            if (!inNetwork && !blockEntity.isRemoved()) {
                cfeNetworkInstance.fireCFENetworkEvent(this, NetworkAction.ADD);
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
            CFENetwork cfeNetwork = TerraCompositioAPI.instance().getCFENetworkInstance();
            Set<CFENetworkMember> targets = cfeNetwork.getAvailableNetworkTargets(this);
            targets.forEach(target -> {
                if (target.getMainHandler().getFreeSpace() > TCCommonConfigs.CFE_PER_BURST_TRANSFER_LIMIT.get())
                    scheduleMemberUpdate(target);
                CFEHelper.CFETransferBuilder cfeTransferBuilder = CFEHelper.newTransfer().targetAndSource(target, this);
                if (target.getEntity() instanceof Player) {
                    cfeTransferBuilder.instant();
                }
                cfeTransferBuilder.build();
            });
        }
    }

    @Override
    public void onCFENetworkMemberUpdate(CFENetworkMember updated) {
        if (getMainHandler().getCFE() > 0 && CFEHelper.validMember(updated)) {
            if (updated.getMainHandler().getFreeSpace() > TCCommonConfigs.CFE_PER_BURST_TRANSFER_LIMIT.get()) {
                if (updated instanceof PPCFEMemberProxy proxy && proxy.target() instanceof CFENetworkMemberEntity) {
                    if (updated.getPos().closerThan(proxy.proxy().getOutputPos(),getRange()))
                        scheduleMemberUpdate(updated);
                } else scheduleMemberUpdate(updated);
            }
            CFEHelper.CFETransferBuilder cfeTransferBuilder = CFEHelper.newTransfer().targetAndSource(updated, this);
            if (updated.getEntity() instanceof Player) {
                cfeTransferBuilder.instant();
            }
            cfeTransferBuilder.build();
        } else onCFENetworkMemberUpdate();
    }

    @Override
    public @Nullable LazyOptional<?> getCapability(Capability<?> cap, @Nullable Direction side) {
        if (cap == TCCapabilities.CFE){
            return lazyCFEOptional.cast();
        }
        return null;
    }

    @Override
    public void onRemoved() {
        TerraCompositioAPI.INSTANCE.getCFENetworkInstance().fireCFENetworkEvent(this, NetworkAction.REMOVE);
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
    public void onAppendServerData(CompoundTag compoundTag) {
        compoundTag.putInt("cfe",getMainHandler().getCFE());
        compoundTag.putInt("priority",getPriority());
        compoundTag.putInt("limit", getRange());

        compoundTag.putInt("max_cfe",getMainHandler().getMaxCFE());
        compoundTag.putInt("queued",getMainHandler().getQueued());
    }

    @Override
    public void onAppendTooltip(List<Component> iTooltip, CompoundTag serverData) {
        if (serverData.contains("cfe")) {
            iTooltip.add(Component.translatable("block.terracompositio." + "cfe", serverData.getInt("cfe")));
        }
        if (serverData.contains("max_cfe") && TCCommonConfigs.DEBUG.get()) {
            iTooltip.add(Component.translatable("block.terracompositio." + "max_cfe", serverData.getInt("max_cfe")));
        }
        if (serverData.contains("queued") && TCCommonConfigs.DEBUG.get()) {
            iTooltip.add(Component.translatable("block.terracompositio." + "queued", serverData.getInt("queued")));
        }
        if (serverData.contains("priority") && TCCommonConfigs.DEBUG.get()) {
            iTooltip.add(Component.translatable("block.terracompositio." + "priority", serverData.getInt("priority")));
        }
        if (serverData.contains("limit") && TCCommonConfigs.DEBUG.get()) {
            iTooltip.add(Component.translatable("block.terracompositio." + "limit", serverData.getInt("limit")));
        }
        if (serverData.contains("speed") && TCCommonConfigs.DEBUG.get()) {
            iTooltip.add(Component.translatable("block.terracompositio." + "speed", serverData.getFloat("speed")));
        }
    }




    @Override
    public ICFEHandler getMainHandler() {
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
            Set<CFENetworkMember> scheduledMembers1 = Set.copyOf(this.scheduledMembers);
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
    public void scheduleMemberUpdate(CFENetworkMember updated) {
        this.scheduledMembers.add(updated);
        if (scheduledMembersUpdate < 0) scheduledMembersUpdate = TCCommonConfigs.TICKS_BETWEEN_BURSTS.get();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o.getClass().isInstance(this))) return false;
        CFEHandlerBehaviour that = (CFEHandlerBehaviour) o;
        return Objects.equals(blockEntity, that.blockEntity);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(blockEntity);
    }

    @Override
    public void collectKnowledgeData(CompoundTag data) {

        data.putInt(TooltipHelper.Keys.CFE.toData(), cfeHandler.getCFE());

        if (TCCommonConfigs.DEBUG.get()) {
            data.putInt(TooltipHelper.Keys.MAX_CFE.toData(), cfeHandler.getMaxCFE());
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

        TooltipHelper.addHeader(TooltipHelper.Headers.CFE, tooltip);

        TooltipHelper.addIfExist(TooltipHelper.Keys.CFE, tooltip, data);
        TooltipHelper.addIfExist(TooltipHelper.Keys.MAX_CFE, tooltip, data);
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
