package net.sinedkadis.terracompositio.events;

import lombok.Getter;
import net.minecraftforge.eventbus.api.Event;
import net.sinedkadis.terracompositio.api.networks.NetworkAction;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetworkMemberBE;

@Getter
public class CFENetworkEvent extends Event {
    private final CFENetworkMemberBE source;
    private final NetworkAction action;

    public CFENetworkEvent(CFENetworkMemberBE source, NetworkAction action) {
        this.source = source;
        this.action = action;
    }
}
