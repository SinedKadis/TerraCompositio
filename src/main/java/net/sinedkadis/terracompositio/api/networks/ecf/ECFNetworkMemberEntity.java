package net.sinedkadis.terracompositio.api.networks.ecf;


import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * The Entity ECF Network Member. Implement this to your {@link Entity}.
 */
public interface ECFNetworkMemberEntity extends ECFNetworkMember {
    @SuppressWarnings("unchecked")
    default Entity getEntity(){
        return  ((Entity) this);
    }

    /**
     * Gets entity position.
     *
     * @return the position
     */
    default Vec3 getPosition() {
        return getEntity().position();
    }
    default Level getLevel() {
        return getEntity().level();
    }
    default BlockPos getPos() {
        return getEntity().blockPosition();
    }

}
