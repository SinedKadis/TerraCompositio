package net.sinedkadis.terracompositio.entity.custom;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.sinedkadis.terracompositio.api.TerraCompositioAPI;
import net.sinedkadis.terracompositio.api.dummies.DummyCFEHandler;
import net.sinedkadis.terracompositio.api.networks.NetworkAction;
import net.sinedkadis.terracompositio.api.TCCapabilities;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetwork;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetworkMember;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetworkMemberEntity;
import net.sinedkadis.terracompositio.api.networks.cfe.ICFEHandler;
import net.sinedkadis.terracompositio.cfe.LimitlessCFEContainer;
import net.sinedkadis.terracompositio.cfe.burst.CFEBurstProjectileEntity;
import net.sinedkadis.terracompositio.config.TCCommonConfigs;
import net.sinedkadis.terracompositio.config.TCInnerConfig;
import net.sinedkadis.terracompositio.registries.TCEntities;
import net.sinedkadis.terracompositio.util.TCUtil;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashSet;
import java.util.Set;
import java.util.function.ToIntFunction;

// /kill @e[type=terracompositio:cfe_cloud_entity]

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CFECloudEntity extends Entity implements CFENetworkMemberEntity {
    private static final EntityDataAccessor<Integer> CFE_DATA =
            SynchedEntityData.defineId(CFECloudEntity.class, EntityDataSerializers.INT);
    private int queuedCFE;
    boolean scheduleUpdate = false;
    protected Set<CFENetworkMember> scheduledMembers = new HashSet<>();
    protected int scheduledMembersUpdate = -1;


    protected LazyOptional<ICFEHandler> lazyCFEOptional = LazyOptional.of(() -> new LimitlessCFEContainer(this){
        @Override
        public int getCFE() {
            return getSyncedCFE();
        }

        @Override
        public void setCFE(int CFE) {
            setSyncedCFE(CFE);
        }

        @Override
        public int sendCFE(int cfe, ICFEHandler target, float speed, boolean simulate) {
            int freeSpace = target.getFreeSpace();
            int added = Mth.clamp(cfe, 0, freeSpace);
            if (added < 1)
                return 0;
            if (!simulate) {
                Vec3 burstOffset = getBurstOffset(target);
                BlockPos offset = BlockPos.containing(this.getPos().getCenter().add(burstOffset));
                if (offset.closerThan(target.getPos(),2)) {
                    target.addCFE(added,false);
                    return added;
                }
                CFEBurstProjectileEntity entity = CFEBurstProjectileEntity.sendBurst(this, burstOffset,target,added,speed);
                if (entity != null)
                    target.addToQueue(added);
            }
            return added;
        }

        @Override
        public int addCFE(int cfe, boolean simulate) {
            int pMax = getMaxCFE() - getCFE();
            int added = Mth.clamp(cfe, 0, pMax);
            if (!simulate) {
                queuedCFE += added;
                //subFromQueue(added);
                sendCFEUpdate();
                onContentsChanged();
            }
            return added;
        }
    });

    @Override
    public void onCFENetworkMemberUpdate() {
        if (getPriority() < 0 && getMainHandler().getCFE() > 0){
            CFENetwork cfeNetwork = TerraCompositioAPI.instance().getCFENetworkInstance();
            Set<CFENetworkMember> targets = cfeNetwork.getAvailableNetworkTargets(this);
            targets.forEach(target -> {
                if (target.getMainHandler().getFreeSpace() > TCCommonConfigs.CFE_PER_BURST_TRANSFER_LIMIT.get())
                    scheduleMemberUpdate(target);
                TCUtil.tryCFETransfer(target, this);
            });
        }
    }

    @Override
    public void onCFENetworkMemberUpdate(CFENetworkMember updated) {
        if (getPriority() < 0 && getMainHandler().getCFE() > 0 && TCUtil.validMember(updated)){
            if (updated.getMainHandler().getFreeSpace() > TCCommonConfigs.CFE_PER_BURST_TRANSFER_LIMIT.get())
                scheduleMemberUpdate(updated);
            TCUtil.tryCFETransfer(updated,this);
        }
    }

    private Vec3 getBurstOffset(ICFEHandler target) {
        double r = getRadius();
        BlockPos sourcePos = this.getPos();
        BlockPos targetPos = target.getPos();
        if (sourcePos.closerThan(targetPos,r)) return targetPos.subtract(sourcePos).getCenter();
        return sourcePos.getCenter().vectorTo(targetPos.getCenter()).normalize().scale(r);
    }

    public static final ToIntFunction<Integer> RENDER_COUNT_FUNCTION = cfe ->
            (int) Math.ceil(cfe * 0.1f);
    private double getRadius() {
        int cfe = RENDER_COUNT_FUNCTION.applyAsInt(getSyncedCFE());
        float k = (float) Math.log10(cfe);
        double baseRadius = 0.2 + 0.3 * Math.log1p(cfe * 0.1);
        return baseRadius * k;
    }

    public CFECloudEntity(EntityType<? extends Entity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        setNoGravity(true);
        setInvulnerable(true);
    }

    public CFECloudEntity(Level pLevel) {
        this(TCEntities.CFE_CLOUD.get(),pLevel);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing) {
        if (capability == TCCapabilities.CFE)
            return lazyCFEOptional.cast();
        return super.getCapability(capability, facing);
    }

    @Override
    public void tick() {
        super.tick();
        CFENetwork cfeNetworkInstance = TerraCompositioAPI.instance().getCFENetworkInstance();
        boolean inNetwork = cfeNetworkInstance.isIn(this.level(), this);
        if (!inNetwork && !this.isRemoved()) {
            cfeNetworkInstance.fireCFENetworkEvent(this, NetworkAction.ADD);
        }
        updateIfScheduled();
        if (getSyncedCFE() <= 0) discard();

        if (queuedCFE > 0){
            int toAdd = (int) Math.ceil(queuedCFE*0.1f);
            if (toAdd < 1) toAdd = queuedCFE;
            queuedCFE -= toAdd;

            setSyncedCFE(getSyncedCFE()+toAdd);
        }

    }

    public AABB getBoundingBox() {
        double radius = getRadius()*2;
        return AABB.ofSize(position(), radius,radius,radius);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(CFE_DATA, 0);
    }

    public int getSyncedCFE() {
        return this.entityData.get(CFE_DATA);
    }

    public void setSyncedCFE(int amount) {
        if (!this.level().isClientSide()) {
            this.entityData.set(CFE_DATA, amount);
        }
    }

    @Override
    public void remove(RemovalReason pReason) {
        super.remove(pReason);
        TerraCompositioAPI.INSTANCE.getCFENetworkInstance().fireCFENetworkEvent(this, NetworkAction.REMOVE);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyCFEOptional.invalidate();
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
        scheduleUpdate = true;
    }
    @Override
    public void scheduleMemberUpdate(CFENetworkMember updated) {
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
    public ICFEHandler getMainHandler() {
        return lazyCFEOptional.orElse(DummyCFEHandler.instance);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        lazyCFEOptional.ifPresent(cap -> cap.writeToNBT(pCompound));
        pCompound.putInt("queuedCFE", queuedCFE);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        lazyCFEOptional.ifPresent(cap -> cap.readFromNBT(pCompound));
        queuedCFE = pCompound.getInt("queuedCFE");
    }
}
