package net.sinedkadis.terracompositio.entity.custom;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.sinedkadis.terracompositio.api.IHaveKnowledge;
import net.sinedkadis.terracompositio.api.TCCapabilities;
import net.sinedkadis.terracompositio.api.TerraCompositioAPI;
import net.sinedkadis.terracompositio.api.dummies.DummyECFHandler;
import net.sinedkadis.terracompositio.api.helpers.TooltipHelper;
import net.sinedkadis.terracompositio.api.networks.NetworkAction;
import net.sinedkadis.terracompositio.api.networks.ecf.ECFNetwork;
import net.sinedkadis.terracompositio.api.networks.ecf.ECFNetworkMember;
import net.sinedkadis.terracompositio.api.networks.ecf.ECFNetworkMemberEntity;
import net.sinedkadis.terracompositio.api.networks.ecf.IECFHandler;
import net.sinedkadis.terracompositio.block.entity.AirSaturatorBlockEntity;
import net.sinedkadis.terracompositio.config.TCCommonConfigs;
import net.sinedkadis.terracompositio.config.TCInnerConfig;
import net.sinedkadis.terracompositio.ecf.LimitlessECFContainer;
import net.sinedkadis.terracompositio.ecf.burst.ECFBurstProjectileEntity;
import net.sinedkadis.terracompositio.registries.TCEntities;
import net.sinedkadis.terracompositio.util.helpers.ECFHelper;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.ToIntFunction;

