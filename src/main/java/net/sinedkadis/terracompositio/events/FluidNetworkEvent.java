package net.sinedkadis.terracompositio.events;

import lombok.Getter;
import net.minecraftforge.eventbus.api.Event;
import net.sinedkadis.terracompositio.api.FluidSource;
import net.sinedkadis.terracompositio.api.cfe.NetworkAction;


@Getter
public class FluidNetworkEvent extends Event {
    private final FluidSource source;
    private final NetworkAction action;

    public FluidNetworkEvent(FluidSource source, NetworkAction action) {
        this.source = source;
        this.action = action;
    }
}
