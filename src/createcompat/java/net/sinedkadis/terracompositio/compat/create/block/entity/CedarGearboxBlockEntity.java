package net.sinedkadis.terracompositio.compat.create.block.entity;

import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.DistExecutor;
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
import net.sinedkadis.terracompositio.config.TCCommonConfigs;
import net.sinedkadis.terracompositio.config.TCInnerConfig;
import net.sinedkadis.terracompositio.registries.TCBlockStateProperties;
import org.jetbrains.annotations.NotNull;

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
        setLazyTickRate(20);
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
        if (level.isClientSide) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> this::tickAudio);
            return;
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

        // Заголовок — всегда первым, не зависит от данных
        tooltip.add(Component.translatable("block.terracompositio.cfe_header"));

        if (data.contains("val.cfe")) {
            tooltip.add(
                    Component.translatable("block.terracompositio.cfe",
                                    Component.literal(String.valueOf(data.getInt("val.cfe")))
                                            .append(Component.translatable("block.terracompositio.units"))
                                            .withStyle(ChatFormatting.AQUA))
                            .withStyle(ChatFormatting.GRAY));
            if (isShifting)
                tooltip.add(Component.translatable("block.terracompositio." + "consume",
                        Component.literal(1 + "").append(Component.translatable("block.terracompositio.cfe_second"))
                                .withStyle(ChatFormatting.AQUA)).withStyle(ChatFormatting.GRAY));
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
        if (data.contains("val.priority")) {
            tooltip.add(
                    Component.translatable("block.terracompositio.priority",
                                    Component.literal(String.valueOf(data.getInt("val.priority")))
                                            .append(Component.translatable("block.terracompositio.units"))
                                            .withStyle(ChatFormatting.AQUA))
                            .withStyle(ChatFormatting.GRAY));

        }
        if (data.contains("val.range")) {
            tooltip.add(
                    Component.translatable("block.terracompositio.range",
                                    Component.literal(String.valueOf(data.getInt("val.range")))
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
                                    Component.translatable("block.terracompositio.consumer")
                                            .withStyle(ChatFormatting.AQUA))
                            .withStyle(ChatFormatting.GRAY));
        }
    }
}