// /kill @e[type=terracompositio:cfe_cloud_entity]

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ECFCloudEntity extends Entity implements ECFNetworkMemberEntity, IHaveKnowledge {
    private static final EntityDataAccessor<Integer> ECF_DATA =
            SynchedEntityData.defineId(ECFCloudEntity.class, EntityDataSerializers.INT);
    private int queuedECF;
    boolean scheduleUpdate = false;
    protected Set<ECFNetworkMember> scheduledMembers = new HashSet<>();
    protected int scheduledMembersUpdate = -1;


    protected LazyOptional<IECFHandler> lazyECFOptional = LazyOptional.of(() -> new LimitlessECFContainer(this) {
        @Override
        public int getECF() {
            return getSyncedECF();
        }

        @Override
        public void setECF(int ECF) {
            setSyncedECF(ECF);
        }

        @Override
        public int sendECF(ECFNetworkMember target, int ecf, float speed, boolean simulate) {
            if (target instanceof DummyECFHandler) return 0;

            if (target.getEntity() instanceof AirSaturatorBlockEntity) return 0;

            IECFHandler mainHandler = target.getMainHandler();
            int freeSpace = mainHandler.getFreeSpace();
            int added = Mth.clamp(ecf, 0, freeSpace);
            if (added < 1)
                return 0;

            if (!simulate) {
                Vec3 burstOffset = getBurstOffset(mainHandler);
                BlockPos offset = BlockPos.containing(this.getPos().getCenter().add(burstOffset));
                if (offset.closerThan(target.getPos(), 2)) {
                    mainHandler.addECF(added, false);
                    return added;
                }
                ECFBurstProjectileEntity entity = ECFBurstProjectileEntity.sendBurst(this, burstOffset, target, added, speed);
                if (entity != null) {
                    level().addFreshEntity(entity);
                    mainHandler.addToQueue(added);
                }
            }
            return added;
        }

        @Override
        public int addECF(int ecf, boolean simulate) {
            int pMax = this.getMaxECF() - this.getECF();
            int added = Mth.clamp(ecf, 0, pMax);
            if (!simulate) {
                queuedECF += added;
                //subFromQueue(added);
                sendCFEUpdate();
                onContentsChanged();
            }
            return added;
        }
    });

    @Override
    public void onECFNetworkMemberUpdate() {
        if (getPriority() < 0 && getMainHandler().getECF() > 0) {
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
        if (updated.getEntity() instanceof AirSaturatorBlockEntity) return;
        if (getPriority() < 0 && getMainHandler().getECF() > 0 && ECFHelper.validMember(updated)) {
            if (updated.getMainHandler().getFreeSpace() > TCCommonConfigs.ECF_PER_BURST_TRANSFER_LIMIT.get())
                scheduleMemberUpdate(updated);
            ECFHelper.newTransfer().targetAndSource(updated, this).build();
        }
    }

    private Vec3 getBurstOffset(IECFHandler target) {
        double r = getRadius();
        BlockPos sourcePos = this.getPos();
        BlockPos targetPos = target.getPos();
        if (sourcePos.closerThan(targetPos,r)) return targetPos.subtract(sourcePos).getCenter();
        return sourcePos.getCenter().vectorTo(targetPos.getCenter()).normalize().scale(r);
    }

    public static final ToIntFunction<Integer> RENDER_COUNT_FUNCTION = cfe ->
            (int) Math.ceil(cfe * 0.1f);
    private double getRadius() {
        int cfe = RENDER_COUNT_FUNCTION.applyAsInt(getSyncedECF());
        float k = (float) Math.log10(cfe);
        double baseRadius = 0.2 + 0.3 * Math.log1p(cfe * 0.1);
        return baseRadius * k;
    }

    public ECFCloudEntity(EntityType<? extends Entity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        setNoGravity(true);
        setInvulnerable(true);
    }

    public ECFCloudEntity(Level pLevel) {
        this(TCEntities.ECF_CLOUD.get(),pLevel);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing) {
        if (capability == TCCapabilities.ECF)
            return lazyECFOptional.cast();
        return super.getCapability(capability, facing);
    }

    @Override
    public void tick() {
        super.tick();
        ECFNetwork ECFNetworkInstance = TerraCompositioAPI.instance().getECFNetworkInstance();
        boolean inNetwork = ECFNetworkInstance.isIn(this.level(), this);
        if (!inNetwork && !this.isRemoved()) {
            ECFNetworkInstance.fireECFNetworkEvent(this, NetworkAction.ADD);
        }
        updateIfScheduled();
        if (getSyncedECF() <= 0) discard();

        if (queuedECF > 0) {
            int toAdd = (int) Math.ceil(queuedECF * 0.1f);
            if (toAdd < 1) toAdd = queuedECF;
            queuedECF -= toAdd;

            setSyncedECF(getSyncedECF() + toAdd);
        }

        if (tickCount % 20 == 1) {
            setSyncedECF(getSyncedECF() - 1);
        }
    }

    public AABB getBoundingBox() {
        double radius = getRadius()*2;
        return AABB.ofSize(position(), radius,radius,radius);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(ECF_DATA, 0);
    }

    public int getSyncedECF() {
        return this.entityData.get(ECF_DATA);
    }

    public void setSyncedECF(int amount) {
        if (!this.level().isClientSide()) {
            this.entityData.set(ECF_DATA, amount);
        }
    }

    @Override
    public void remove(RemovalReason pReason) {
        super.remove(pReason);
        TerraCompositioAPI.INSTANCE.getECFNetworkInstance().fireECFNetworkEvent(this, NetworkAction.REMOVE);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyECFOptional.invalidate();
    }

    @Override
    public Level getLevel() {
        return this.level();
    }

    @Override
    public BlockPos getPos() {
        return this.blockPosition();
    }

    @Override
    public void updateIfScheduled() {
        if (scheduleUpdate) {
            this.scheduleUpdate = false;
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
        scheduleUpdate = true;
    }
    @Override
    public void scheduleMemberUpdate(ECFNetworkMember updated) {
        this.scheduledMembers.add(updated);
        if (scheduledMembersUpdate < 0) scheduledMembersUpdate = 5;
    }

    @Override
    public int getRange() {
        return (int) (getRadius() + 5);
    }

    @Override
    public int getPriority() {
        return TCInnerConfig.DEFAULT_SOURCE_PRIORITY;
    }


    @Override
    public IECFHandler getMainHandler() {
        return lazyECFOptional.orElse(DummyECFHandler.instance);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        lazyECFOptional.ifPresent(cap -> cap.writeToNBT(pCompound));
        pCompound.putInt("queuedCFE", queuedECF);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        lazyECFOptional.ifPresent(cap -> cap.readFromNBT(pCompound));
        queuedECF = pCompound.getInt("queuedCFE");
    }

    @Override
    public void collectKnowledgeData(CompoundTag data) {

        lazyECFOptional.ifPresent(cfeHandler -> {
            data.putInt(TooltipHelper.Keys.ECF.toData(), cfeHandler.getECF());
            if (TCCommonConfigs.DEBUG.get()) {
                data.putInt(TooltipHelper.Keys.MAX_ECF.toData(), cfeHandler.getMaxECF());
                data.putInt(TooltipHelper.Keys.QUEUED.toData(), cfeHandler.getQueued());
            }
        });

        int priority = this.getPriority();

        data.putInt(TooltipHelper.Keys.PRIORITY.toData(), priority);
        data.putInt(TooltipHelper.Keys.RANGE.toData(), this.getRange());


    }

    @Override
    public void addTooltipLines(CompoundTag data, List<Component> tooltip, boolean isShifting) {

        if (isShifting) {
            TooltipHelper.addHeader(TooltipHelper.Headers.BLOCK, tooltip);
            if (TCCommonConfigs.DEBUG.get())
                TooltipHelper.addIfExist(TooltipHelper.Keys.PRIORITY, tooltip, data);
            TooltipHelper.addIfExist(TooltipHelper.Keys.RANGE, tooltip, data);

            TooltipHelper.addHeader(TooltipHelper.Headers.ECF, tooltip);

            TooltipHelper.addIfExist(TooltipHelper.Keys.ECF, tooltip, data);
            TooltipHelper.addIfExist(TooltipHelper.Keys.MAX_ECF, tooltip, data);
            TooltipHelper.addIfExist(TooltipHelper.Keys.QUEUED, tooltip, data);

            if (data.contains(TooltipHelper.Keys.PRIORITY.toData())) {
                int priority = data.getInt(TooltipHelper.Keys.PRIORITY.toData());
                if (priority == TCInnerConfig.DEFAULT_CONSUMER_PRIORITY) {
                    TooltipHelper.add(TooltipHelper.Keys.TYPE, TooltipHelper.Units.CONSUMER, tooltip);
                }
                if (priority == TCInnerConfig.DEFAULT_SOURCE_PRIORITY) {
                    TooltipHelper.add(TooltipHelper.Keys.TYPE, TooltipHelper.Units.SOURCE, tooltip);
                }
            }
        }
    }
}
