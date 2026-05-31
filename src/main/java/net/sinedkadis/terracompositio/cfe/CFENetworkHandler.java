package net.sinedkadis.terracompositio.cfe;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.MinecraftForge;
import net.sinedkadis.terracompositio.api.TerraCompositioAPI;
import net.sinedkadis.terracompositio.api.networks.NetworkAction;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetwork;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetworkMember;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetworkMemberEntity;
import net.sinedkadis.terracompositio.api.networks.cfe.ICFEHandler;
import net.sinedkadis.terracompositio.block.entity.PathPointerBlockEntity;
import net.sinedkadis.terracompositio.entity.custom.FlowCedarEntEntity;
import net.sinedkadis.terracompositio.events.CFENetworkEvent;
import net.sinedkadis.terracompositio.registries.TCItems;
import net.sinedkadis.terracompositio.util.helpers.CFEHelper;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.*;

public class CFENetworkHandler implements CFENetwork {
    public static final CFENetworkHandler INSTANCE = new CFENetworkHandler();

    private final Map<Level, Set<CFENetworkMember>> cfeSources = new WeakHashMap<>();

    public void networkMemberUpdated(CFENetworkMember updated) {
        Level level = updated.getLevel();
        Set<CFENetworkMember> members = cfeSources.get(level);
        if (members == null) return;

        Queue<CFENetworkMember> queue = new ArrayDeque<>();
        // Защита от зацикливания: храним entity, а не member (прокси могут отличаться)
        Set<Object> visitedEntities = new HashSet<>();
        Set<PathPointerBlockEntity> updatedEmitters = new HashSet<>();

        queue.add(updated);
        visitedEntities.add(updated.getEntity());

        while (!queue.isEmpty()) {
            CFENetworkMember current = queue.poll();
            if (current instanceof CFEMemberProxy memberProxy) {
                PathPointerBlockEntity ppBE = memberProxy.proxy();
                if (ppBE.parts.contains(PathPointerBlockEntity.PPPart.EXTRACTOR)) {
                    TerraCompositioAPI.instance().getCFENetworkInstance().getAllCFENetworkMembers(level).stream()
                            .filter(FlowCedarEntEntity.class::isInstance)
                            .map(FlowCedarEntEntity.class::cast)
                            .filter(entEntity -> entEntity.getItemBySlot(EquipmentSlot.HEAD).is(TCItems.TECHNETIUM_CROWN.get()))
                            .filter(entEntity -> entEntity.position().closerThan(ppBE.getBlockPos().getCenter(),3))
                            .forEach(entEntity -> entEntity.scheduleMemberUpdate(memberProxy));
                }
            }
            for (CFENetworkMember member : members) {
                if (!current.getPos().closerThan(member.getPos(), Math.max(current.getRange(), member.getRange()))) continue;
                if (current.getEntity().equals(member.getEntity())) continue;
                if (current.getPriority() <= member.getPriority()) continue;

                // PathPointer EMITTER — добавляем входы в очередь
                if (member.getEntity() instanceof PathPointerBlockEntity ppBE
                        && ppBE.parts.contains(PathPointerBlockEntity.PPPart.EMITTER)
                        && updatedEmitters.add(ppBE)) { // add() возвращает false если уже есть
                    for (BlockPos inputPos : ppBE.getInputPoses()) {
                        BlockEntity be = level.getBlockEntity(inputPos);
                        if (be instanceof PathPointerBlockEntity inputEntity
                                && visitedEntities.add(inputEntity)) { // защита от петли
                            queue.add(new CFEMemberProxy(updated, inputEntity));
                        }
                    }
                }

                member.scheduleMemberUpdate(current);
            }
        }
    }

    @Override
    public void updateInRange(Level level, BlockPos origin, int range) {
        Set<CFENetworkMember> members = cfeSources.get(level);
        if (members == null) return;
        for (CFENetworkMember member : members) {
            if (member.getPos().closerThan(origin, range)) {
                member.scheduleMemberUpdate();
            }
        }
    }

    @Override
    public @Nullable CFENetworkMember getMemberAt(Level level, BlockPos blockPos) {
        Set<CFENetworkMember> members = cfeSources.get(level);
        if (members == null) return null;
        for (CFENetworkMember member : members) {
            if (member.getPos().equals(blockPos)) return member;
        }
        return null;
    }

    @Override
    public Set<CFENetworkMember> getAllCFENetworkMembers(Level level) {
        return cfeSources.getOrDefault(level, Set.of());
    }

