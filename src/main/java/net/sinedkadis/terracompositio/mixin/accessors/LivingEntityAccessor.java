package net.sinedkadis.terracompositio.mixin.accessors;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {
    @Accessor("lastPos")
    BlockPos getLastPos();

    @Accessor("lastPos")
    void setLastPos(BlockPos pos);
}
