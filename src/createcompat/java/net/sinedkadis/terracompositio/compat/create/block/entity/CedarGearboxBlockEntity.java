package net.sinedkadis.terracompositio.compat.create.block.entity;

import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.api.IHaveKnowledge;
import net.sinedkadis.terracompositio.api.TCCapabilities;
import net.sinedkadis.terracompositio.api.TerraCompositioAPI;
import net.sinedkadis.terracompositio.api.networks.NetworkAction;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetwork;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetworkMemberBE;
import net.sinedkadis.terracompositio.api.networks.cfe.ICFEHandler;
import net.sinedkadis.terracompositio.cfe.CFEContainer;
import net.sinedkadis.terracompositio.compat.create.TCCreateCompat;
import net.sinedkadis.terracompositio.compat.jade.JadeTerraCompositioPlugin;
import net.sinedkadis.terracompositio.config.TCCommonConfigs;
import net.sinedkadis.terracompositio.config.TCInnerConfig;
import net.sinedkadis.terracompositio.registries.TCBlockStateProperties;
import org.jetbrains.annotations.NotNull;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import java.util.List;
import java.util.Objects;

public class CedarGearboxBlockEntity extends GeneratingKineticBlockEntity implements CFENetworkMemberBE, IHaveKnowledge {

    protected int range;
    protected int priority;
    protected boolean scheduledUpdate = false;
    protected ICFEHandler cfeHandler = new CFEContainer(this){
        @Override
        protected void sendCFEUpdate() {
            super.sendCFEUpdate();
            updateGeneratedRotation();
        }
    };
    protected LazyOptional<ICFEHandler> lazyCFEOptional = LazyOptional.empty();

