package net.sinedkadis.terracompositio.mixin;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.sinedkadis.terracompositio.util.IEntityInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@MethodsReturnNonnullByDefault
@Mixin(Entity.class)
public abstract class EntityMixin implements IEntityInstance {

    @Shadow
    public abstract BlockPos blockPosition();

    @Shadow
    public abstract Vec3 position();

    @Override
    public BlockEntity tc$asBE() {
        throw new RuntimeException("Tried to get BlockEntity from Entity");
    }

    @Override
    public Entity tc$asEntity() {
        return ((Entity) (Object) this);
    }

    @Shadow
    public abstract Level level();

    @Override
    public BlockPos tc$getBlockPos() {
        return blockPosition();
    }

    @Override
    public Vec3 tc$getPosition() {
        return position();
    }

    @Override
    public Level tc$getLevel() {
        return level();
    }

    @Override
    public boolean tc$isEntity() {
        return true;
    }

    @Override
    public BlockState tc$getBlockState() {
        return Blocks.AIR.defaultBlockState();
    }
}
