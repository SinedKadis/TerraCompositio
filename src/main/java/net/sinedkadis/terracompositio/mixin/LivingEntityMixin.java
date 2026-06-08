package net.sinedkadis.terracompositio.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.sinedkadis.terracompositio.block.custom.TechnetiumBoardBlock;
import net.sinedkadis.terracompositio.item.custom.TechnetiumArmorItem;
import net.sinedkadis.terracompositio.mixin.accessors.LivingEntityAccessor;
import net.sinedkadis.terracompositio.registries.TCBlocks;
import net.sinedkadis.terracompositio.registries.TCItems;
import net.sinedkadis.terracompositio.util.accessors.LivingEntityAnimationAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED;
import static org.objectweb.asm.Opcodes.PUTFIELD;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements LivingEntityAnimationAccessor {
    public LivingEntityMixin(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Shadow public abstract boolean isAlive();

    @Inject(
            method = "onChangedBlock(Lnet/minecraft/core/BlockPos;)V",
            at = @At("RETURN")
    )
    private void terraCompositio$onBlockChanged(BlockPos pPos, CallbackInfo ci) {
        TechnetiumArmorItem.onBlockChanged((LivingEntity)(Object)this);
    }

    @Unique
    public final AnimationState terraCompositio$idleAnimationState = new AnimationState();

    @Unique
    @Override
    public AnimationState terraCompositio$getIdleAnimationState() {
        return this.terraCompositio$idleAnimationState;
    }

}
