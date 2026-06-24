package net.sinedkadis.terracompositio.ecf;

import net.minecraft.util.Mth;
import net.sinedkadis.terracompositio.api.networks.cfe.ECFNetworkMember;
import net.sinedkadis.terracompositio.ecf.burst.ECFBurstProjectileEntity;
import org.jetbrains.annotations.NotNull;

public class LimitlessECFContainer extends ECFContainer {
    public LimitlessECFContainer(ECFNetworkMember entity) {
        super(entity);
    }

    @Override
    public int sendCFE(@NotNull ECFNetworkMember target, int cfe, float speed, boolean simulate) {
        int freeSpace = target.getMainHandler().getFreeSpace();
        int added = Mth.clamp(cfe, 0, freeSpace);
        if (added < 1)
            return 0;
        if (!simulate) {
            ECFBurstProjectileEntity entity = ECFBurstProjectileEntity.sendBurst(this, target, added, speed);
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
