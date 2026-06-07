package net.sinedkadis.terracompositio.util.helpers;

import com.mojang.datafixers.util.Pair;
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

import java.util.*;

import static net.sinedkadis.terracompositio.block.entity.PathPointerBlockEntity.setYawAndPitchFromRot;

public class CFEHelper {

    static CFETransferManager instance = new CFETransferManager();

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

    public static void tryCFETransfer(CFENetworkMember target,
                                      CFENetworkMember source,
                                      int maxTransfer,
                                      float speed,
                                      boolean noCol) {
        if (!validMember(target)) return;
        if (!validMember(source)) return;
        if (target.getEntity() instanceof CFETrashCanBlockEntity) maxTransfer = Integer.MAX_VALUE;
        ICFEHandler sourceMainHandler = source.getMainHandler();
        int taken = sourceMainHandler.takeCFE(maxTransfer, true);
        ICFEHandler targetMainHandler = target.getMainHandler();
        int added = targetMainHandler.addCFE(taken, true);

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
            if (target.getPos().closerThan(source.getPos(), 2) && !(target instanceof Entity))
                added = targetMainHandler.addCFE(added, false);
            else if (noCol) {
                added = sourceMainHandler.sendCFE(added, targetMainHandler, speed, true, false);
            } else {
                added = sourceMainHandler.sendCFE(added, target, speed, false);
            }

            sourceMainHandler.takeCFE(added, false);
        }

    }

    public static CFETransferManager transferManager() {
        return instance;
    }

    public static CFETransferBuilder newTransfer() {
        return new CFETransferBuilder();
    }

    public static class CFETransferManager {

        Map<CFENetworkMember, TransferData> transferDataMap = new HashMap<>();
        //exist to avoid ConcurrentModificationException
        List<Pair<CFENetworkMember, TransferData>> queueList = new ArrayList<>();
        boolean applying = false;

        public void addToTransfers(CFENetworkMember target, TransferData data) {
            if (applying) {
                queueList.add(new Pair<>(target, data));
                return;
            }
            if (!transferDataMap.containsKey(target)) {
                transferDataMap.put(target, data);
            } else {
                transferDataMap.computeIfPresent(target, (k, was) -> new TransferData(
                        data.source,
                        Math.max(was.maxTransfer(), data.maxTransfer), Math.max(was.speed,
                        data.speed), data.noCollision || was.noCollision
                ));
            }
        }

        public void applyTransfers() {
            applying = true;
            Set<Map.Entry<CFENetworkMember, TransferData>> entries = Set.copyOf(transferDataMap.entrySet());
            transferDataMap.clear();
            for (Map.Entry<CFENetworkMember, TransferData> entry : entries) {
                TransferData value = entry.getValue();
                tryCFETransfer(entry.getKey(), value.source(), value.maxTransfer, value.speed, value.noCollision);
            }
            applying = false;
            queueList.forEach(entry ->
                    addToTransfers(entry.getFirst(), entry.getSecond()));
            queueList.clear();
        }


    }
    public static class CFETransferBuilder {
        CFENetworkMember target = null;
        CFENetworkMember source = null;

        int maxTransfer = TCCommonConfigs.CFE_PER_BURST_TRANSFER_LIMIT.get();
        float speed = 1 / 20f;
        boolean noCollision = false;

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

        public CFETransferBuilder noCollision() {
            this.noCollision = true;
            return this;
        }

        public void build() {
            if (target != null && source != null) {
                transferManager().addToTransfers(target, new TransferData(source, maxTransfer, speed, noCollision));
            }
        }

    }

    public record TransferData(CFENetworkMember source, int maxTransfer, float speed, boolean noCollision) {

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
