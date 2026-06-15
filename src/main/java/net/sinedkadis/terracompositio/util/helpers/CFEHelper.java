package net.sinedkadis.terracompositio.util.helpers;

import com.mojang.datafixers.util.Pair;
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
import net.sinedkadis.terracompositio.config.TCServerConfigs;
import net.sinedkadis.terracompositio.entity.custom.CFECloudEntity;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

import static net.sinedkadis.terracompositio.block.entity.PathPointerBlockEntity.setYawAndPitchFromRot;

public class CFEHelper {

    static CFETransferManager instance = new CFETransferManager();

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

    public static CFETransferManager transferManager() {
        return instance;
    }

    public static CFETransferBuilder newTransfer() {
        return new CFETransferBuilder();
    }

    public static class CFETransferManager {
        // Ключ — пара (source, target), значение — накопленные данные
        private final ConcurrentHashMap<TransferKey, TransferData> transferDataMap = new ConcurrentHashMap<>();
        // Очередь для запросов, пришедших во время копирования entries
        private final ConcurrentLinkedQueue<Pair<TransferKey, TransferData>> queueList = new ConcurrentLinkedQueue<>();
        // Список записей для равномерного размазывания вызовов
        private final List<Map.Entry<TransferKey, TransferData>> entries = new CopyOnWriteArrayList<>();

        private volatile boolean applying = false;

        public void addToTransfers(CFENetworkMember target, CFENetworkMember source, TransferData data, boolean instant) {
            TransferKey key = new TransferKey(source, target);
            if (instant) {
                doTransfer(key, data);
                return;
            }
            if (applying) {
                queueList.add(new Pair<>(key, data));
                return;
            }
            mergeIntoMap(key, data);
        }

        private void mergeIntoMap(TransferKey key, TransferData data) {
            transferDataMap.merge(key, data, (was, incoming) -> new TransferData(
                    was.maxTransfer() + incoming.maxTransfer(),
                    Math.max(was.speed(), incoming.speed())
            ));
        }

        public void applyTransfers(long gameTime) {
            int frequency = TCServerConfigs.CFE_SEND_FREQUENCY.get();
            int time = Math.toIntExact(gameTime % frequency);

            if (time == 0) {
                applying = true;
                // Копируем все записи из карты в список для размазывания
                entries.addAll(transferDataMap.entrySet().stream()
                        .map(e -> Map.entry(e.getKey(), e.getValue()))
                        .toList());
                transferDataMap.clear();
                applying = false;

                // Обрабатываем накопившуюся очередь
                Pair<TransferKey, TransferData> queued;
                while ((queued = queueList.poll()) != null) {
                    mergeIntoMap(queued.getFirst(), queued.getSecond());
                }
            }

            if (entries.isEmpty()) return;

            int size = entries.size();
            List<Map.Entry<TransferKey, TransferData>> toApply = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                // Тик, на котором должна обработаться запись i
                int scheduledTick = (i * frequency) / size;
                if (scheduledTick == time) {
                    toApply.add(entries.get(i));
                }
            }

            entries.removeAll(toApply);

            for (Map.Entry<TransferKey, TransferData> entry : toApply) {
                doTransfer(entry.getKey(), entry.getValue());
            }
        }

        private void doTransfer(TransferKey key, TransferData value) {
            doCFETransfer(key.target(), key.source(), value.maxTransfer(), value.speed());
        }

        public record TransferKey(CFENetworkMember source, CFENetworkMember target) {
        }
    }

    public record TransferData(int maxTransfer, float speed) {
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
                transferManager().addToTransfers(target, source, new TransferData(maxTransfer, speed), instant);
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


    public static class CFESpawnQueue {
        private static final ConcurrentLinkedQueue<Pair<Level, Entity>> pending = new ConcurrentLinkedQueue<>();

        public static void schedule(Level level, Entity entity) {
            pending.add(Pair.of(level, entity));
        }

        public static void flush() {
            Pair<Level, Entity> entry;
            while ((entry = pending.poll()) != null) {
                entry.getFirst().addFreshEntity(entry.getSecond());
            }
        }
    }
}
