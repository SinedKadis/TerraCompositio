package net.sinedkadis.terracompositio.ecf.burst;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.sinedkadis.terracompositio.api.dummies.DummyECFHandler;
import net.sinedkadis.terracompositio.api.networks.ecf.ECFNetworkMember;
import net.sinedkadis.terracompositio.api.networks.ecf.ECFNetworkMemberBE;
import net.sinedkadis.terracompositio.api.networks.ecf.ECFNetworkMemberEntity;
import net.sinedkadis.terracompositio.api.networks.ecf.IECFHandler;
import net.sinedkadis.terracompositio.api.registries.TCCapabilities;
import net.sinedkadis.terracompositio.block.entity.PathPointerBlockEntity;
import net.sinedkadis.terracompositio.block.entity.TCBlockEntity;
import net.sinedkadis.terracompositio.ecf.PPECFMemberProxy;
import net.sinedkadis.terracompositio.registries.TCEntities;
import net.sinedkadis.terracompositio.registries.TCItems;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@Slf4j
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ECFBurstProjectileEntity extends ThrowableProjectile {
    private static final EntityDataAccessor<Integer> ECF = SynchedEntityData.defineId(ECFBurstProjectileEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<BlockPos> TARGET = SynchedEntityData.defineId(ECFBurstProjectileEntity.class, EntityDataSerializers.BLOCK_POS);


    private final BlockPos.MutableBlockPos lastBP = new BlockPos.MutableBlockPos();
    private int timeToLive = 100;

    @Getter
    @Setter
    Vector3f[] offsets = null;

    public ECFBurstProjectileEntity(EntityType<? extends ThrowableProjectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);

    }

    public ECFBurstProjectileEntity(double pX, double pY, double pZ, Level pLevel) {
        super(TCEntities.ECF_BURST_PROJECTILE.get(), pX, pY, pZ, pLevel);
    }

    private ECFBurstProjectileEntity(IECFHandler pSource, ECFNetworkMember target, int cfe, float cfeTravelSpeed) {
        this(pSource, Vec3.ZERO, target, cfe, cfeTravelSpeed);
    }


    private ECFBurstProjectileEntity(IECFHandler pSource, Vec3 startOffset, ECFNetworkMember target, int cfe, float cfeTravelSpeed) {
        this(pSource.getPos(), startOffset, target, cfe, cfeTravelSpeed);
    }

    private ECFBurstProjectileEntity(BlockPos pSource, Vec3 startOffset, ECFNetworkMember target, int cfe, float cfeTravelSpeed) {
        this(pSource.getX() + startOffset.x + 0.5f, pSource.getY() + startOffset.y + 0.5f, pSource.getZ() + startOffset.z + 0.5f, target.getLevel());

        Vec3 offset;
        if (target instanceof PPECFMemberProxy) {
            offset = Vec3.ZERO;
        } else {
            offset = target.getMainHandler().getOffset().apply(Vec3.ZERO);
        }
        if (target instanceof LivingEntity livingEntity) {
            this.setOwner(livingEntity);
        }
        if (target instanceof PPECFMemberProxy memberProxy) {
            ECFNetworkMember target1 = memberProxy.target();
            if (target1 instanceof LivingEntity livingEntity) {
                this.setOwner(livingEntity);
            }
        }
        this.setECF(cfe);
        this.setNoGravity(true);
        float size = 0f;
        this.setBoundingBox(new AABB(size, size, size, size, size, size));
        Vec3 targetPos = target.getPos().getCenter().add(offset);
        Vec3 startPos = pSource.getCenter().add(startOffset);
        Vec3 shootVec = targetPos.subtract(startPos);
        //pp proxy backdoor
        this.setTarget(target.getMainHandler().getAttachedMember().getPos());
        this.shoot(shootVec.x(), shootVec.y(), shootVec.z(), cfeTravelSpeed, 0);
        lastBP.set(pSource);
    }

    public static @Nullable ECFBurstProjectileEntity sendBurst(IECFHandler pSource, ECFNetworkMember target, int cfe, float cfeTravelSpeed) {
        if (cfe < 1) {
            return null;
        }
        return new ECFBurstProjectileEntity(pSource, target, cfe, cfeTravelSpeed);
    }

    public static @Nullable ECFBurstProjectileEntity sendBurst(BlockPos pSource, ECFNetworkMember target, int cfe, float cfeTravelSpeed) {
        if (cfe < 1) {
            return null;
        }
        return new ECFBurstProjectileEntity(pSource, Vec3.ZERO, target, cfe, cfeTravelSpeed);
    }

    public static @Nullable ECFBurstProjectileEntity sendBurst(IECFHandler pSource, Vec3 offset, ECFNetworkMember target, int cfe, float cfeTravelSpeed) {
        if (cfe < 1) {
            return null;
        }
        return new ECFBurstProjectileEntity(pSource, offset, target, cfe, cfeTravelSpeed);
    }

    boolean trackCrown = false;
    @Override
    public void tick() {
        killIfTimeEnded();
        calculateCollisions();

        if (trackCrown)
            recalculateCrownOwnerTarget();

        calculateMovement();
    }

    private void calculateMovement() {
        Vec3 vec3 = this.getDeltaMovement();
        double d2 = this.getX() + vec3.x;
        double d0 = this.getY() + vec3.y;
        double d1 = this.getZ() + vec3.z;
        this.updateRotation();
        float f;
        if (this.isInWater()) {
            for (int i = 0; i < 4; ++i) {
                float f1 = 0.25F;
                this.level().addParticle(ParticleTypes.BUBBLE,
                        d2 - vec3.x * f1,
                        d0 - vec3.y * f1,
                        d1 - vec3.z * f1,
                        vec3.x,
                        vec3.y,
                        vec3.z);
            }
            f = 0.8F;
        } else {
            f = 0.99F;
        }

        this.setDeltaMovement(vec3.scale(f));
        if (!this.isNoGravity()) {
            Vec3 vec31 = this.getDeltaMovement();
            this.setDeltaMovement(vec31.x, vec31.y - (double) this.getGravity(), vec31.z);
        }

        this.setPos(d2, d0, d1);
    }

    private void consumeOwner() {
        Entity owner = getOwner();
        if (owner instanceof ECFNetworkMemberEntity memberEntity) {
            int oCfe = this.getECF();
            int cfe = oCfe - tryConsumeCFEHandler(memberEntity.getMainHandler(), oCfe);

            if (cfe > 0) {
                for (ItemStack stack : owner.getArmorSlots()) {
                    IECFHandler IECFHandler = stack.getCapability(TCCapabilities.ECF).orElse(DummyECFHandler.instance);
                    cfe -= IECFHandler.addECF(cfe, false);
                    if (cfe <= 0) break;
                }
            }

        }
    }

    private void calculateCollisions() {
        Entity owner = getOwner();
        if (owner instanceof ECFNetworkMemberEntity memberEntity) {
            Vec3 position = this.position();
            Vec3 offsettedPos = memberEntity.getMainHandler().getOffset().apply(owner.position());
            double distanceToSqr = position.distanceToSqr(offsettedPos);
            if (distanceToSqr < 1.1f) {
                consumeOwner();
                return;
            }
        }
        BlockPos blockPos = BlockPos.containing(this.position());
        if (!blockPos.equals(lastBP) && tickCount > 1) {
            BlockEntity blockEntity = level().getBlockEntity(blockPos);
            if (blockEntity instanceof PathPointerBlockEntity pathPointerBlockEntity) {
                tryRedirectByPPBE(pathPointerBlockEntity,this.getDeltaMovement());
                lastBP.set(blockPos);
                return;
            }
            BlockPos target = getTarget();
            if (blockPos.equals(target) && blockEntity != null) {
                tryConsumeCFEHandler(blockEntity.getCapability(TCCapabilities.ECF).orElse(DummyECFHandler.instance), this.getECF());
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
                && tickCount % 5 == 0
                && owner instanceof ECFNetworkMemberEntity memberEntity) {
            setDeltaMovement(Vec3.ZERO);
            Vec3 shootVec = memberEntity.getMainHandler()
                    .getOffset().apply(livingEntity.position()).subtract(this.position());
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
        Level level = pathPointerBlockEntity.getLevel();
        if (level == null) return;
        level.playSound(null,
                pathPointerBlockEntity.getBlockPos(),
                SoundEvents.BAMBOO_WOOD_HIT,
                SoundSource.BLOCKS,
                0.1f,1);
    }

    private int tryConsumeCFEHandler(IECFHandler IECFHandler, int cfe) {
        int added = IECFHandler.addECF(cfe, false);
        discard();
        return added;
    }

    @Override
    public void remove(RemovalReason pReason) {
        if (isRemoved()) {
            return;
        }
        super.remove(pReason);

        BlockEntity blockEntity = level().getBlockEntity(getTarget());
        int cfe = getECF();
        if (blockEntity instanceof ECFNetworkMemberBE memberBE) {
            memberBE.getMainHandler().subFromQueue(cfe);
        } else if (blockEntity instanceof TCBlockEntity tcBlockEntity) {
            tcBlockEntity.getCapability(TCCapabilities.ECF).orElse(DummyECFHandler.instance)
                    .subFromQueue(cfe);
        } else {
            Entity target;
            Entity owner = getOwner();
            if (owner instanceof ECFNetworkMemberEntity || owner instanceof Player) {
                target = owner;
            } else {
                return;
            }
            assert target instanceof ECFNetworkMemberEntity;
            ECFNetworkMemberEntity cfeNetworkMemberEntity = ((ECFNetworkMemberEntity) target);

            cfeNetworkMemberEntity.getMainHandler().subFromQueue(cfe);

        }
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(ECF, 0);
        entityData.define(TARGET,BlockPos.ZERO);
    }

    public int getECF() {
        return entityData.get(ECF);
    }

    public void setECF(int cfe) {
        entityData.set(ECF, cfe);
    }

    public BlockPos getTarget() {
        return entityData.get(TARGET);
    }
    public void setTarget(BlockPos blockPos) {
        entityData.set(TARGET,blockPos);
    }





}
