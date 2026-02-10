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
import net.sinedkadis.terracompositio.api.TCCapabilities;
import net.sinedkadis.terracompositio.api.behaviors.blockentity.IBEBehaviour;
import net.sinedkadis.terracompositio.api.dummies.DummyCFEHandler;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetworkMember;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetworkMemberBE;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetworkMemberEntity;
import net.sinedkadis.terracompositio.api.networks.cfe.ICFEHandler;
import net.sinedkadis.terracompositio.block.behaviours.pp_behaviours.ReceiverBehaviour;
import net.sinedkadis.terracompositio.block.behaviours.pp_behaviours.SenderBehaviour;
import net.sinedkadis.terracompositio.block.entity.PathPointerBlockEntity;
import net.sinedkadis.terracompositio.block.entity.TCBlockEntity;
import net.sinedkadis.terracompositio.entity.custom.CFECloudEntity;
import net.sinedkadis.terracompositio.registries.TCEntities;
import net.sinedkadis.terracompositio.registries.TCItems;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CFEBurstProjectileEntity extends ThrowableProjectile {
    private static final EntityDataAccessor<Integer> CFE = SynchedEntityData.defineId(CFEBurstProjectileEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> O_CFE = SynchedEntityData.defineId(CFEBurstProjectileEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<BlockPos> TARGET = SynchedEntityData.defineId(CFEBurstProjectileEntity.class, EntityDataSerializers.BLOCK_POS);


    private float cfeTravelSpeed;
    private final BlockPos.MutableBlockPos lastBP = new BlockPos.MutableBlockPos();
    private int timeToLive = 100;

    public CFEBurstProjectileEntity(EntityType<? extends ThrowableProjectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);

    }

    public CFEBurstProjectileEntity(double pX, double pY, double pZ, Level pLevel) {
        super(TCEntities.CFE_BURST_PROJECTILE.get(), pX, pY, pZ, pLevel);
    }

    private CFEBurstProjectileEntity(ICFEHandler pSource, ICFEHandler target, int cfe, float cfeTravelSpeed) {
        this(pSource.x(), pSource.y(), pSource.z(), pSource.getLevel());
        init(pSource,Vec3.ZERO, target.getPos(),target.getAttachedMember(), cfe, cfeTravelSpeed);
    }



    private CFEBurstProjectileEntity(ICFEHandler pSource,Vec3 offset, ICFEHandler target, int cfe, float cfeTravelSpeed) {
        this(pSource.x()+offset.x,pSource.y()+offset.y,pSource.z()+offset.z,pSource.getLevel());
        init(pSource,offset,target.getPos(),target.getAttachedMember(),cfe,cfeTravelSpeed);
    }
    private CFEBurstProjectileEntity(ICFEHandler pSource,BlockPos targetPos,@Nullable CFENetworkMember attachedMember, int cfe, float cfeTravelSpeed) {
        this(pSource.x(),pSource.y(),pSource.z(),pSource.getLevel());
        init(pSource,Vec3.ZERO,targetPos,attachedMember,cfe,cfeTravelSpeed);
    }

    private void init(ICFEHandler pSource,Vec3 offset,BlockPos targetPos,@Nullable CFENetworkMember attachedMember, int cfe, float cfeTravelSpeed) {
        if (attachedMember instanceof LivingEntity livingEntity) {
            this.setOwner(livingEntity);
        }
        this.setO_CFE(cfe);
        this.setTarget(targetPos);
        this.setCFE(cfe);
        this.setNoGravity(true);
        float size = 0f;
        this.setBoundingBox(new AABB(size, size, size, size, size, size));
        BlockPos shootVec = targetPos.subtract(BlockPos.containing(pSource.getPos().getCenter().add(offset)));
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

    public static @Nullable CFEBurstProjectileEntity sendBurst(ICFEHandler pSource, BlockPos targetPos, int cfe, float cfeTravelSpeed) {
        if (cfe < 1) {
            return null;
        }
        return new CFEBurstProjectileEntity(pSource, targetPos,null, cfe, cfeTravelSpeed);
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
        recalculateCrownOwnerTarget();
        killIfTimeEnded();
        calculateCollisions();

    }

    private void calculateCollisions() {
        BlockPos blockPos = BlockPos.containing(this.position());
        if (!blockPos.equals(lastBP) && tickCount > 1) {
            BlockEntity blockEntity = level().getBlockEntity(blockPos);
            int cfe = this.getCFE();
            if (blockEntity instanceof PathPointerBlockEntity pathPointerBlockEntity) {
                if (tryRedirectByPPBE(pathPointerBlockEntity,blockPos)) {
                    lastBP.set(blockPos);
                    return;
                }
            }
            if (blockEntity instanceof TCBlockEntity memberBE) {
                tryConsumeTCBE(memberBE, cfe);
            } else if (blockEntity instanceof CFENetworkMemberBE memberBE) {
                tryConsumeCFENetworkMemberBE(memberBE.getMainHandler(), cfe);
            } else {
                tryConsumeCFENetworkMemberEntity(blockPos, cfe);
            }

        }
        lastBP.set(blockPos);
    }



    private void killIfTimeEnded() {
        if (tickCount > timeToLive) discard();
    }

    private void recalculateCrownOwnerTarget() {
        Entity owner = getOwner();
        if (owner instanceof LivingEntity livingEntity
                && livingEntity.getItemBySlot(EquipmentSlot.HEAD).is(TCItems.TECHNETIUM_CROWN.get())
                && tickCount % 20 == 0) {
            setDeltaMovement(Vec3.ZERO);
            Vec3 shootVec = livingEntity.position().subtract(this.position());
            this.shoot(shootVec.x(),shootVec.y(),shootVec.z(),cfeTravelSpeed,0);
        }
    }

    private boolean tryRedirectByPPBE(PathPointerBlockEntity pathPointerBlockEntity,BlockPos currentPos) {
        List<IBEBehaviour> behaviours = pathPointerBlockEntity.getBehaviours();
        Optional<SenderBehaviour> sender = behaviours.stream()
                .map(ibeBehaviour -> ibeBehaviour instanceof SenderBehaviour senderBehaviour ? senderBehaviour : null)
                .filter(Objects::nonNull)
                .findAny();
        if (sender.isPresent()) {
            Optional<ReceiverBehaviour> receiver = behaviours.stream()
                    .map(ibeBehaviour -> ibeBehaviour instanceof ReceiverBehaviour senderBehaviour ? senderBehaviour : null)
                    .filter(Objects::nonNull)
                    .findAny();
            if (receiver.isPresent()) {
                if (!receiver.get().validAngle(currentPos)) return false;
                timeToLive+=100;
                setDeltaMovement(Vec3.ZERO);
                Vec3 shootVec = sender.get().getBindPos().subtract(currentPos).getCenter();
                this.shoot(shootVec.x(),shootVec.y(),shootVec.z(),cfeTravelSpeed,0);
                return true;
            }
        }
        return false;
    }

    private void tryConsumeCFENetworkMemberEntity(BlockPos blockPos, int cfe) {
        level().getEntities(null, AABB.of(BoundingBox.fromCorners(blockPos, blockPos))).stream()
                .filter(entity -> !(entity instanceof CFECloudEntity))
                .map(entity -> entity instanceof CFENetworkMemberEntity memberEntity ? memberEntity : null)
                .filter(Objects::nonNull)
                .forEach(memberEntity -> {
                    int added = memberEntity.getMainHandler().addCFE(cfe, true);
                    CFEBurstProjectileEntity burst = this.createPartWithCFE(added);
                    int consumed = memberEntity.getMainHandler().addCFE(burst.getCFE(), false);
                    burst.discard();
                    if (consumed == cfe) discard();
                });
    }

    private void tryConsumeCFENetworkMemberBE(ICFEHandler icfeHandler, int cfe) {
        int added = icfeHandler.addCFE(cfe, true);
        CFEBurstProjectileEntity burst = this.createPartWithCFE(added);
        int consumed = icfeHandler.addCFE(burst.getCFE(), false);
        burst.discard();
        if (consumed == cfe) discard();
    }

    private void tryConsumeTCBE(TCBlockEntity memberBE, int cfe) {
        tryConsumeCFENetworkMemberBE(memberBE.getCapability(TCCapabilities.CFE).orElse(DummyCFEHandler.instance), cfe);
    }

    @Override
    public void remove(RemovalReason pReason) {
        BlockEntity blockEntity = level().getBlockEntity(getTarget());
        if (blockEntity instanceof TCBlockEntity memberBE) {
            memberBE.getCapability(TCCapabilities.CFE).orElse(DummyCFEHandler.instance).subFromQueue(getO_CFE());
        } else if (blockEntity instanceof CFENetworkMemberBE memberBE) {
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
