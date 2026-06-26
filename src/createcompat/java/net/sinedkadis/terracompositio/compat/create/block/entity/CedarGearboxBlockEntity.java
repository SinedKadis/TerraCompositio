package net.sinedkadis.terracompositio.compat.create.block.entity;

import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
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
import net.sinedkadis.terracompositio.api.registries.TCCapabilities;
import net.sinedkadis.terracompositio.api.TerraCompositioAPI;
import net.sinedkadis.terracompositio.api.helpers.TooltipHelper;
import net.sinedkadis.terracompositio.api.networks.NetworkAction;
import net.sinedkadis.terracompositio.api.networks.ecf.ECFNetwork;
import net.sinedkadis.terracompositio.api.networks.ecf.ECFNetworkMemberBE;
import net.sinedkadis.terracompositio.api.networks.ecf.IECFHandler;
import net.sinedkadis.terracompositio.compat.create.TCCreateCompat;
import net.sinedkadis.terracompositio.config.TCCommonConfigs;
import net.sinedkadis.terracompositio.config.TCInnerConfig;
import net.sinedkadis.terracompositio.ecf.DefaultECFHandler;
import net.sinedkadis.terracompositio.api.registries.TCBlockStateProperties;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class CedarGearboxBlockEntity extends GeneratingKineticBlockEntity implements ECFNetworkMemberBE, IHaveKnowledge {

    protected int range;
    protected int priority;
    protected boolean scheduledUpdate = false;
    protected IECFHandler ecfHandler = new DefaultECFHandler(this) {
        @Override
        protected void sendCFEUpdate() {
            super.sendCFEUpdate();
            updateGeneratedRotation();
        }
    };
    protected LazyOptional<IECFHandler> lazyCFEOptional = LazyOptional.empty();

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
        if (ecfHandler.getECF() <= 0) return 0;
        return getBlockState().getValue(TCBlockStateProperties.INFUSED) ? 16 : 8;
    }



    @Override
    public void lazyTick() {
        super.lazyTick();
        ECFNetwork ECFNetworkInstance = TerraCompositioAPI.INSTANCE.getECFNetworkInstance();
        Level pLevel = this.level;
        if (pLevel == null) return;
        if (!pLevel.isClientSide && range != 0) {
            boolean inNetwork = ECFNetworkInstance.isIn(pLevel, this);
            if (!inNetwork && !isRemoved()) {
                ECFNetworkInstance.fireECFNetworkEvent(this, NetworkAction.ADD);
            }
        }
        if (!isOverStressed() && ecfHandler.takeECF(1, false) > 0) {
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
        lazyCFEOptional = LazyOptional.of(() -> ecfHandler);
        scheduleMemberUpdate();
        updateGeneratedRotation();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap) {
        if (cap == TCCapabilities.ECF){
            return lazyCFEOptional.cast();
        }
        return super.getCapability(cap);
    }

    @Override
    public void remove() {
        super.remove();
        TerraCompositioAPI.INSTANCE.getECFNetworkInstance().fireECFNetworkEvent(this, NetworkAction.REMOVE);
    }

    @Override
    public void invalidate() {
        super.invalidate();
        lazyCFEOptional.invalidate();
    }

    @Override
    protected void write(CompoundTag compound, boolean clientPacket) {
        super.write(compound, clientPacket);
        ecfHandler.writeToNBT(compound);
    }

    @Override
    protected void read(CompoundTag compound, boolean clientPacket) {
        super.read(compound, clientPacket);
        ecfHandler.readFromNBT(compound);
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
    public IECFHandler getMainHandler() {
        return ecfHandler;
    }

    @Override
    public void updateIfScheduled() {
        if (scheduledUpdate) {
            this.scheduledUpdate = false;
            this.onECFNetworkMemberUpdate();
        }
    }

    @Override
    public void scheduleMemberUpdate() {
        this.scheduledUpdate = true;
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

        TooltipHelper.addHeader(TooltipHelper.Headers.BLOCK, tooltip);

        boolean added = false;
        if (TCCommonConfigs.DEBUG.get()) {
            added |= TooltipHelper.addIfExist(TooltipHelper.Keys.PRIORITY, tooltip, data);
        }
        if (isShifting)
            added |= TooltipHelper.addIfExist(TooltipHelper.Keys.RANGE, TooltipHelper.Units.BLOCKS, tooltip, data);

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
