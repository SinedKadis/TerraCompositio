package net.sinedkadis.terracompositio.entity.custom;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.sinedkadis.terracompositio.api.TerraCompositioAPI;
import net.sinedkadis.terracompositio.api.dummies.DummyCFEHandler;
import net.sinedkadis.terracompositio.api.networks.NetworkAction;
import net.sinedkadis.terracompositio.api.networks.cfe.CFECapability;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetwork;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetworkMemberEntity;
import net.sinedkadis.terracompositio.api.networks.cfe.ICFEHandler;
import net.sinedkadis.terracompositio.cfe.LimitlessCFEContainer;
import net.sinedkadis.terracompositio.registries.TCEntities;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

// /kill @e[type=terracompositio:cfe_cloud_entity]

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CFECloudEntity extends Entity implements CFENetworkMemberEntity {
    private static final EntityDataAccessor<Integer> CFE_DATA =
            SynchedEntityData.defineId(CFECloudEntity.class, EntityDataSerializers.INT);

    protected LazyOptional<ICFEHandler> lazyCFEOptional = LazyOptional.of(() -> new LimitlessCFEContainer(this){
        @Override
        public int getCFE() {
            return getSyncedCFE();
        }

        @Override
        public void setCFE(int CFE) {
            setSyncedCFE(CFE);
        }
    });

    public CFECloudEntity(EntityType<? extends Entity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        setNoGravity(true);
        setInvulnerable(true);
    }

    public CFECloudEntity(Level pLevel,int cfe) {
        this(TCEntities.CFE_CLOUD.get(),pLevel);
        setSyncedCFE(cfe);

    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing) {
        if (capability == CFECapability.CFE)
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
        int cfe = getSyncedCFE();
        //todo find more accurate formula
        double size = Math.log10(cfe*cfe/4f);
        setBoundingBox(AABB.ofSize(position(),size,size,size));
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
    public int getLimit() {
        return 5;
    }

    @Override
    public int getPriority() {
        return -100;
    }


    @Override
    public ICFEHandler getMainHandler() {
        return lazyCFEOptional.orElse(DummyCFEHandler.instance);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        lazyCFEOptional.ifPresent(cap -> cap.writeToNBT(pCompound));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        lazyCFEOptional.ifPresent(cap -> cap.readFromNBT(pCompound));
    }
}
