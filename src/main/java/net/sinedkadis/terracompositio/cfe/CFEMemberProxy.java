package net.sinedkadis.terracompositio.cfe;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetworkMember;
import net.sinedkadis.terracompositio.api.networks.cfe.ICFEHandler;
import net.sinedkadis.terracompositio.block.entity.PathPointerBlockEntity;

public record CFEMemberProxy(CFENetworkMember target, PathPointerBlockEntity proxy) implements CFENetworkMember {

    @Override
    public int getRange() {
        return target.getRange();
    }

    @Override
    public int getPriority() {
        return target.getPriority();
    }

    @Override
    public ICFEHandler getMainHandler() {
        return target.getMainHandler();
    }

    @Override
    public <T> T getEntity() {
        return target.getEntity();
    }

    @Override
    public Level getLevel() {
        return proxy.getLevel();
    }

    @Override
    public BlockPos getPos() {
        return proxy.getBlockPos();
    }

    @Override
    public void updateIfScheduled() {
        target.updateIfScheduled();
    }

    @Override
    public void scheduleMemberUpdate() {
        target.scheduleMemberUpdate();
    }

    @Override
    public void scheduleMemberUpdate(CFENetworkMember updated) {
        target.scheduleMemberUpdate(updated);
    }

    @Override
    public void onCFENetworkMemberUpdate() {
        target.onCFENetworkMemberUpdate();
    }

    @Override
    public void onCFENetworkMemberUpdate(CFENetworkMember updated) {
        target.onCFENetworkMemberUpdate(updated);
    }

}
