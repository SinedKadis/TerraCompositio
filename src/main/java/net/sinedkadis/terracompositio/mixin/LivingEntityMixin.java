package net.sinedkadis.terracompositio.mixin;

import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.sinedkadis.terracompositio.util.accessors.LivingEntityAnimationAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements LivingEntityAnimationAccessor {

    public LivingEntityMixin(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }
    @Unique
    public final AnimationState terraCompositio$idleAnimationState = new AnimationState();

    @Unique
    @Override
    public AnimationState terraCompositio$getIdleAnimationState() {
        return this.terraCompositio$idleAnimationState;
    }

}
