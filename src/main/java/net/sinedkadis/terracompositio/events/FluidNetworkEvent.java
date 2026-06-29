package net.sinedkadis.terracompositio.events;

import lombok.Getter;
import net.minecraftforge.eventbus.api.Event;
import net.sinedkadis.terracompositio.api.networks.NetworkAction;
import net.sinedkadis.terracompositio.api.networks.fluid.FluidNetworkMember;


@Getter
public class FluidNetworkEvent extends Event {
    private final FluidNetworkMember source;
    private final NetworkAction action;

    public FluidNetworkEvent(FluidNetworkMember source, NetworkAction action) {
        this.source = source;
        this.action = action;
    }
}
