package net.sinedkadis.terracompositio.block.behaviours;

import lombok.Data;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.sinedkadis.terracompositio.api.IHaveKnowledge;
import net.sinedkadis.terracompositio.api.TCCapabilities;
import net.sinedkadis.terracompositio.api.TerraCompositioAPI;
import net.sinedkadis.terracompositio.api.behaviors.blockentity.IBECFEBehaviour;
import net.sinedkadis.terracompositio.api.networks.NetworkAction;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetwork;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetworkMember;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetworkMemberEntity;
import net.sinedkadis.terracompositio.api.networks.cfe.ICFEHandler;
import net.sinedkadis.terracompositio.block.entity.TCBlockEntity;
import net.sinedkadis.terracompositio.cfe.CFEContainer;
import net.sinedkadis.terracompositio.cfe.CFEMemberProxy;
import net.sinedkadis.terracompositio.config.TCCommonConfigs;
import net.sinedkadis.terracompositio.config.TCInnerConfig;
import net.sinedkadis.terracompositio.util.helpers.CFEHelper;
import net.sinedkadis.terracompositio.util.helpers.TooltipHelper;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashSet;
import java.util.List;
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
                CFEHelper.createTransfer().fromMembers(target, this).build();
            });
        }
    }

    @Override
    public void onCFENetworkMemberUpdate(CFENetworkMember updated) {
        if (getMainHandler().getCFE() > 0 && CFEHelper.validMember(updated)) {
            if (updated.getMainHandler().getFreeSpace() > TCCommonConfigs.CFE_PER_BURST_TRANSFER_LIMIT.get()) {
                if (updated instanceof CFEMemberProxy proxy && proxy.target() instanceof CFENetworkMemberEntity) {
                    if (updated.getPos().closerThan(proxy.proxy().getOutputPos(),getRange()))
                        scheduleMemberUpdate(updated);
                } else scheduleMemberUpdate(updated);
            }
            CFEHelper.createTransfer().fromMembers(updated, this).build();
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
    public void collectKnowledgeData(CompoundTag data) {

        data.putInt("val.cfe", cfeHandler.getCFE());

        if (TCCommonConfigs.DEBUG.get()) {
            data.putInt("val.max_cfe", cfeHandler.getMaxCFE());
            data.putInt("val.queued", cfeHandler.getQueued());
            data.putInt("val.priority", this.getPriority());
        }

        data.putInt("val.range", this.getRange());

        if (priority == TCInnerConfig.DEFAULT_CONSUMER_PRIORITY) {
            data.putBoolean("flag.type.consumer", true);
        } else if (priority == TCInnerConfig.DEFAULT_SOURCE_PRIORITY) {
            data.putBoolean("flag.type.source", true);
        }

    }

    @Override
    public void addTooltipLines(CompoundTag data, List<Component> tooltip, boolean isShifting) {

        tooltip.add(Component.translatable("block.terracompositio.block_header"));

        if (data.contains("val.priority")) {
            tooltip.add(
                    Component.translatable("block.terracompositio.priority",
                                    Component.literal(String.valueOf(data.getInt("val.priority")))
                                            .append(Component.translatable("block.terracompositio.units"))
                                            .withStyle(ChatFormatting.AQUA))
                            .withStyle(ChatFormatting.GRAY));

        }
        if (data.contains("val.range")) {
            if (isShifting)
                tooltip.add(
                        Component.translatable("block.terracompositio.range",
                                        Component.literal(String.valueOf(data.getInt("val.range")))
                                                .append(Component.translatable("block.terracompositio.blocks"))
                                                .withStyle(ChatFormatting.AQUA))
                                .withStyle(ChatFormatting.GRAY));

        }

        tooltip.add(Component.translatable("block.terracompositio.cfe_header"));

        if (data.contains("val.cfe")) {
            tooltip.add(TooltipHelper.defaultTextWithArg("block.terracompositio.cfe", data.getInt("val.cfe")));
        }
        if (data.contains("val.max_cfe")) {
            tooltip.add(
                    Component.translatable("block.terracompositio.max_cfe",
                                    Component.literal(String.valueOf(data.getInt("val.max_cfe")))
                                            .append(Component.translatable("block.terracompositio.units"))
                                            .withStyle(ChatFormatting.AQUA))
                            .withStyle(ChatFormatting.GRAY));

        }
        if (data.contains("val.queued")) {
            tooltip.add(
                    Component.translatable("block.terracompositio.queued",
                                    Component.literal(String.valueOf(data.getInt("val.queued")))
                                            .append(Component.translatable("block.terracompositio.units"))
                                            .withStyle(ChatFormatting.AQUA))
                            .withStyle(ChatFormatting.GRAY));

        }
        if (data.contains("flag.type.consumer") && data.getBoolean("flag.type.consumer")) {
            tooltip.add(
                    Component.translatable("block.terracompositio.type",
                                    Component.translatable("block.terracompositio.consumer")
                                            .withStyle(ChatFormatting.AQUA))
                            .withStyle(ChatFormatting.GRAY));
        }
        if (data.contains("flag.type.source") && data.getBoolean("flag.type.source")) {
            tooltip.add(
                    Component.translatable("block.terracompositio.type",
                                    Component.translatable("block.terracompositio.source")
                                            .withStyle(ChatFormatting.AQUA))
                            .withStyle(ChatFormatting.GRAY));
        }
    }

}
