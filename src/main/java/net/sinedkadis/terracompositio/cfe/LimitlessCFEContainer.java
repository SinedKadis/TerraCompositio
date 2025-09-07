package net.sinedkadis.terracompositio.cfe;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetworkMember;
import net.sinedkadis.terracompositio.api.networks.cfe.ICFEHandler;

public class LimitlessCFEContainer extends CFEContainer{
    public LimitlessCFEContainer(CFENetworkMember entity) {
        super(entity);
    }

    @Override
    public int addCFE(int cfe, BlockPos source, boolean simulate) {
        if (!simulate && cfe > 0) {
            CfeQueueMember member = new CfeQueueMember(cfe, this, source, ((ServerLevel) this.getAttachedMember().getLevel()));
            cfeQueue.add(member);
        }
        return cfe;
    }

    @Override
    public int addCFE(int cfe, ICFEHandler source, boolean simulate, boolean doRender) {
        if (!simulate && cfe > 0) {
            cfeQueue.add(new CfeQueueMember(cfe, this, source, ((ServerLevel) this.getAttachedMember().getLevel()),doRender));
        }
        return cfe;
    }

    @Override
    public int getFreeSpace() {
        return Integer.MAX_VALUE;
    }

    @Override
    public int getMaxCFE() {
        return Integer.MAX_VALUE;
    }
}
