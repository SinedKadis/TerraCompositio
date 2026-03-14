package net.sinedkadis.terracompositio.cfe.pp_network;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.MinecraftForge;
import net.sinedkadis.terracompositio.api.TerraCompositioAPI;
import net.sinedkadis.terracompositio.api.networks.NetworkAction;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetworkMember;
import net.sinedkadis.terracompositio.api.networks.cfe.ICFEHandler;
import net.sinedkadis.terracompositio.block.behaviours.pp.CollectorBehaviour;
import net.sinedkadis.terracompositio.block.entity.PathPointerBlockEntity;
import net.sinedkadis.terracompositio.events.PPNetworkEvent;

import java.util.*;

public enum PathPointerNetwork {
    INSTANCE;

    //emitter and collectors
    private final Map<PathPointerBlockEntity,Set<PathPointerBlockEntity>> members = new HashMap<>();

    public void firePPNetworkEvent(Pair<PathPointerBlockEntity,PathPointerBlockEntity> emitterAndCollector, NetworkAction action) {
        MinecraftForge.EVENT_BUS.post(new PPNetworkEvent(emitterAndCollector,action));
    }

    public void onNetworkEvent(Pair<PathPointerBlockEntity,PathPointerBlockEntity> emitterAndCollector, NetworkAction action) {
        if (action.equals(NetworkAction.ADD)){
            addCollector(emitterAndCollector);
        } else if (action.equals(NetworkAction.REMOVE)){
            remove(emitterAndCollector);
        } else throw new RuntimeException("Unsupported Network action: "+ action);
    }

    //Adds collector from pair to emitter from pair, and emitter, if it is not added yet
    private void addCollector(Pair<PathPointerBlockEntity,PathPointerBlockEntity> emitterAndCollector) {
        PathPointerBlockEntity emitter = emitterAndCollector.getFirst();
        members.computeIfAbsent(emitter,(emitter1) ->{
            Set<PathPointerBlockEntity> collectors = new HashSet<>();
            PathPointerBlockEntity collector = emitterAndCollector.getSecond();
            collectors.add(collector);
            CollectorBehaviour.setEmitter(collector,emitter.getBlockPos());
            return collectors;
        });
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
        CollectorBehaviour.setEmitter(collector,null);
        if (set.isEmpty()) {
            members.remove(emitter);
        } else {
            members.put(emitter,set);
        }
    }

    public void updateMembersAroundCollectors(PathPointerBlockEntity emitter,CFENetworkMember target) {
        members.get(emitter).forEach( collector ->
                TerraCompositioAPI.instance().getCFENetworkInstance()
                        .fireCFENetworkEvent(new CFEMemberProxy(target,collector), NetworkAction.UPDATE));
    }



    public Set<CFENetworkMember> getAvailableTargetsToSend(PathPointerBlockEntity collector, CFENetworkMember cfeNetworkMember) {
        BlockPos emitterPos = CollectorBehaviour.getEmitter(collector);
        if (emitterPos == null) return Set.of();

        Level level = collector.getLevel();
        if (level == null) return Set.of();

        BlockEntity emitterBE = level.getBlockEntity(emitterPos);
        if (!(emitterBE instanceof PathPointerBlockEntity emitter)) return Set.of();

        return TerraCompositioAPI.instance().getCFENetworkInstance()
                .getAvailableNetworkTargets(new CFEMemberProxy(cfeNetworkMember, emitter));
    }

    static class CFEMemberProxy implements CFENetworkMember {

        private final CFENetworkMember target;
        private final PathPointerBlockEntity collector;

        public CFEMemberProxy(CFENetworkMember target, PathPointerBlockEntity collector) {
            this.target = target;
            this.collector = collector;
        }

        @Override
        public int getRange() {
            return target.getRange();
        }

        @Override
        public int getPriority() {
            return target.getRange();
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
            return collector.getLevel();
        }

        @Override
        public BlockPos getPos() {
            return collector.getBlockPos();
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
