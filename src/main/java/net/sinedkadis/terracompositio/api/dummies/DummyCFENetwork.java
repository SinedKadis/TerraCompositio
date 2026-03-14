package net.sinedkadis.terracompositio.api.dummies;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.sinedkadis.terracompositio.api.networks.NetworkAction;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetwork;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetworkMember;
import net.sinedkadis.terracompositio.api.networks.cfe.ICFEHandler;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Set;

public class DummyCFENetwork implements CFENetwork {
    public static final DummyCFENetwork instance = new DummyCFENetwork();


    @Override
    public @Nullable CFENetworkMember getMemberAt(Level level, BlockPos blockPos) {
        return null;
    }

    @Override
    public Set<CFENetworkMember> getAvailableNetworkTargets(CFENetworkMember sender) {
        return Set.of();
    }

    @Override
    public Set<CFENetworkMember> getAllCFENetworkMembers(Level level) {
        return Set.of();
    }

    @Override
    public void fireCFENetworkEvent(CFENetworkMember source, NetworkAction action) {

    }

    @Override
    public boolean isIn(Level pLevel, ICFEHandler cfeHandler) {
        return true;
    }

    @Override
    public boolean isIn(Level pLevel, CFENetworkMember cfeHandler) {
        return true;
    }

}
