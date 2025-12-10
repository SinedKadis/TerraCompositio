package net.sinedkadis.terracompositio.entity.custom;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.sinedkadis.terracompositio.registries.TCEntities;
import net.sinedkadis.terracompositio.registries.TCItems;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CFEBallProjectileEntity extends ThrowableItemProjectile {
    private static final EntityDataAccessor<Integer> DATA_VISITED_BLOCKS = SynchedEntityData.defineId(CFEBallProjectileEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Long> DATA_LAST_VISITED = SynchedEntityData.defineId(CFEBallProjectileEntity.class, EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Integer> DATA_TARGET_HEIGHT = SynchedEntityData.defineId(CFEBallProjectileEntity.class, EntityDataSerializers.INT);


    public CFEBallProjectileEntity(EntityType<? extends ThrowableItemProjectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public CFEBallProjectileEntity( double pX, double pY, double pZ, Level pLevel) {
        super(TCEntities.CFE_BALL_PROJECTILE.get(), pX, pY, pZ, pLevel);
        setTargetHeight((int) pY-1);
        setNoGravity(true);
    }

    public CFEBallProjectileEntity(Level pLevel, LivingEntity livingEntity) {
        super(TCEntities.CFE_BALL_PROJECTILE.get(), livingEntity, pLevel);
        Entity owner = this.getOwner();
        if (owner != null) {
            setTargetHeight((int) (owner.position().y-1));
        }
        setNoGravity(true);
    }

    public CFEBallProjectileEntity(BlockSource pSource) {
        this(pSource.x(),pSource.y(),pSource.z(),pSource.getLevel());
    }


    @Override
    protected Item getDefaultItem() {
        return TCItems.CFE_BALL.get();
    }

    @Override
    public void tick() {
        super.tick();

        BlockPos blockPos = BlockPos.containing(position());

        if (!blockPos.equals(BlockPos.of(getLastVisited()))) {
            CFEDropProjectileEntity dropProjectile = new CFEDropProjectileEntity(this,level());
            level().addFreshEntity(dropProjectile);
            setLastVisited(blockPos.asLong());
            addVisitedBlocks();
        }


        Entity owner = getOwner();
        int visitedBlocks = getVisitedBlocks();
        if (visitedBlocks > (owner != null ? 7 : 10)) {
            this.discard();
        }
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.getEntityData().define(DATA_LAST_VISITED, 0L);
        this.getEntityData().define(DATA_VISITED_BLOCKS, 0);
        this.getEntityData().define(DATA_TARGET_HEIGHT, -64);
    }

    public void setLastVisited(long packed) {
        this.entityData.set(DATA_LAST_VISITED, packed);
    }

    public long getLastVisited() {
        return this.entityData.get(DATA_LAST_VISITED);
    }

    public void setVisitedBlocks(int visited) {
        this.entityData.set(DATA_VISITED_BLOCKS, visited);
    }

    public int getVisitedBlocks() {
        return this.entityData.get(DATA_VISITED_BLOCKS);
    }

    public void addVisitedBlocks() {
        this.setVisitedBlocks(this.getVisitedBlocks()+1);
    }

    public void setTargetHeight(int height) {
        this.entityData.set(DATA_TARGET_HEIGHT, height);
    }

    public int getTargetHeight() {
        return this.entityData.get(DATA_TARGET_HEIGHT);
    }


}
