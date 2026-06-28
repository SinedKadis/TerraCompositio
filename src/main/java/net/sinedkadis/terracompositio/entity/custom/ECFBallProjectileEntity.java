package net.sinedkadis.terracompositio.entity.custom;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.sinedkadis.terracompositio.api.helpers.BlockPosHelper;
import net.sinedkadis.terracompositio.registries.TCEntities;
import net.sinedkadis.terracompositio.registries.TCItems;

import javax.annotation.ParametersAreNonnullByDefault;

import static net.sinedkadis.terracompositio.config.TCCommonConfigs.PLATFORM_ALIVE_PER_PLAYER;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ECFBallProjectileEntity extends ThrowableItemProjectile {
    private static final EntityDataAccessor<Integer> DATA_VISITED_BLOCKS = SynchedEntityData.defineId(ECFBallProjectileEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Long> DATA_LAST_VISITED = SynchedEntityData.defineId(ECFBallProjectileEntity.class, EntityDataSerializers.LONG);

    public ECFBallProjectileEntity(EntityType<? extends ThrowableItemProjectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public ECFBallProjectileEntity(double pX, double pY, double pZ, Level pLevel) {
        super(TCEntities.ECF_BALL_PROJECTILE.get(), pX, pY, pZ, pLevel);
//        setTargetHeight((int) pY-1);
        setNoGravity(true);
    }

    public ECFBallProjectileEntity(Level pLevel, LivingEntity livingEntity) {
        super(TCEntities.ECF_BALL_PROJECTILE.get(), livingEntity, pLevel);
//        Entity owner = this.getOwner();
//        if (owner != null) {
//            setTargetHeight((int) (owner.position().y-1));
//        }
        setNoGravity(true);

        Entity owner = getOwner();
        if (owner == null) return;
        CompoundTag persistentData = owner.getPersistentData();
        int ballsThrew = persistentData.getInt("balls_threw");
        persistentData.putInt("balls_threw", ballsThrew + 1);
        ListTag list = persistentData.getList("platform_on_throw_" + (ballsThrew % PLATFORM_ALIVE_PER_PLAYER.get()), Tag.TAG_COMPOUND);
        for (Tag tag : list) {
            BlockPos pPos = BlockPosHelper.loadBlockPos(tag);
            if (pPos != null)
                level().setBlock(pPos, Blocks.AIR.defaultBlockState(), 3);

        }
    }

    public ECFBallProjectileEntity(BlockSource pSource) {
        this(pSource.x(),pSource.y(),pSource.z(),pSource.getLevel());
    }


    @Override
    protected Item getDefaultItem() {
        return TCItems.ECF_CHARGE.get();
    }

    @Override
    public void tick() {
        super.tick();

        BlockPos blockPos = BlockPos.containing(position());

        if (!blockPos.equals(BlockPos.of(getLastVisited()))) {
            ECFDropProjectileEntity dropProjectile = new ECFDropProjectileEntity(this,level());
            level().addFreshEntity(dropProjectile);
            setLastVisited(blockPos.asLong());
            addVisitedBlocks();
        }


        Entity owner = getOwner();
        int visitedBlocks = getVisitedBlocks();
        if (visitedBlocks > (owner != null ? 7 : 10) || tickCount > 100) {
            this.discard();
        }
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.getEntityData().define(DATA_LAST_VISITED, 0L);
        this.getEntityData().define(DATA_VISITED_BLOCKS, 0);
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


}
