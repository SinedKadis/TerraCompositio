package net.sinedkadis.terracompositio.cfe;

import net.minecraft.util.Mth;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetworkMember;
import net.sinedkadis.terracompositio.cfe.burst.CFEBurstProjectileEntity;
import org.jetbrains.annotations.NotNull;

public class LimitlessCFEContainer extends CFEContainer{
    public LimitlessCFEContainer(CFENetworkMember entity) {
        super(entity);
    }

    @Override
    public int sendCFE(@NotNull CFENetworkMember target, int cfe, float speed, boolean simulate) {
        int freeSpace = target.getMainHandler().getFreeSpace();
        int added = Mth.clamp(cfe, 0, freeSpace);
        if (added < 1)
            return 0;
        if (!simulate) {
            CFEBurstProjectileEntity entity = CFEBurstProjectileEntity.sendBurst(this, target, added, speed);
            if (entity != null) {
                target.getMainHandler().addToQueue(added);
            }
        }
        return added;
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
