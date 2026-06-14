package net.sinedkadis.terracompositio.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.sinedkadis.terracompositio.item.custom.TechnetiumArmorItem;
import net.sinedkadis.terracompositio.util.accessors.LivingEntityAnimationAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import static org.objectweb.asm.Opcodes.PUTFIELD;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements LivingEntityAnimationAccessor {
    @Shadow
    private BlockPos lastPos;

    public LivingEntityMixin(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }


    @Redirect(method = "baseTick", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/LivingEntity;lastPos:Lnet/minecraft/core/BlockPos;", opcode = PUTFIELD))
    private void redirectLastPosAssignment(LivingEntity instance, BlockPos value) {
        TechnetiumArmorItem.onBlockChanged((LivingEntity) (Object) this, lastPos);
        lastPos = value;
    }

    @Unique
    public final AnimationState terraCompositio$idleAnimationState = new AnimationState();

    @Unique
    @Override
    public AnimationState terraCompositio$getIdleAnimationState() {
        return this.terraCompositio$idleAnimationState;
    }

}
