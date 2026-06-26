package net.sinedkadis.terracompositio.util.helpers;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.sinedkadis.terracompositio.api.networks.AnyNetworkMember;
import net.sinedkadis.terracompositio.api.networks.ecf.ECFNetworkMember;
import net.sinedkadis.terracompositio.api.networks.ecf.ECFNetworkMemberEntity;
import net.sinedkadis.terracompositio.api.networks.ecf.IECFHandler;
import net.sinedkadis.terracompositio.block.entity.ECFTrashCanBlockEntity;
import net.sinedkadis.terracompositio.block.entity.PathPointerBlockEntity;
import net.sinedkadis.terracompositio.config.TCCommonConfigs;
import net.sinedkadis.terracompositio.ecf.PPECFMemberProxy;
import net.sinedkadis.terracompositio.entity.custom.ECFCloudEntity;

import java.util.Objects;
import java.util.Optional;

import static net.sinedkadis.terracompositio.block.entity.PathPointerBlockEntity.setYawAndPitchFromRot;

public class ECFHelper {

    public static boolean validMember(AnyNetworkMember target) {
        if (target instanceof PPECFMemberProxy proxy) {
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

    public static void doECFTransfer(ECFNetworkMember target,
                                     ECFNetworkMember source,
                                     int maxTransfer,
                                     float speed) {
        if (!validMember(target)) return;
        if (!validMember(source)) return;
        if (target.getEntity() instanceof ECFTrashCanBlockEntity) maxTransfer = Integer.MAX_VALUE;
        IECFHandler sourceMainHandler = source.getMainHandler();
        int taken = sourceMainHandler.takeECF(maxTransfer, true);
        IECFHandler targetMainHandler = target.getMainHandler();
        int added = targetMainHandler.addECF(taken, true);

        if (added > 0) {
            if (target instanceof PPECFMemberProxy proxy && proxy.target() instanceof ECFNetworkMemberEntity) {
                BlockPos pos = proxy.proxy().getOutputPos();
                PathPointerBlockEntity ppBE = ((PathPointerBlockEntity) target.getLevel().getBlockEntity(pos));
                if (ppBE != null) {
                    if (ppBE.parts.contains(PathPointerBlockEntity.PPPart.INFUSER)) {
                        setYawAndPitchFromRot(pos.getCenter().vectorTo(proxy.target().getPos().getCenter()), ppBE);
                    }
                }
            }
            if (target.getPos().closerThan(source.getPos(), 2) && !(target instanceof Entity))
                added = targetMainHandler.addECF(added, false);
            else {
                added = sourceMainHandler.sendECF(target, added, speed, false);
            }

            sourceMainHandler.takeECF(added, false);
        }

    }

    public static ECFTransferBuilder newTransfer() {
        return new ECFTransferBuilder();
    }

    public static int placeECFCloud(Level pLevel, BlockPos targetPos, int cfe) {
        Optional<ECFCloudEntity> first = pLevel.getEntities(null, AABB.ofSize(targetPos.getCenter(), 1, 1, 1))
                .stream()
                .map(entity -> entity instanceof ECFCloudEntity cfeCloudEntity ? cfeCloudEntity : null)
                .filter(Objects::nonNull)
                .findFirst();
        ECFCloudEntity entity = first.orElseGet(() -> new ECFCloudEntity(pLevel));
        int cfe1 = entity.getSyncedECF();
        if (cfe1 == 0) {
            pLevel.addFreshEntity(entity);
            entity.setPos(targetPos.getCenter());
        }
        entity.setSyncedECF(cfe1 + cfe);
        return cfe;
    }

    public static class ECFTransferBuilder {
        ECFNetworkMember target = null;
        ECFNetworkMember source = null;

        int maxTransfer = TCCommonConfigs.ECF_PER_BURST_TRANSFER_LIMIT.get();
        float speed = 1 / 20f;
        boolean instant = false;


        public ECFTransferBuilder targetAndSource(ECFNetworkMember target, ECFNetworkMember source) {
            this.target = target;
            this.source = source;
            return this;
        }

        public ECFTransferBuilder maxTransfer(int maxTransfer) {
            this.maxTransfer = maxTransfer;
            return this;
        }

        public ECFTransferBuilder speed(float speed) {
            this.speed = speed;
            return this;
        }

        public ECFTransferBuilder instant() {
            this.instant = true;
            return this;
        }

        public void build() {
            if (target != null && source != null) {
                doECFTransfer(target, source, maxTransfer, speed);
            }
        }

    }
}
