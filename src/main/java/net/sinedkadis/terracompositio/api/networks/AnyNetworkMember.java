package net.sinedkadis.terracompositio.api.networks;

import net.sinedkadis.terracompositio.util.IEntityInstance;

/**
 * The Common parts of Each Network Member.
 */
public interface AnyNetworkMember {
    /**
     * Gets entity. May be {@link net.minecraft.world.level.block.entity.BlockEntity},
     * or {@link net.minecraft.world.entity.Entity}
     *
     * @return the entity
     */
    IEntityInstance getEntityInstance();


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
