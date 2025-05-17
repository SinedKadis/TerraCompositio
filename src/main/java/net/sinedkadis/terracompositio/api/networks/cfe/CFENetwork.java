package net.sinedkadis.terracompositio.api.networks.cfe;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.sinedkadis.terracompositio.api.networks.NetworkAction;
import net.sinedkadis.terracompositio.cfe.CFECapability;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.Optional;

public interface CFENetwork {
    @Nullable CFENetworkMemberBE getClosestSourceWithCFE(BlockPos pos, Level level, int limit, int priority);
    @Nullable CFENetworkMemberBE getRandomSourceInRange(BlockPos pos, Level level, int limit, int priority);
    List<CFENetworkMemberBE> getAllCFENetworkMembers(Level level);
    void fireCFENetworkEvent(CFENetworkMemberBE source, NetworkAction action);
    boolean isIn(Level pLevel, ICFEHandler cfeHandler);
    boolean isIn(Level pLevel, CFENetworkMemberBE cfeHandler);
    static Optional<ICFEHandler> getCFEHandler(CFENetworkMemberBE source){
        return source.getBE().getCapability(CFECapability.CFE).resolve();
    }
}
