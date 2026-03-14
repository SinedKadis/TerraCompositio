package net.sinedkadis.terracompositio.events;

import com.mojang.datafixers.util.Pair;
import lombok.Getter;
import net.minecraftforge.eventbus.api.Event;
import net.sinedkadis.terracompositio.api.networks.NetworkAction;
import net.sinedkadis.terracompositio.block.entity.PathPointerBlockEntity;

@Getter
public class PPNetworkEvent extends Event {
    private final Pair<PathPointerBlockEntity,PathPointerBlockEntity> emitterAndCollector;
    private final NetworkAction action;

    public PPNetworkEvent(Pair<PathPointerBlockEntity,PathPointerBlockEntity> emitterAndCollector, NetworkAction action) {
        this.emitterAndCollector = emitterAndCollector;
        this.action = action;
    }
}
