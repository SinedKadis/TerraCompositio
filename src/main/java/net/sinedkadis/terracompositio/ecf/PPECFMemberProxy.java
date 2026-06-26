package net.sinedkadis.terracompositio.ecf;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.sinedkadis.terracompositio.api.networks.ecf.ECFNetworkMember;
import net.sinedkadis.terracompositio.api.networks.ecf.IECFHandler;
import net.sinedkadis.terracompositio.block.entity.PathPointerBlockEntity;

public record PPECFMemberProxy(ECFNetworkMember target, PathPointerBlockEntity proxy) implements ECFNetworkMember {

    @Override
    public int getRange() {
        return target.getRange();
    }

    @Override
    public int getPriority() {
        return target.getPriority();
    }

    @Override
    public IECFHandler getMainHandler() {
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
    public void scheduleMemberUpdate(ECFNetworkMember updated) {
        target.scheduleMemberUpdate(updated);
    }

    @Override
    public void onECFNetworkMemberUpdate() {
        target.onECFNetworkMemberUpdate();
    }

    @Override
    public void onECFNetworkMemberUpdate(ECFNetworkMember updated) {
        target.onECFNetworkMemberUpdate(updated);
    }

}
