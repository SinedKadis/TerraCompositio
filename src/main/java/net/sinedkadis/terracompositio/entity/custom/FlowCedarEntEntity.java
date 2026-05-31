package net.sinedkadis.terracompositio.entity.custom;

import lombok.Getter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemStackHandler;
import net.sinedkadis.terracompositio.api.TCCapabilities;
import net.sinedkadis.terracompositio.api.TerraCompositioAPI;
import net.sinedkadis.terracompositio.api.dummies.DummyCFEHandler;
import net.sinedkadis.terracompositio.api.networks.NetworkAction;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetwork;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetworkMember;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetworkMemberEntity;
import net.sinedkadis.terracompositio.api.networks.cfe.ICFEHandler;
import net.sinedkadis.terracompositio.block.entity.EntStatueBlockEntity;
import net.sinedkadis.terracompositio.cfe.CFEContainer;
import net.sinedkadis.terracompositio.cfe.CFEMemberProxy;
import net.sinedkadis.terracompositio.config.TCCommonConfigs;
import net.sinedkadis.terracompositio.config.TCInnerConfig;
import net.sinedkadis.terracompositio.entity.goals.CFEExtractGoal;
import net.sinedkadis.terracompositio.entity.goals.CFEHoldGoal;
import net.sinedkadis.terracompositio.entity.goals.ReachSourceGoal;
import net.sinedkadis.terracompositio.registries.TCBlocks;
import net.sinedkadis.terracompositio.registries.TCItems;
import net.sinedkadis.terracompositio.util.helpers.CFEHelper;
import net.sinedkadis.terracompositio.util.helpers.ParticleHelper;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

