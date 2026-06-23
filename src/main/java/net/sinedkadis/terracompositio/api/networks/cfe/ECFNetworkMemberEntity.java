package net.sinedkadis.terracompositio.api.networks.cfe;


import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public interface ECFNetworkMemberEntity extends ECFNetworkMember {
    @SuppressWarnings("unchecked")
    default Entity getEntity(){
        return  ((Entity) this);
    }

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
