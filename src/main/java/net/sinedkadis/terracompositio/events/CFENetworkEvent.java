package net.sinedkadis.terracompositio.events;

import lombok.Getter;
import net.minecraftforge.eventbus.api.Event;
import net.sinedkadis.terracompositio.cfe.CFENetworkAction;
import net.sinedkadis.terracompositio.cfe.CFESource;

public class CFENetworkEvent extends Event {
    @Getter
    private final CFESource receiver;
    @Getter
    private final CFENetworkAction action;

    public CFENetworkEvent(CFESource receiver, CFENetworkAction action) {
        this.receiver = receiver;
        this.action = action;
    }
}
