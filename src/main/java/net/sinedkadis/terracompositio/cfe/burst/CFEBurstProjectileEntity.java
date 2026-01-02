package net.sinedkadis.terracompositio.cfe.burst;

import lombok.extern.slf4j.Slf4j;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetworkMember;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetworkMemberBE;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetworkMemberEntity;
import net.sinedkadis.terracompositio.api.networks.cfe.ICFEHandler;
import net.sinedkadis.terracompositio.entity.custom.CFECloudEntity;
import net.sinedkadis.terracompositio.registries.TCEntities;
import net.sinedkadis.terracompositio.registries.TCItems;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

@Slf4j
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CFEBurstProjectileEntity extends ThrowableProjectile {
    private static final EntityDataAccessor<Integer> CFE = SynchedEntityData.defineId(CFEBurstProjectileEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> O_CFE = SynchedEntityData.defineId(CFEBurstProjectileEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<BlockPos> TARGET = SynchedEntityData.defineId(CFEBurstProjectileEntity.class, EntityDataSerializers.BLOCK_POS);


    private float cfeTravelSpeed;
    private final BlockPos.MutableBlockPos lastBP = new BlockPos.MutableBlockPos();
    public CFEBurstProjectileEntity(EntityType<? extends ThrowableProjectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);

    }

    public CFEBurstProjectileEntity(double pX, double pY, double pZ, Level pLevel) {
        super(TCEntities.CFE_BURST_PROJECTILE.get(), pX, pY, pZ, pLevel);
    }

    private CFEBurstProjectileEntity(ICFEHandler pSource, ICFEHandler target, int cfe, float cfeTravelSpeed) {
        this(pSource.x(), pSource.y(), pSource.z(), pSource.getLevel());
        init(pSource,Vec3.ZERO, target, cfe, cfeTravelSpeed);
    }



    private CFEBurstProjectileEntity(ICFEHandler pSource,Vec3 offset, ICFEHandler target, int cfe, float cfeTravelSpeed) {
        this(pSource.x()+offset.x,pSource.y()+offset.y,pSource.z()+offset.z,pSource.getLevel());
        init(pSource,offset,target,cfe,cfeTravelSpeed);
    }

    private void init(ICFEHandler pSource,Vec3 offset, ICFEHandler target, int cfe, float cfeTravelSpeed) {
        CFENetworkMember attachedMember = target.getAttachedMember();
        if (attachedMember instanceof LivingEntity livingEntity) {
            this.setOwner(livingEntity);
        }
        this.setO_CFE(cfe);
        this.setTarget(target.getPos());
        this.setCFE(cfe);
        this.setNoGravity(true);
        float size = 0f;
        this.setBoundingBox(new AABB(size, size, size, size, size, size));
        BlockPos shootVec = target.getPos().subtract(BlockPos.containing(pSource.getPos().getCenter().add(offset)));
        this.cfeTravelSpeed = cfeTravelSpeed;
        this.shoot(shootVec.getX(), shootVec.getY(), shootVec.getZ(), cfeTravelSpeed, 0);
        lastBP.set(pSource.getPos());
        pSource.getLevel().addFreshEntity(this);
    }
    private CFEBurstProjectileEntity(int cfe,CFEBurstProjectileEntity owner) {
        this(owner.getX(), owner.getY(), owner.getZ(), owner.level());
        this.setOwner(owner);
        this.setCFE(cfe);
    }

    public static @Nullable CFEBurstProjectileEntity sendBurst(ICFEHandler pSource, ICFEHandler target, int cfe, float cfeTravelSpeed) {
        if (cfe < 1) {
            return null;
        }
        return new CFEBurstProjectileEntity(pSource, target, cfe, cfeTravelSpeed);
    }
    public static @Nullable CFEBurstProjectileEntity sendBurst(ICFEHandler pSource, Vec3 offset, ICFEHandler target, int cfe, float cfeTravelSpeed) {
        if (cfe < 1) {
            return null;
        }
        return new CFEBurstProjectileEntity(pSource,offset, target, cfe, cfeTravelSpeed);
    }

    public CFEBurstProjectileEntity createPartWithCFE(int cfe) {
        return new CFEBurstProjectileEntity(cfe,this);
    }


    @Override
    public void tick() {
        super.tick();
        Entity owner = getOwner();
        if (owner instanceof LivingEntity livingEntity
                && livingEntity.getItemBySlot(EquipmentSlot.HEAD).is(TCItems.TECHNETIUM_CROWN.get())
                && tickCount % 20 == 0) {
            setDeltaMovement(Vec3.ZERO);
            Vec3 shootVec = livingEntity.position().subtract(this.position());
            this.shoot(shootVec.x(),shootVec.y(),shootVec.z(),cfeTravelSpeed,0);
        }
        if (tickCount > 100) discard();
        BlockPos blockPos = BlockPos.containing(this.position());
        if (!blockPos.equals(lastBP) && tickCount > 1) {
            BlockEntity blockEntity = level().getBlockEntity(blockPos);
            int cfe = this.getCFE();
            if (blockEntity instanceof CFENetworkMemberBE memberBE) {
                int added = memberBE.getMainHandler().addCFE(cfe, true);
                int consumed = memberBE.consumeCFEBurst(this.createPartWithCFE(added));
                if (consumed == cfe) discard();
            } else {
                level().getEntities(null, AABB.of(BoundingBox.fromCorners(blockPos,blockPos))).stream()
                        .filter(entity -> !(entity instanceof CFECloudEntity))
                        .map(entity -> entity instanceof CFENetworkMemberEntity memberEntity ? memberEntity : null)
                        .filter(Objects::nonNull)
                        .forEach(memberEntity -> {
                            int added = memberEntity.getMainHandler().addCFE(cfe, true);
                            int consumed = memberEntity.consumeCFEBurst(this.createPartWithCFE(added));
                            if (consumed == cfe) discard();
                        });
            }

        }
        lastBP.set(blockPos);

    }

    @Override
    public void remove(RemovalReason pReason) {
        BlockEntity blockEntity = level().getBlockEntity(getTarget());
        if (blockEntity instanceof  CFENetworkMemberBE memberBE) {
            memberBE.getMainHandler().subFromQueue(getO_CFE());
        }
        super.remove(pReason);
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(CFE, 0);
        entityData.define(O_CFE,0);
        entityData.define(TARGET,BlockPos.ZERO);
    }

    public int getCFE() {
        return entityData.get(CFE);
    }
    public void setCFE(int cfe) {
        entityData.set(CFE,cfe);
    }

    public int getO_CFE() {
        return entityData.get(O_CFE);
    }
    public void setO_CFE(int o_CFE) {
        entityData.set(O_CFE,o_CFE);
    }

    public BlockPos getTarget() {
        return entityData.get(TARGET);
    }
    public void setTarget(BlockPos blockPos) {
        entityData.set(TARGET,blockPos);
    }





}
