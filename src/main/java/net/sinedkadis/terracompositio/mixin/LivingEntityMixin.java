package net.sinedkadis.terracompositio.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.sinedkadis.terracompositio.mixin.accessors.LivingEntityAccessor;
import net.sinedkadis.terracompositio.util.LivingEntityAnimationAccessor;
import net.sinedkadis.terracompositio.registries.TCBlocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;



import static net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED;
import static org.objectweb.asm.Opcodes.PUTFIELD;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements LivingEntityAnimationAccessor {
    public LivingEntityMixin(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Shadow public abstract boolean isAlive();

    @Redirect(method = "baseTick", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/LivingEntity;lastPos:Lnet/minecraft/core/BlockPos;", opcode = PUTFIELD))
    private void redirectLastPosAssignment(LivingEntity instance, BlockPos value) {
        LivingEntityAccessor accessor = (LivingEntityAccessor) instance;
        BlockPos oldPos = accessor.getLastPos();
        if (oldPos == null) {
            accessor.setLastPos(value);
            return;
        }
        oldPos = oldPos.below();
        BlockState blockState = level().getBlockState(oldPos);
        if (blockState.is(TCBlocks.TECHNETIUM_BOARD.get())){
            List<Entity> entities = level().getEntities(null, new AABB(oldPos.above(), oldPos.above(2)));
            if (entities.isEmpty()) {
                BlockState replaceState = blockState.hasProperty(WATERLOGGED)
                        && blockState.getValue(WATERLOGGED)
                        ? Blocks.WATER.defaultBlockState()
                        : Blocks.AIR.defaultBlockState();
                level().setBlockAndUpdate(oldPos, replaceState);
            }
        }

        accessor.setLastPos(value);
    }

    @Unique
    public final AnimationState terraCompositio$idleAnimationState = new AnimationState();

    @Unique
    @Override
    public AnimationState terraCompositio$getIdleAnimationState() {
        return this.terraCompositio$idleAnimationState;
    }

}
