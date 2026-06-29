package net.sinedkadis.terracompositio.ecf;

import net.sinedkadis.terracompositio.api.networks.ecf.ECFNetworkMember;
import net.sinedkadis.terracompositio.api.networks.ecf.IECFHandler;
import net.sinedkadis.terracompositio.block.entity.PathPointerBlockEntity;
import net.sinedkadis.terracompositio.util.IEntityInstance;

public record PPECFMemberProxy(ECFNetworkMember target, PathPointerBlockEntity proxy) implements ECFNetworkMember {

    @Override
    public IEntityInstance getEntityInstance() {
        return ((IEntityInstance) proxy);
    }

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
