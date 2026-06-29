package net.sinedkadis.terracompositio.mixin;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.sinedkadis.terracompositio.util.IEntityInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;

@MethodsReturnNonnullByDefault
@Mixin(BlockEntity.class)
public abstract class BlockEntityMixin implements IEntityInstance {
    @Shadow
    public abstract BlockPos getBlockPos();

    @Shadow
    @Nullable
    public abstract Level getLevel();

    @Shadow
    public abstract BlockState getBlockState();

    @Override
    public BlockEntity tc$asBE() {
        return ((BlockEntity) (Object) this);
    }

    @Override
    public Entity tc$asEntity() {
        throw new RuntimeException("Tried to get Entity from BlockEntity");
    }

    @Override
    public BlockPos tc$getBlockPos() {
        return getBlockPos();
    }

    @Override
    public Vec3 tc$getPosition() {
        return getBlockPos().getCenter();
    }

    @Override
    public Level tc$getLevel() {
        Level level = getLevel();
        if (level == null) throw new RuntimeException("Method called before level became not null");
        return level;
    }

    @Override
    public boolean tc$isEntity() {
        return false;
    }

    @Override
    public BlockState tc$getBlockState() {
        return getBlockState();
    }
}
