package net.sinedkadis.terracompositio.util.helpers;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.sinedkadis.terracompositio.api.networks.AnyNetworkMember;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetworkMember;
import net.sinedkadis.terracompositio.api.networks.cfe.ICFEHandler;
import net.sinedkadis.terracompositio.block.entity.CFETrashCanBlockEntity;
import net.sinedkadis.terracompositio.block.entity.PathPointerBlockEntity;
import net.sinedkadis.terracompositio.cfe.CFEMemberProxy;
import net.sinedkadis.terracompositio.config.TCCommonConfigs;
import net.sinedkadis.terracompositio.entity.custom.CFECloudEntity;

import java.util.Objects;
import java.util.Optional;

import static net.sinedkadis.terracompositio.block.entity.PathPointerBlockEntity.setYawAndPitchFromRot;

public class CFEHelper {

    public static void tryCFETransfer(ICFEHandler target,
                                      ICFEHandler source,
                                      int maxTransfer,
                                      float speed,
                                      boolean noCol) {
        int taken = source.takeCFE(maxTransfer, true);
        int added = source.sendCFE(taken, target, speed, noCol, true);
        if (added <= taken) {
            if (target.getPos().closerThan(source.getPos(), 2) && !(target.getAttachedMember() instanceof Entity))
                added = target.addCFE(added, false);
            else
                added = source.sendCFE(added, target, speed, noCol, false);
            source.takeCFE(added, false);
        }
    }

    public static void tryCFETransfer(CFENetworkMember target, CFENetworkMember source, int maxTransfer, float speed) {
        if (!validMember(target)) return;
        if (!validMember(source)) return;
        if (target.getEntity() instanceof CFETrashCanBlockEntity) maxTransfer = Integer.MAX_VALUE;
        int taken = source.getMainHandler().takeCFE(maxTransfer, true);
        int added = target.getMainHandler().addCFE(taken, true);

        if (added <= taken) {
            if (added > 0 && target instanceof CFEMemberProxy proxy) {
                BlockPos pos = proxy.proxy().getOutputPos();
                PathPointerBlockEntity ppBE = ((PathPointerBlockEntity) target.getLevel().getBlockEntity(pos));
                if (ppBE != null) {
                    if (ppBE.parts.contains(PathPointerBlockEntity.PPPart.INFUSER)) {
                        setYawAndPitchFromRot(pos.getCenter().vectorTo(proxy.target().getPos().getCenter()), ppBE);
                    }
                }
            }
            added = source.getMainHandler().sendCFE(added, target, speed, false);
            source.getMainHandler().takeCFE(added, false);
        }

    }

    public static boolean validMember(AnyNetworkMember target) {
        if (target instanceof CFEMemberProxy proxy) {
            if (proxy.proxy().parts.contains(PathPointerBlockEntity.PPPart.COLLECTOR)) {
                if (proxy.proxy().getOutputPos() == null) return false;
            }
        }
        if (target.getEntity() instanceof BlockEntity memberBE) {
            return !memberBE.isRemoved();
        }
        if (target.getEntity() instanceof Entity memberEntity) {
            return !memberEntity.isRemoved();
        }
        return false;
    }

    public static CFETransferBuilder createTransfer() {
        return new CFETransferBuilder();
    }

    public static class CFETransferBuilder {
        CFENetworkMember target = null;
        CFENetworkMember source = null;
        ICFEHandler target1 = null;
        ICFEHandler source1 = null;
        int maxTransfer = TCCommonConfigs.CFE_PER_BURST_TRANSFER_LIMIT.get();
        float speed = 1 / 20f;
        boolean noCollision = false;

        public CFETransferBuilder fromMembers(CFENetworkMember target, CFENetworkMember source) {
            this.target = target;
            this.source = source;
            return this;
        }

        public CFETransferBuilder fromHandlers(ICFEHandler target, ICFEHandler source) {
            this.target1 = target;
            this.source1 = source;
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

        public CFETransferBuilder noCollision() {
            this.noCollision = true;
            return this;
        }

        public void build() {
            if (target != null) {
                tryCFETransfer(target, source, maxTransfer, speed);
            } else if (target1 != null) {
                tryCFETransfer(target1, source1, maxTransfer, speed, noCollision);
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
