package net.sinedkadis.terracompositio.util;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

//DO NOT IMPLEMENT ANYWHERE MORE
@MethodsReturnNonnullByDefault
public interface IEntityInstance {
    static IEntityInstance wrap(Entity entity) {
        return ((IEntityInstance) entity);
    }

    static IEntityInstance wrap(BlockEntity entity) {
        return ((IEntityInstance) entity);
    }

    BlockPos tc$getBlockPos();

    Vec3 tc$getPosition();

    Level tc$getLevel();

    boolean tc$isEntity();

    default boolean tc$isBlock() {
        return !tc$isEntity();
    }

    BlockState tc$getBlockState();

    BlockEntity tc$asBE();

    Entity tc$asEntity();
}
