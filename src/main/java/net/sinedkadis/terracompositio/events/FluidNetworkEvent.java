package net.sinedkadis.terracompositio.events;

import lombok.Getter;
import net.minecraftforge.eventbus.api.Event;
import net.sinedkadis.terracompositio.api.networks.fluid.FluidNetworkMemberBE;
import net.sinedkadis.terracompositio.api.networks.NetworkAction;


@Getter
public class FluidNetworkEvent extends Event {
    private final FluidNetworkMemberBE source;
    private final NetworkAction action;

    public FluidNetworkEvent(FluidNetworkMemberBE source, NetworkAction action) {
        this.source = source;
        this.action = action;
    }
}
