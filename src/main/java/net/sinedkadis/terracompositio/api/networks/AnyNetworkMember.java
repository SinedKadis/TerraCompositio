package net.sinedkadis.terracompositio.api.networks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * The Common parts of Each Network Member.
 */
public interface AnyNetworkMember {
    /**
     * Gets entity. May be {@link net.minecraft.world.level.block.entity.BlockEntity},
     * or {@link net.minecraft.world.entity.Entity}, depends on implementation
     *
     * @param <T> the type parameter
     * @return the entity
     */
    <T> T getEntity();

    /**
     * Gets level.
     *
     * @return the level
     */
    Level getLevel();

    /**
     * Gets block pos.
     *
     * @return the pos
     */
    BlockPos getPos();

    /**
     * Gets range. Using to filter interactions between network members and other purposes
     *
     * @return the range
     */
    int getRange();

    /**
     * Gets priority. Using to filter interactions between network members and other purposes. Default as -100 for sources, 100 for consumers
     *
     * @return the priority
     */
    int getPriority();
}
