package net.sinedkadis.terracompositio.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.sinedkadis.terracompositio.block.custom.TechnetiumBoardBlock;
import net.sinedkadis.terracompositio.mixin.accessors.LivingEntityAccessor;
import net.sinedkadis.terracompositio.registries.TCItems;
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
        if (instance.isShiftKeyDown() || instance.getItemBySlot(EquipmentSlot.FEET).is(TCItems.TECHNETIUM_BOOTS.get())) {
            accessor.setLastPos(value);
            return;
        }
        BlockPos lastPos = accessor.getLastPos();
        if (lastPos != null) {
            BlockState lastState = level().getBlockState(lastPos.below());
            if (lastState.is(TCBlocks.TECHNETIUM_BOARD.get())) {
                level().setBlockAndUpdate(lastPos.below(),lastState.setValue(TechnetiumBoardBlock.WAS_USED,true));
            }
        }

        for (BlockPos pos : BlockPos.betweenClosed(
                instance.blockPosition().offset(-2,-2,-2),
                instance.blockPosition().offset(2,2,2))) {
            BlockState blockState = level().getBlockState(pos);

            if (blockState.is(TCBlocks.TECHNETIUM_BOARD.get())){
                if (!blockState.getValue(TechnetiumBoardBlock.WAS_USED)) continue;
                List<Entity> entities = level().getEntities(null, new AABB(
                        pos.offset(-1,1,-1),
                        pos.offset(1,2,1)));
                if (entities.isEmpty()) {
                    BlockState replaceState = blockState.hasProperty(WATERLOGGED)
                            && blockState.getValue(WATERLOGGED)
                            ? Blocks.WATER.defaultBlockState()
                            : Blocks.AIR.defaultBlockState();
                    level().setBlockAndUpdate(pos, replaceState);
                }
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
