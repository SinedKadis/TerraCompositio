package net.sinedkadis.terracompositio.entity.custom;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.sinedkadis.terracompositio.registries.TCBlocks;
import net.sinedkadis.terracompositio.registries.TCEntities;
import net.sinedkadis.terracompositio.registries.TCItems;
import net.sinedkadis.terracompositio.util.helpers.BlockPosHelper;

import javax.annotation.ParametersAreNonnullByDefault;

import static net.sinedkadis.terracompositio.config.TCCommonConfigs.PLATFORM_ALIVE_PER_PLAYER;


@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CFEDropProjectileEntity extends ThrowableProjectile implements ItemSupplier {


    public CFEDropProjectileEntity(EntityType<? extends ThrowableProjectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public CFEDropProjectileEntity(double pX, double pY, double pZ, Level pLevel) {
        super(TCEntities.CFE_DROP_PROJECTILE.get(), pX, pY, pZ, pLevel);
        setDeltaMovement(0,-1,0);
    }

    public CFEDropProjectileEntity(CFEBallProjectileEntity pShooter, Level pLevel) {
        this(pShooter.getX(), pShooter.getY(), pShooter.getZ(), pLevel);
        this.setOwner(pShooter.getOwner());
    }

    @Override
    public void tick() {
        super.tick();
        BlockPos blockPos = BlockPos.containing(position());
        if (this.tickCount > 1) {
            BlockHitResult blockHitResult = new BlockHitResult(position(), Direction.UP, blockPos, false);
            this.onHitBlock(blockHitResult);
            this.level().gameEvent(GameEvent.PROJECTILE_LAND, blockPos, GameEvent.Context.of(this, this.level().getBlockState(blockPos)));
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult pResult) {
        BlockPos blockPos = pResult.getBlockPos();
        if (!level().isClientSide() && level().getBlockState(blockPos).is(BlockTags.REPLACEABLE)) {
            level().setBlockAndUpdate(blockPos, TCBlocks.TECHNETIUM_BOARD.get().defaultBlockState()
                    .setValue(BlockStateProperties.WATERLOGGED,false));
            Entity owner = getOwner();
            if (owner != null) {
                CompoundTag persistentData = owner.getPersistentData();
                int ballsThrew = persistentData.getInt("balls_threw");
                ListTag list = persistentData.getList("platform_on_throw_" + (ballsThrew % PLATFORM_ALIVE_PER_PLAYER.get()), Tag.TAG_COMPOUND);
                list.add(BlockPosHelper.saveBlockPos(blockPos));
                persistentData.put("platform_on_throw_" + (ballsThrew % PLATFORM_ALIVE_PER_PLAYER.get()), list);

            }
        }
        this.discard();
    }

    @Override
    protected void defineSynchedData() {

    }

    @Override
    public ItemStack getItem() {
        return TCItems.CFE_BALL.get().getDefaultInstance();
    }
}
