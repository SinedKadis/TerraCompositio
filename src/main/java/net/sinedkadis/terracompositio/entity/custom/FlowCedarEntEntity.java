package net.sinedkadis.terracompositio.entity.custom;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.sinedkadis.terracompositio.api.TerraCompositioAPI;
import net.sinedkadis.terracompositio.api.networks.NetworkAction;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetwork;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetworkMemberEntity;
import net.sinedkadis.terracompositio.api.networks.cfe.ICFEHandler;
import net.sinedkadis.terracompositio.cfe.CFECapability;
import net.sinedkadis.terracompositio.cfe.CFEContainer;
import net.sinedkadis.terracompositio.entity.goals.ReachSourceGoal;
import net.sinedkadis.terracompositio.entity.goals.TreeExtractGoal;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;


@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class FlowCedarEntEntity extends AbstractGolem implements CFENetworkMemberEntity {
    private static final EntityDataAccessor<Boolean> EXTRACTING =
            SynchedEntityData.defineId(FlowCedarEntEntity.class, EntityDataSerializers.BOOLEAN);

    protected LazyOptional<ICFEHandler> lazyCFEOptional = LazyOptional.of(() -> new CFEContainer(this,5*60+60));
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState extractionAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();

    @Setter @Getter @Nullable
    private BlockPos sourcePos;

    public FlowCedarEntEntity(EntityType<? extends AbstractGolem> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing) {
        if (capability == CFECapability.CFE)
            return lazyCFEOptional.cast();
        return super.getCapability(capability, facing);
    }

    int tickCounter = 20;
    @Override
    public void tick() {
        super.tick();
        if(this.level().isClientSide()) {
            setupAnimationStates();
        }
        if (!this.level().isClientSide()) {
            CFENetwork cfeNetworkInstance = TerraCompositioAPI.instance().getCFENetworkInstance();
            boolean inNetwork = cfeNetworkInstance.isIn(this.level(), this);
            if (!inNetwork && !this.isRemoved()) {
                cfeNetworkInstance.fireCFENetworkEvent(this, NetworkAction.ADD);
            }
        }
        getCapability(CFECapability.CFE).ifPresent(icfeHandler -> {
            icfeHandler.containerTick();
            tickCounter--;
            if (tickCounter <= 0){
                tickCounter = 20;
                icfeHandler.takeCFE(1, false);
            }
        });

    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(EXTRACTING, false);
    }

    public void setExtracting(boolean attacking) {
        this.entityData.set(EXTRACTING, attacking);
    }

    public boolean isExtracting() {
        return this.entityData.get(EXTRACTING);
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

    private void setupAnimationStates() {
        idleAnimationState.startIfStopped(this.tickCount);

        extractionAnimationState.animateWhen(this.isExtracting(),this.tickCount);
//        extractionAnimationState.ifStarted(animationState -> {
//            extractAnimationCooldown = 13*20;
//        });
//        if (!extractionAnimationState.isStarted()){
//            --this.extractAnimationCooldown;
//        }

//        if(!this.isExtracting()) {
//            extractionAnimationState.ifStarted(AnimationState::stop);
//        }
    }
    @Override
    protected void updateWalkAnimation(float pPartialTick) {
        float f;
        if(this.getPose() == Pose.STANDING) {
            f = Math.min(pPartialTick * 6F, 1f);
        } else {
            f = 0f;
        }

        this.walkAnimation.update(f, 0.2f);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));

//        this.goalSelector.addGoal(1, new BreedGoal(this, 1.15D));
//        this.goalSelector.addGoal(2, new TemptGoal(this, 1.2D, Ingredient.of(Items.COOKED_BEEF), false));

        this.goalSelector.addGoal(3, new ReachSourceGoal(this,1.2D,32,5));
        TreeExtractGoal treeExtractGoal = new TreeExtractGoal(this);
        this.goalSelector.addGoal(3, treeExtractGoal);
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 3f));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));

    }



    public static AttributeSupplier.Builder createAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 20D)
                .add(Attributes.FOLLOW_RANGE, 24D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ARMOR_TOUGHNESS, 0.1f)
                .add(Attributes.ATTACK_KNOCKBACK, 0.5f)
                .add(Attributes.ATTACK_DAMAGE, 2f);
    }

    @Override
    protected @Nullable SoundEvent getHurtSound(DamageSource pDamageSource) {
        return SoundEvents.WOOD_HIT;
    }

    @Override
    protected @Nullable SoundEvent getDeathSound() {
        return SoundEvents.WOOD_BREAK;
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return SoundEvents.WOOD_FALL;
    }

    @Override
    public Level getLevel() {
        return this.level();
    }

    @Override
    public BlockPos getBlockPos() {
        return this.getOnPos(1);
    }

    @Override
    public int getLimit() {
        return 5;
    }

    @Override
    public int getPriority() {
        return 512;
    }

    @Override
    public Vec3 particleTargetOffset() {
        return new Vec3(0.5d,1.5d,0.5d);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        getCapability(CFECapability.CFE).ifPresent(cap -> cap.writeToNBT(pCompound));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        getCapability(CFECapability.CFE).ifPresent(cap -> cap.readFromNBT(pCompound));
    }


    public void abortCFEConsume() {

    }
}
