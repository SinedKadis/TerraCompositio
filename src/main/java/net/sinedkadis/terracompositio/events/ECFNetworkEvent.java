package net.sinedkadis.terracompositio.events;

import lombok.Getter;
import net.minecraftforge.eventbus.api.Event;
import net.sinedkadis.terracompositio.api.networks.NetworkAction;
import net.sinedkadis.terracompositio.api.networks.ecf.ECFNetworkMember;

@Getter
public class ECFNetworkEvent extends Event {
    private final ECFNetworkMember source;
    private final NetworkAction action;

    public ECFNetworkEvent(ECFNetworkMember source, NetworkAction action) {
        this.source = source;
        this.action = action;
    }
}