// /kill @e[type=terracompositio:flow_cedar_ent_entity]

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class FlowCedarEntEntity extends AbstractGolem implements CFENetworkMemberEntity {
    private static final EntityDataAccessor<Boolean> EXTRACTING =
            SynchedEntityData.defineId(FlowCedarEntEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> HOLDING =
            SynchedEntityData.defineId(FlowCedarEntEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> CFE_DATA =
            SynchedEntityData.defineId(FlowCedarEntEntity.class, EntityDataSerializers.INT);

    protected LazyOptional<ICFEHandler> lazyCFEOptional = LazyOptional.of(() -> new CFEContainer(this)
            .setMaxCFE(10000)
            .setOffset(vec3 -> vec3.add(0,this.getBbHeight() + (0.1f + (this.getSyncedCFE() / 10000d)) * 10 * 0.2f,0))
            .setIndex(0));
    @Getter
    protected LazyOptional<ICFEHandler> innerCFEOptional = LazyOptional.of(() -> new CFEContainer(this)
            .setMaxCFE(5 * 60 + 60)
            .setOffset(vec3 -> vec3.add(0,1,0))
            .setIndex(1));

    @Getter
    public final List<LazyOptional<ICFEHandler>> cfeHandlers = List.of(lazyCFEOptional,innerCFEOptional);
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState extractionAnimationState = new AnimationState();
    public final AnimationState extractionCompleteAnimationState = new AnimationState();
    public final AnimationState cfeHoldState = new AnimationState();

    protected int scheduledMembersUpdate = -1;
    protected Set<CFEMemberProxy> scheduledMembers = new HashSet<>();

    boolean scheduledUpdate = false;

    public FlowCedarEntEntity(EntityType<? extends AbstractGolem> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    public boolean canTakeItem(ItemStack pItemstack) {
        return pItemstack.is(TCItems.TECHNETIUM_CROWN.get());
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing) {
        if (capability == TCCapabilities.CFE)
            return lazyCFEOptional.cast();
        return super.getCapability(capability, facing);
    }

    int tickCounter = 20;
    private int lastSyncedEnergy = -1;
    @Override
    public void tick() {
        super.tick();
        updateIfScheduled();
        ItemStack item = this.getItemBySlot(EquipmentSlot.HEAD);
        item.getItem().inventoryTick(item,level(),this,3,false);
        if (this.level().isClientSide()) {
            setupAnimationStates();
        } else {
            // Серверная логика
            CFENetwork cfeNetworkInstance = TerraCompositioAPI.instance().getCFENetworkInstance();
            boolean inNetwork = cfeNetworkInstance.isIn(this.level(), this);
            if (!inNetwork && !this.isRemoved()) {
                cfeNetworkInstance.fireCFENetworkEvent(this, NetworkAction.ADD);
            }

            lazyCFEOptional.ifPresent(icfeHandler -> {
                int currentEnergy = icfeHandler.getCFE();

                if (currentEnergy > 10000) abortCFEConsume();

                if (currentEnergy != lastSyncedEnergy) {
                    setSyncedCFE(currentEnergy);
                    lastSyncedEnergy = currentEnergy;
                }

                innerCFEOptional.ifPresent(icfeHandler1 -> {
                    tickCounter--;
                    if (tickCounter <= 0) {
                        CFEHelper.tryCFETransfer(icfeHandler1, icfeHandler);
                        tickCounter = 20;
                        icfeHandler1.takeCFE(1, false);
                        if (icfeHandler1.getCFE() <= 0) {
                            this.turnIntoStatue();
                        }
                    }
                });
            });
        }
    }

    public void turnIntoStatue() {
        ItemStack crown = this.getItemBySlot(EquipmentSlot.HEAD);
        this.level().setBlock(this.blockPosition(),
                TCBlocks.FLOW_CEDAR_ENT_STATUE.get().defaultBlockState(),3);
        BlockEntity blockEntity = this.level().getBlockEntity(this.blockPosition());
        if (blockEntity instanceof EntStatueBlockEntity entStatueBlockEntity) {
            entStatueBlockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(iItemHandler -> {
                if (iItemHandler instanceof ItemStackHandler itemStackHandler)
                    itemStackHandler.setStackInSlot(0,crown);
            });

        }
        this.remove(RemovalReason.CHANGED_DIMENSION);
    }

    @SuppressWarnings("deprecation")
    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData, @Nullable CompoundTag pDataTag) {
        this.innerCFEOptional.ifPresent(icfeHandler -> icfeHandler.setCFE(level().getRandom().nextInt(60,360)));
        return super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(EXTRACTING, false);
        this.entityData.define(HOLDING, false);
        this.entityData.define(CFE_DATA, 0);
    }

    public void setExtracting(boolean extracting) {
        this.entityData.set(EXTRACTING, extracting);
    }

    public boolean isExtracting() {
        return this.entityData.get(EXTRACTING);
    }

    public void setHolding(boolean extracting) {
        this.entityData.set(HOLDING, extracting);
    }

    public boolean isHolding() {
        return this.entityData.get(HOLDING);
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
        innerCFEOptional.invalidate();
    }

    boolean wasHeld = false;
    private void setupAnimationStates() {
        idleAnimationState.startIfStopped(this.tickCount);

        extractionAnimationState.animateWhen(this.isExtracting(),this.tickCount);
        cfeHoldState.animateWhen(this.isHolding(),this.tickCount);
        if (!isHolding() && wasHeld){
            cfeHoldState.stop();
            extractionCompleteAnimationState.start(this.tickCount);
        }
        wasHeld = isHolding();

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
        this.goalSelector.addGoal(0, new CFEHoldGoal(this));
//        this.goalSelector.addGoal(1, new BreedGoal(this, 1.15D));
//        this.goalSelector.addGoal(2, new TemptGoal(this, 1.2D, Ingredient.of(Items.COOKED_BEEF), false));

        this.goalSelector.addGoal(3, new ReachSourceGoal(this,1.2D,32,3));
        this.goalSelector.addGoal(3, new CFEExtractGoal(this,3));
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
    public BlockPos getPos() {
        return this.blockPosition();
    }

    @Override
    public int getRange() {
        return 5;
    }

    @Override
    public int getPriority() {
        return TCInnerConfig.DEFAULT_SOURCE_PRIORITY;
    }

    @Override
    public Vec3 particleTargetOffset() {
        Optional<ICFEHandler> icfeHandler = lazyCFEOptional.resolve();
        float scale = 3;
        if (icfeHandler.isPresent() && icfeHandler.get().getCFE() > 0) {
            scale = (0.1f + (getSyncedCFE() / (float) icfeHandler.get().getMaxCFE())) * 10;
        }
        return new Vec3(0.5d,this.getBbHeight() + scale * 0.2f,0.5d);
    }

    @Override
    public ICFEHandler getMainHandler() {
        return lazyCFEOptional.orElse(DummyCFEHandler.instance);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        lazyCFEOptional.ifPresent(cap -> cap.writeToNBT(pCompound));
        innerCFEOptional.ifPresent(cap -> cap.writeToNBT(pCompound));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        lazyCFEOptional.ifPresent(cap -> cap.readFromNBT(pCompound));
        innerCFEOptional.ifPresent(cap -> cap.readFromNBT(pCompound));
    }


    public void abortCFEConsume() {
        lazyCFEOptional.ifPresent(icfeHandler -> {
            if (!this.level().isClientSide()){
                float scale = (0.1f + (icfeHandler.getCFE() / (float) icfeHandler.getMaxCFE())) * 10;
                ParticleHelper.spawnParticlesIn(this.level(), BlockPos.containing(this.position().add(0, this.getBbHeight() + scale * 0.2f, 0)), icfeHandler.getCFE() / 10);
                icfeHandler.setCFE(0);
            }
        });

    }

    public void sendViaPP(CFEMemberProxy current) {
        if (getMainHandler().getCFE() > 0 && CFEHelper.validMember(current)) {
            if (current.getMainHandler().getFreeSpace() > TCCommonConfigs.CFE_PER_BURST_TRANSFER_LIMIT.get())
                scheduleMemberUpdate(current);
            CFEHelper.tryCFETransfer(current, this, TCCommonConfigs.CFE_PER_BURST_TRANSFER_LIMIT.get(), 5 / 20f);
        } else onCFENetworkMemberUpdate();
    }

    @Override
    public void scheduleMemberUpdate(CFENetworkMember updated) {
        if (updated instanceof CFEMemberProxy proxy) {
            this.scheduledMembers.add(proxy);
            if (scheduledMembersUpdate < 0) scheduledMembersUpdate = TCCommonConfigs.TICKS_BETWEEN_BURSTS.get();
        }
    }

    @Override
    public void scheduleMemberUpdate() {
        scheduledUpdate = true;
    }

    @Override
    public void updateIfScheduled() {
        if (scheduledUpdate) {
            scheduledUpdate = false;
            onCFENetworkMemberUpdate();
        }
        if (scheduledMembersUpdate == 0) {
            scheduledMembersUpdate = -1;
            Set<CFEMemberProxy> scheduledMembers1 = Set.copyOf(this.scheduledMembers);
            this.scheduledMembers.clear();
            scheduledMembers1.forEach(this::sendViaPP);
        } else if (scheduledMembersUpdate > 0)
            scheduledMembersUpdate--;
    }
}
