package net.sinedkadis.terracompositio.events;

import lombok.Getter;
import net.minecraftforge.eventbus.api.Event;
import net.sinedkadis.terracompositio.api.networks.NetworkAction;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetworkMember;

@Getter
public class CFENetworkEvent extends Event {
    private final CFENetworkMember source;
    private final NetworkAction action;

    public CFENetworkEvent(CFENetworkMember source, NetworkAction action) {
        this.source = source;
        this.action = action;
    }
}