    @Override
    public Set<CFENetworkMember> getAvailableNetworkTargets(CFENetworkMember requesterMember) {
        Level level = requesterMember.getLevel();
        Set<CFENetworkMember> members = cfeSources.get(level);
        if (members == null) return Set.of();

        Set<CFENetworkMember> toReturn = new HashSet<>();
        Queue<CFENetworkMember> queue = new ArrayDeque<>();
        // Защита от зацикливания по entity-идентичности
        Set<Object> visitedEntities = new HashSet<>();

        queue.add(requesterMember);
        visitedEntities.add(requesterMember.getEntity());

        while (!queue.isEmpty()) {
            CFENetworkMember current = queue.poll();

            List<PathPointerBlockEntity.PPPart> parts = null;
            PathPointerBlockEntity collector = null;

            if (current instanceof CFEMemberProxy proxy) {
                PathPointerBlockEntity proxyBE = proxy.proxy();
                parts = proxyBE.parts;

                // Ищем ближайший collector среди входов
                BlockPos requesterPos = requesterMember.getPos();
                BlockPos closestInput = null;
                double closestDist = Double.MAX_VALUE;
                for (BlockPos inputPos : proxyBE.getInputPoses()) {
                    double dist = requesterPos.distSqr(inputPos);
                    if (dist < closestDist) {
                        closestDist = dist;
                        closestInput = inputPos;
                    }
                }

                if (closestInput != null
                        && level.getBlockEntity(closestInput) instanceof PathPointerBlockEntity collectorBE) {
                    collector = collectorBE;
                }

                // INFUSER: добавляем живые сущности с короной рядом с proxyBE
                if (collector != null && parts.contains(PathPointerBlockEntity.PPPart.INFUSER)) {
                    PathPointerBlockEntity finalCollector = collector;
                    level.getEntitiesOfClass(
                            LivingEntity.class,
                            new AABB(proxyBE.getPos()).inflate(3),
                            e -> e.getItemBySlot(EquipmentSlot.HEAD).is(TCItems.TECHNETIUM_CROWN.get())
                    ).forEach(entity -> {
                        if (entity instanceof CFENetworkMemberEntity memberEntity
                                && visitedEntities.add(entity)) { // защита: не добавляем уже посещённых
                            toReturn.add(new CFEMemberProxy(memberEntity, finalCollector));
                        }
                    });
                }
            }

            // Фильтруем членов сети в радиусе с нужным приоритетом
            for (CFENetworkMember member : members) {
                if (!member.getPos().closerThan(current.getPos(), current.getRange())) continue;
                if (member.getPriority() <= current.getPriority()) continue;
                if (member.getEntity().equals(current.getEntity())) continue;

                // Если текущий — EMITTER прокси, перенаправляем позицию к collector
                CFENetworkMember mapped = (parts != null && collector != null
                        && parts.contains(PathPointerBlockEntity.PPPart.EMITTER))
                        ? new CFEMemberProxy(member, collector)
                        : member;

                toReturn.add(mapped);

                // COLLECTOR: идём дальше по цепочке PathPointer
                if (member.getEntity() instanceof PathPointerBlockEntity ppBE
                        && ppBE.parts.contains(PathPointerBlockEntity.PPPart.COLLECTOR)) {
                    BlockPos outputPos = ppBE.getOutputPos();
                    if (outputPos != null
                            && level.getBlockEntity(outputPos) instanceof PathPointerBlockEntity outputEntity
                            && visitedEntities.add(outputEntity)) { // защита от петли A→B→A
                        queue.add(new CFEMemberProxy(requesterMember, outputEntity));
                    }
                }
            }
        }

        // validMember фильтр — в конце, один раз
        toReturn.removeIf(m -> !CFEHelper.validMember(m));
        return toReturn;
    }

    @Override
    public boolean isIn(Level level, ICFEHandler cfeHandler) {
        Set<CFENetworkMember> members = cfeSources.get(level);
        if (members == null) return false;
        for (CFENetworkMember member : members) {
            if (member.getMainHandler().equals(cfeHandler)) return true;
        }
        return false;
    }

    @Override
    public boolean isIn(Level level, CFENetworkMember networkMember) {
        Set<CFENetworkMember> members = cfeSources.get(level);
        if (members == null) return false;
        return members.contains(networkMember);
    }

    private void remove(Level level, CFENetworkMember thing) {
        Set<CFENetworkMember> set = cfeSources.get(level);
        if (set == null) return;
        networkMemberUpdated(thing);
        set.remove(thing);
        if (set.isEmpty()) cfeSources.remove(level);
    }

    private void add(Level level, CFENetworkMember thing) {
        cfeSources.computeIfAbsent(level, k -> new HashSet<>()).add(thing);
        networkMemberUpdated(thing);
    }

    public void onNetworkEvent(CFENetworkMember source, NetworkAction action) {
        switch (action) {
            case ADD    -> add(source.getLevel(), source);
            case REMOVE -> remove(source.getLevel(), source);
            case UPDATE -> networkMemberUpdated(source);
            default     -> throw new RuntimeException("Unsupported Network action: " + action);
        }
    }
    @Override
    public void fireCFENetworkEvent(CFENetworkMember source, NetworkAction action) {
        MinecraftForge.EVENT_BUS.post(new CFENetworkEvent(source,action));
    }
}