    public CedarGearboxBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(Objects.requireNonNull(((TCCreateCompat) TerraCompositio.createCompat).blockEntities.CEDAR_GEARBOX_BE).get(),pPos, pBlockState);
        setLazyTickRate(60);
        this.range = 5;
        this.priority = TCInnerConfig.DEFAULT_CONSUMER_PRIORITY;
        this.capacity = 256f;
    }

    @Override
    public float getGeneratedSpeed() {
        if (isOverStressed()) return 0;
        if (cfeHandler.getCFE() <= 0) return 0;
        return getBlockState().getValue(TCBlockStateProperties.INFUSED) ? 16 : 8;
    }



    @Override
    public void lazyTick() {
        super.lazyTick();
        CFENetwork cfeNetworkInstance = TerraCompositioAPI.INSTANCE.getCFENetworkInstance();
        Level pLevel = this.level;
        if (pLevel == null) return;
        if (!pLevel.isClientSide && range != 0) {
            boolean inNetwork = cfeNetworkInstance.isIn(pLevel, this);
            if (!inNetwork && !isRemoved()) {
                cfeNetworkInstance.fireCFENetworkEvent(this, NetworkAction.ADD);
            }
        }
        if (!isOverStressed() && cfeHandler.takeCFE(1, false) > 0) {
            updateGeneratedRotation();
        } else {
            if (getSpeed() != 0)
                updateGeneratedRotation();
        }
        updateIfScheduled();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyCFEOptional = LazyOptional.of(() -> cfeHandler);
        scheduleMemberUpdate();
        updateGeneratedRotation();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap) {
        if (cap == TCCapabilities.CFE){
            return lazyCFEOptional.cast();
        }
        return super.getCapability(cap);
    }

    @Override
    public void remove() {
        super.remove();
        TerraCompositioAPI.INSTANCE.getCFENetworkInstance().fireCFENetworkEvent(this, NetworkAction.REMOVE);
    }

    @Override
    public void invalidate() {
        super.invalidate();
        lazyCFEOptional.invalidate();
    }

    @Override
    protected void write(CompoundTag compound, boolean clientPacket) {
        super.write(compound, clientPacket);
        cfeHandler.writeToNBT(compound);
    }

    @Override
    protected void read(CompoundTag compound, boolean clientPacket) {
        super.read(compound, clientPacket);
        cfeHandler.readFromNBT(compound);
    }

    public void onAppendServerData(CompoundTag compoundTag) {
        compoundTag.putInt("cfe",getMainHandler().getCFE());
        compoundTag.putInt("priority",getPriority());
        compoundTag.putInt("limit", getRange());

        compoundTag.putInt("max_cfe",getMainHandler().getMaxCFE());
        compoundTag.putInt("queued",getMainHandler().getQueued());
    }

    public void onAppendTooltip(ITooltip iTooltip, CompoundTag serverData, IPluginConfig iPluginConfig) {
        if (serverData.contains("cfe")) {
            iTooltip.add(Component.translatable("block.terracompositio." + "cfe", serverData.getInt("cfe")));
        }
        if (serverData.contains("max_cfe")  && iPluginConfig.get(JadeTerraCompositioPlugin.debugConfigRL())) {
            iTooltip.add(Component.translatable("block.terracompositio." + "max_cfe", serverData.getInt("max_cfe")));
        }
        if (serverData.contains("queued")  && iPluginConfig.get(JadeTerraCompositioPlugin.debugConfigRL())) {
            iTooltip.add(Component.translatable("block.terracompositio." + "queued", serverData.getInt("queued")));
        }
        if (serverData.contains("priority")  && iPluginConfig.get(JadeTerraCompositioPlugin.debugConfigRL())) {
            iTooltip.add(Component.translatable("block.terracompositio." + "priority", serverData.getInt("priority")));
        }
        if (serverData.contains("limit")  && iPluginConfig.get(JadeTerraCompositioPlugin.debugConfigRL())) {
            iTooltip.add(Component.translatable("block.terracompositio." + "limit", serverData.getInt("limit")));
        }
        if (serverData.contains("speed")  && iPluginConfig.get(JadeTerraCompositioPlugin.debugConfigRL())) {
            iTooltip.add(Component.translatable("block.terracompositio." + "speed", serverData.getFloat("speed")));
        }
    }




    @Override
    public int getRange() {
        return range;
    }

    @Override
    public int getPriority() {
        return priority;
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
    }

    @Override
    public void scheduleMemberUpdate() {
        this.scheduledUpdate = true;
    }

    @Override
    public void addToKnowledgeTooltip(List<Component> tooltip, boolean isShifting) {
        tooltip.add(Component.translatable("block.terracompositio." + "cfe_header"));

        tooltip.add(Component.translatable("block.terracompositio." + "cfe",
                Component.literal(cfeHandler.getCFE() + "").append(Component.translatable("block.terracompositio.units"))
                        .withStyle(ChatFormatting.AQUA)).withStyle(ChatFormatting.GRAY));
        if (isShifting)
            tooltip.add(Component.translatable("block.terracompositio." + "consume",
                    Component.literal(1 + "").append(Component.translatable("block.terracompositio.cfe_second"))
                            .withStyle(ChatFormatting.AQUA)).withStyle(ChatFormatting.GRAY));
        if (TCCommonConfigs.DEBUG.get()) {
            tooltip.add(Component.translatable("block.terracompositio." + "max_cfe",
                    Component.literal(cfeHandler.getMaxCFE() + "").withStyle(ChatFormatting.AQUA)).withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("block.terracompositio." + "queued",
                    Component.literal(cfeHandler.getQueued() + "").withStyle(ChatFormatting.AQUA)).withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("block.terracompositio." + "priority",
                    Component.literal(this.getPriority() + "").withStyle(ChatFormatting.AQUA)).withStyle(ChatFormatting.GRAY));
        }
        if (isShifting)
            tooltip.add(Component.translatable("block.terracompositio." + "range",
                    Component.literal(this.getRange() + "").append(Component.translatable("block.terracompositio.blocks"))
                            .withStyle(ChatFormatting.AQUA)).withStyle(ChatFormatting.GRAY));
        if (priority == TCInnerConfig.DEFAULT_CONSUMER_PRIORITY)
            tooltip.add(Component.translatable("block.terracompositio." + "type",
                    Component.translatable("block.terracompositio.consumer").withStyle(ChatFormatting.AQUA)).withStyle(ChatFormatting.GRAY));
        if (priority == TCInnerConfig.DEFAULT_SOURCE_PRIORITY)
            tooltip.add(Component.translatable("block.terracompositio." + "type",
                    Component.translatable("block.terracompositio.source").withStyle(ChatFormatting.AQUA)).withStyle(ChatFormatting.GRAY));
    }
}
