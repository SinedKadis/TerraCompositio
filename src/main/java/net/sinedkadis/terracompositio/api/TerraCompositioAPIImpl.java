package net.sinedkadis.terracompositio.api;

import net.minecraftforge.common.MinecraftForge;
import net.sinedkadis.terracompositio.api.cfe.CFENetwork;
import net.sinedkadis.terracompositio.api.cfe.CFENetworkAction;
import net.sinedkadis.terracompositio.api.cfe.CFENetworkHandler;
import net.sinedkadis.terracompositio.api.cfe.CFESource;
import net.sinedkadis.terracompositio.events.CFENetworkEvent;

public class TerraCompositioAPIImpl implements TerraCompositioAPI{
    @Override
    public CFENetwork getCFENetworkInstance() {
        return CFENetworkHandler.instance;
    }

    @Override
    public void fireCFENetworkEvent(CFESource source, CFENetworkAction action) {
        MinecraftForge.EVENT_BUS.post(new CFENetworkEvent(source, action));
    }
}
