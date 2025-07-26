package net.sinedkadis.terracompositio.cfe;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetworkMemberEntity;
import net.sinedkadis.terracompositio.util.TCUtil;
import org.apache.commons.lang3.tuple.MutablePair;

public class LimitlessCFEContainer extends CFEContainer{
    public LimitlessCFEContainer(BlockEntity entity) {
        super(entity);
    }

    @Override
    public int addCFE(int cfe, BlockPos sourcePos, boolean simulate) {
        if (!simulate && cfe > 0) {
            if (blockEntity != null)
                cfeQueue.add(new MutablePair<>(cfe, Math.sqrt(TCUtil.distSqr(sourcePos, this.blockEntity.getBlockPos()))));
            else if (entity != null)
                cfeQueue.add(new MutablePair<>(cfe, Math.sqrt(TCUtil.distSqr(sourcePos, targetOffset.apply(((CFENetworkMemberEntity) this.entity).getBlockPos())))));
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
