package net.sinedkadis.terracompositio.cfe.burst;

import lombok.Getter;
import lombok.Setter;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.sinedkadis.terracompositio.api.TCCapabilities;
import net.sinedkadis.terracompositio.api.dummies.DummyCFEHandler;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetworkMember;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetworkMemberBE;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetworkMemberEntity;
import net.sinedkadis.terracompositio.api.networks.cfe.ICFEHandler;
import net.sinedkadis.terracompositio.block.entity.PathPointerBlockEntity;
import net.sinedkadis.terracompositio.block.entity.TCBlockEntity;
import net.sinedkadis.terracompositio.cfe.CFEMemberProxy;
import net.sinedkadis.terracompositio.entity.custom.CFECloudEntity;
import net.sinedkadis.terracompositio.registries.TCEntities;
import net.sinedkadis.terracompositio.registries.TCItems;
import org.joml.Vector3f;

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


    private final BlockPos.MutableBlockPos lastBP = new BlockPos.MutableBlockPos();
    private int timeToLive = 130;
    private boolean noCollision = false;
    private int targetIndex = 0;

    @Getter
    @Setter
    Vector3f[] offsets = null;
    private double tickToLiveNoCol = 0;

    public CFEBurstProjectileEntity(EntityType<? extends ThrowableProjectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);

    }

    public CFEBurstProjectileEntity(double pX, double pY, double pZ, Level pLevel) {
        super(TCEntities.CFE_BURST_PROJECTILE.get(), pX, pY, pZ, pLevel);
    }

    private CFEBurstProjectileEntity(ICFEHandler pSource, ICFEHandler target, int cfe, float cfeTravelSpeed) {
        this(pSource.x(), pSource.y(), pSource.z(), pSource.getLevel());
        targetIndex = target.getIndex();
        init(pSource, pSource.getOffset().apply(Vec3.ZERO), target.getAttachedMember(), target.getOffset().apply(Vec3.ZERO), cfe, cfeTravelSpeed);
    }
    private CFEBurstProjectileEntity(ICFEHandler pSource, CFENetworkMember target, int cfe, float cfeTravelSpeed) {
        this(pSource.x(), pSource.y(), pSource.z(), pSource.getLevel());
        init(pSource, Vec3.ZERO, target, target.getMainHandler().getOffset().apply(Vec3.ZERO), cfe, cfeTravelSpeed);
    }



    private CFEBurstProjectileEntity(ICFEHandler pSource,Vec3 offset, ICFEHandler target, int cfe, float cfeTravelSpeed) {
        this(pSource.x()+offset.x,pSource.y()+offset.y,pSource.z()+offset.z,pSource.getLevel());
        init(pSource, offset, target.getAttachedMember(), target.getOffset().apply(Vec3.ZERO), cfe, cfeTravelSpeed);
    }

    public void noCollision(boolean noCollision) {
        this.noCollision = noCollision;
    }

    private void init(ICFEHandler pSource, Vec3 startOffset, CFENetworkMember attachedMember, Vec3 targetOffset, int cfe, float cfeTravelSpeed) {
        if (attachedMember instanceof LivingEntity livingEntity) {
            this.setOwner(livingEntity);
        }
        if (attachedMember instanceof CFEMemberProxy memberProxy) {
            CFENetworkMember target = memberProxy.target();
            if (target instanceof LivingEntity livingEntity) {
                this.setOwner(livingEntity);
            }
        }
        this.setO_CFE(cfe);
        this.setCFE(cfe);
        this.setNoGravity(true);
        float size = 0f;
        this.setBoundingBox(new AABB(size, size, size, size, size, size));
        Vec3 targetPos = attachedMember.getPos().getCenter().add(targetOffset);
        Vec3 startPos = pSource.getPos().getCenter().add(startOffset);
        Vec3 shootVec = targetPos.subtract(startPos);
        tickToLiveNoCol = shootVec.length() / cfeTravelSpeed;
        //pp proxy backdoor
        this.setTarget(attachedMember.getMainHandler().getAttachedMember().getPos());
        this.shoot(shootVec.x(), shootVec.y(), shootVec.z(), cfeTravelSpeed, 0);
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
    public static @Nullable CFEBurstProjectileEntity sendBurst(ICFEHandler pSource, CFENetworkMember target, int cfe, float cfeTravelSpeed) {
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

    boolean trackCrown = false;
    @Override
    public void tick() {
        super.tick();
        killIfTimeEnded();
        if (noCollision) {
            if (tickCount >= tickToLiveNoCol) {
                consumeTarget();
            }
        } else {
            calculateCollisions();
        }
        if (trackCrown)
            recalculateCrownOwnerTarget();
    }

    private void consumeTarget() {
        Entity owner = getOwner();
        if (owner instanceof CFENetworkMember cfeNetworkMember) {
            int cfe = this.getCFE();
            ICFEHandler cfeHandler = cfeNetworkMember.getHandler(targetIndex);
            int added = cfeHandler.addCFE(cfe, true);
            CFEBurstProjectileEntity burst = this.createPartWithCFE(added);
            int consumed = cfeHandler.addCFE(burst.getCFE(), false);
            burst.discard();
            if (consumed == cfe) {
                discard();
            } else {
                noCollision = false;
            }
        }

    }

    private void calculateCollisions() {
        BlockPos blockPos = BlockPos.containing(this.position());
        if (!blockPos.equals(lastBP) && tickCount > 1) {
            BlockEntity blockEntity = level().getBlockEntity(blockPos);
            int cfe = this.getCFE();
            if (blockEntity instanceof PathPointerBlockEntity pathPointerBlockEntity) {
                tryRedirectByPPBE(pathPointerBlockEntity,this.getDeltaMovement());
                lastBP.set(blockPos);
                return;

            }
            if (blockEntity instanceof TCBlockEntity memberBE) {
                tryConsumeTCBE(memberBE, cfe);
            } else if (blockEntity instanceof CFENetworkMemberBE memberBE) {
                tryConsumeCFEHandler(memberBE.getMainHandler(), cfe);
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
                && tickCount % 5 == 0) {
            setDeltaMovement(Vec3.ZERO);
            Vec3 shootVec = livingEntity.position().subtract(this.position());
            this.shoot(shootVec.x(),shootVec.y(),shootVec.z(),5/20f,0);
        }
    }

    private void tryRedirectByPPBE(PathPointerBlockEntity pathPointerBlockEntity, Vec3 burstDir) {
        if (!PathPointerBlockEntity.validAngle(pathPointerBlockEntity,burstDir.normalize())) return;

        timeToLive += 100;
        setDeltaMovement(Vec3.ZERO);
        setPos(pathPointerBlockEntity.getPos().getCenter());
        BlockPos bindPos = pathPointerBlockEntity.getReceiverPos();

        Vec3 shootVec;

        boolean bindposNotValid = bindPos == null || bindPos.equals(BlockPos.ZERO);
        boolean isEmitter = pathPointerBlockEntity.parts.contains(PathPointerBlockEntity.PPPart.EMITTER);
        boolean isInfuser = pathPointerBlockEntity.parts.contains(PathPointerBlockEntity.PPPart.INFUSER);

        if (bindposNotValid && isEmitter) {
            bindPos = getTarget();
            shootVec = pathPointerBlockEntity.getBlockPos().getCenter().vectorTo(bindPos.getCenter());
        } else if (bindposNotValid) {
                shootVec = new Vec3(0, 0, 1)
                        .yRot((float) Math.toRadians(pathPointerBlockEntity.getRotationYaw()))
                        .xRot((float) Math.toRadians(pathPointerBlockEntity.getRotationPitch()));
                if (isInfuser) trackCrown = true;
        } else shootVec = pathPointerBlockEntity.getBlockPos().getCenter().vectorTo(bindPos.getCenter());


        this.shoot(shootVec.x(),shootVec.y(),shootVec.z(),5 / 20f,0);

    }

    private void tryConsumeCFENetworkMemberEntity(BlockPos blockPos, int cfe) {
        level().getEntities(this, new AABB(blockPos)).stream()
                .filter(entity -> !(entity instanceof CFECloudEntity))
                .map(entity -> entity instanceof CFENetworkMemberEntity memberEntity ? memberEntity : null)
                .filter(Objects::nonNull)
                .map(CFENetworkMember::getMainHandler)
                .forEach(cfeHandler -> tryConsumeCFEHandler(cfeHandler, cfe));
    }

    private void tryConsumeCFEHandler(ICFEHandler icfeHandler, int cfe) {
        int consumed = icfeHandler.addCFE(cfe, false);
        this.setCFE(this.getCFE() - consumed);
        if (this.getCFE() <= 0) discard();
    }

    private void tryConsumeTCBE(TCBlockEntity memberBE, int cfe) {
        tryConsumeCFEHandler(memberBE.getCapability(TCCapabilities.CFE).orElse(DummyCFEHandler.instance), cfe);
    }

    @Override
    public void remove(RemovalReason pReason) {
        BlockEntity blockEntity = level().getBlockEntity(getTarget());
        if (blockEntity instanceof TCBlockEntity memberBE) {
            memberBE.getCapability(TCCapabilities.CFE).orElse(DummyCFEHandler.instance).subFromQueue(getO_CFE());
        } else if (blockEntity instanceof CFENetworkMemberBE memberBE) {
            memberBE.getHandler(targetIndex).subFromQueue(getO_CFE());
        } else {
            Entity target;
            Entity owner = getOwner();
            if (owner instanceof CFEBurstProjectileEntity main) {
                target = main.getOwner();
            } else if (owner instanceof CFENetworkMemberEntity || owner instanceof Player) {
                target = owner;
            } else {
                super.remove(pReason);
                return;
            }
            if (target instanceof CFENetworkMemberEntity || target instanceof Player) {
                assert target instanceof CFENetworkMemberEntity;
                CFENetworkMemberEntity cfeNetworkMemberEntity = ((CFENetworkMemberEntity) target);
                int cfe = getCFE();
                int oCfe = getO_CFE();
                cfeNetworkMemberEntity.getHandler(targetIndex).subFromQueue(Math.max(cfe, oCfe));
            }

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
