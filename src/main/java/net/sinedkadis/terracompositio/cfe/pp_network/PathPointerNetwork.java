package net.sinedkadis.terracompositio.cfe.pp_network;

import com.mojang.datafixers.util.Pair;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.MinecraftForge;
import net.sinedkadis.terracompositio.api.TerraCompositioAPI;
import net.sinedkadis.terracompositio.api.networks.NetworkAction;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetworkMember;
import net.sinedkadis.terracompositio.api.networks.cfe.ICFEHandler;
import net.sinedkadis.terracompositio.block.entity.PathPointerBlockEntity;
import net.sinedkadis.terracompositio.events.PPNetworkEvent;
import net.sinedkadis.terracompositio.util.TCUtil;

import java.util.*;
import java.util.stream.Collectors;

public enum PathPointerNetwork {
    INSTANCE;

    //emitter and collectors
    @Getter
    private final Map<PathPointerBlockEntity,Set<PathPointerBlockEntity>> members = new HashMap<>();

    public void firePPNetworkEvent(Pair<PathPointerBlockEntity,PathPointerBlockEntity> outputAndInput, NetworkAction action) {
        MinecraftForge.EVENT_BUS.post(new PPNetworkEvent(outputAndInput,action));
    }

    public void onNetworkEvent(Pair<PathPointerBlockEntity,PathPointerBlockEntity> outputAndInput, NetworkAction action) {
        if (action.equals(NetworkAction.ADD)){
            add(outputAndInput);
        } else if (action.equals(NetworkAction.REMOVE)){
            remove(outputAndInput);
        } else throw new RuntimeException("Unsupported Network action: "+ action);
    }

    //Adds collector from pair to emitter from pair, and emitter, if it is not added yet
    private void add(Pair<PathPointerBlockEntity,PathPointerBlockEntity> emitterAndCollector) {
        PathPointerBlockEntity emitter = emitterAndCollector.getFirst();
        if (!emitter.parts.contains(PathPointerBlockEntity.PPPart.EMITTER)) throw new RuntimeException("Emitter is not emitter: " + emitter);
        members.compute(emitter,(emitter1,set) ->{
            Set<PathPointerBlockEntity> collectors = new HashSet<>();
            PathPointerBlockEntity collector = emitterAndCollector.getSecond();
            if (!emitter.parts.contains(PathPointerBlockEntity.PPPart.COLLECTOR)) throw new RuntimeException("Collector is not collector: " + collector);
            collectors.add(collector);
            collector.setOutputPos(emitter.getBlockPos());
            return collectors;
        });
        clearRemovedMembers(emitter);
    }


    private void clearRemovedMembers(PathPointerBlockEntity emitter) {
        members.computeIfPresent(emitter,(emitter1, collectors) ->
                collectors.stream().filter(TCUtil::validMember).collect(Collectors.toSet()));
    }

    //Removes collector from pair from emitter from pair, and emitter, if it has no collectors
    private void remove(Pair<PathPointerBlockEntity,PathPointerBlockEntity> emitterAndCollector) {
        PathPointerBlockEntity emitter = emitterAndCollector.getFirst();
        if (!members.containsKey(emitter)) {
            return;
        }
        Set<PathPointerBlockEntity> set = members.get(emitter);
        PathPointerBlockEntity collector = emitterAndCollector.getSecond();
        set.remove(collector);
        collector.setOutputPos(null);
        if (set.isEmpty()) {
            members.remove(emitter);
        } else {
            members.put(emitter,set);
        }
    }

    public void updateMembersAroundCollectors(PathPointerBlockEntity emitter,CFENetworkMember target) {
        members.getOrDefault(emitter,Set.of()).forEach( collector ->
                TerraCompositioAPI.instance().getCFENetworkInstance()
                        .fireCFENetworkEvent(new CFEMemberProxy(target,collector), NetworkAction.UPDATE));
    }



    public Set<CFENetworkMember> getAvailableTargetsToSend(PathPointerBlockEntity collector, CFENetworkMember requesterMember) {
        BlockPos emitterPos = collector.getOutputPos();
        if (emitterPos == null) return Set.of();

        Level level = collector.getLevel();
        if (level == null) return Set.of();

        BlockEntity emitterBE = level.getBlockEntity(emitterPos);
        if (!(emitterBE instanceof PathPointerBlockEntity emitter)) return Set.of();

        return TerraCompositioAPI.instance().getCFENetworkInstance()
                //get targets for requested member but with overwrote position of emitter
                .getAvailableNetworkTargets(new CFEMemberProxy(requesterMember, emitter)).stream()
                //return targets with overwrote position of collector
                .map(member -> new CFEMemberProxy(member,collector))
                .collect(Collectors.toSet());
    }

    public static class CFEMemberProxy implements CFENetworkMember {

        private final CFENetworkMember target;
        private final PathPointerBlockEntity proxy;

        public CFEMemberProxy(CFENetworkMember target, PathPointerBlockEntity proxy) {
            this.target = target;
            this.proxy = proxy;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            CFEMemberProxy that = (CFEMemberProxy) o;
            return Objects.equals(target, that.target) && Objects.equals(proxy, that.proxy);
        }

        @Override
        public int hashCode() {
            return Objects.hash(target, proxy);
        }

        @Override
        public int getRange() {
            return target.getRange();
        }

        @Override
        public int getPriority() {
            return target.getPriority();
        }

        @Override
        public ICFEHandler getMainHandler() {
            return target.getMainHandler();
        }

        @Override
        public <T> T getEntity() {
            return target.getEntity();
        }

        @Override
        public Level getLevel() {
            return proxy.getLevel();
        }

        @Override
        public BlockPos getPos() {
            return proxy.getBlockPos();
        }

        @Override
        public void updateIfScheduled() {
            target.updateIfScheduled();
        }

        @Override
        public void scheduleMemberUpdate() {
            target.scheduleMemberUpdate();
        }

        @Override
        public void scheduleMemberUpdate(CFENetworkMember updated) {
            target.scheduleMemberUpdate(updated);
        }

        @Override
        public void onCFENetworkMemberUpdate() {
            target.onCFENetworkMemberUpdate();
        }

        @Override
        public void onCFENetworkMemberUpdate(CFENetworkMember updated) {
            target.onCFENetworkMemberUpdate(updated);
        }

    }
}
