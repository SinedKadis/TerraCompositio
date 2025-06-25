package net.sinedkadis.terracompositio.api.networks.cfe;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.sinedkadis.terracompositio.api.networks.NetworkAction;
import net.sinedkadis.terracompositio.cfe.CFECapability;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.Optional;

public interface CFENetwork {
    @Nullable CFENetworkMember getClosestSourceWithCFE(BlockPos pos, Level level, int limit, int priority);
    @Nullable CFENetworkMember getRandomSourceInRange(BlockPos pos, Level level, int limit, int priority);
    List<CFENetworkMember> getAllCFENetworkMembers(Level level);
    void fireCFENetworkEvent(CFENetworkMember source, NetworkAction action);
    boolean isIn(Level pLevel, ICFEHandler cfeHandler);
    boolean isIn(Level pLevel, CFENetworkMember cfeHandler);
    static Optional<ICFEHandler> getCFEHandler(CFENetworkMember source){
        if (source instanceof CFENetworkMemberBE cfeNetworkMemberBE)
            return cfeNetworkMemberBE.getBE().getCapability(CFECapability.CFE).resolve();
        if (source instanceof CFENetworkMemberEntity cfeNetworkMemberEntity)
            return cfeNetworkMemberEntity.getEntity().getCapability(CFECapability.CFE).resolve();
        return Optional.empty();
    }
}
