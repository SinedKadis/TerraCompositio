package net.sinedkadis.terracompositio.util.helpers;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.sinedkadis.terracompositio.api.networks.AnyNetworkMember;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetworkMember;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetworkMemberEntity;
import net.sinedkadis.terracompositio.api.networks.cfe.ICFEHandler;
import net.sinedkadis.terracompositio.block.entity.CFETrashCanBlockEntity;
import net.sinedkadis.terracompositio.block.entity.PathPointerBlockEntity;
import net.sinedkadis.terracompositio.cfe.PPCFEMemberProxy;
import net.sinedkadis.terracompositio.config.TCCommonConfigs;
import net.sinedkadis.terracompositio.entity.custom.CFECloudEntity;

import java.util.Objects;
import java.util.Optional;

import static net.sinedkadis.terracompositio.block.entity.PathPointerBlockEntity.setYawAndPitchFromRot;

public class CFEHelper {

    public static boolean validMember(AnyNetworkMember target) {
        if (target instanceof PPCFEMemberProxy proxy) {
            if (proxy.proxy().parts.contains(PathPointerBlockEntity.PPPart.COLLECTOR)) {
                if (proxy.proxy().getOutputPos() == null) return false;
            }
            target = proxy.target();
        }
        if (target.getEntity() instanceof BlockEntity memberBE) {
            return !memberBE.isRemoved();
        }
        if (target.getEntity() instanceof Entity memberEntity) {
            return !memberEntity.isRemoved();
        }
        return false;
    }

    public static void doCFETransfer(CFENetworkMember target,
                                     CFENetworkMember source,
                                     int maxTransfer,
                                     float speed) {
        if (!validMember(target)) return;
        if (!validMember(source)) return;
        if (target.getEntity() instanceof CFETrashCanBlockEntity) maxTransfer = Integer.MAX_VALUE;
        ICFEHandler sourceMainHandler = source.getMainHandler();
        int taken = sourceMainHandler.takeCFE(maxTransfer, true);
        ICFEHandler targetMainHandler = target.getMainHandler();
        int added = targetMainHandler.addCFE(taken, true);

        if (added > 0) {
            if (target instanceof PPCFEMemberProxy proxy && proxy.target() instanceof CFENetworkMemberEntity) {
                BlockPos pos = proxy.proxy().getOutputPos();
                PathPointerBlockEntity ppBE = ((PathPointerBlockEntity) target.getLevel().getBlockEntity(pos));
                if (ppBE != null) {
                    if (ppBE.parts.contains(PathPointerBlockEntity.PPPart.INFUSER)) {
                        setYawAndPitchFromRot(pos.getCenter().vectorTo(proxy.target().getPos().getCenter()), ppBE);
                    }
                }
            }
            if (target.getPos().closerThan(source.getPos(), 2) && !(target instanceof Entity))
                added = targetMainHandler.addCFE(added, false);
            else {
                added = sourceMainHandler.sendCFE(target, added, speed, false);
            }

            sourceMainHandler.takeCFE(added, false);
        }

    }

    public static CFETransferBuilder newTransfer() {
        return new CFETransferBuilder();
    }


    public static class CFETransferBuilder {
        CFENetworkMember target = null;
        CFENetworkMember source = null;

        int maxTransfer = TCCommonConfigs.CFE_PER_BURST_TRANSFER_LIMIT.get();
        float speed = 1 / 20f;
        boolean instant = false;


        public CFETransferBuilder targetAndSource(CFENetworkMember target, CFENetworkMember source) {
            this.target = target;
            this.source = source;
            return this;
        }

        public CFETransferBuilder maxTransfer(int maxTransfer) {
            this.maxTransfer = maxTransfer;
            return this;
        }

        public CFETransferBuilder speed(float speed) {
            this.speed = speed;
            return this;
        }

        public CFETransferBuilder instant() {
            this.instant = true;
            return this;
        }

        public void build() {
            if (target != null && source != null) {
                doCFETransfer(target, source, maxTransfer, speed);
            }
        }

    }

    public static int placeCFECloud(Level pLevel, BlockPos targetPos, int cfe) {
        Optional<CFECloudEntity> first = pLevel.getEntities(null, AABB.ofSize(targetPos.getCenter(), 1, 1, 1))
                .stream()
                .map(entity -> entity instanceof CFECloudEntity cfeCloudEntity ? cfeCloudEntity : null)
                .filter(Objects::nonNull)
                .findFirst();
        CFECloudEntity entity = first.orElseGet(() -> new CFECloudEntity(pLevel));
        int cfe1 = entity.getSyncedCFE();
        if (cfe1 == 0) {
            pLevel.addFreshEntity(entity);
            entity.setPos(targetPos.getCenter());
        }
        entity.setSyncedCFE(cfe1 + cfe);
        return cfe;
    }
}
