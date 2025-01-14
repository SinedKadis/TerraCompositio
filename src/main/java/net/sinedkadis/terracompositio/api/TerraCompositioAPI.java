package net.sinedkadis.terracompositio.api;

import net.sinedkadis.terracompositio.api.cfe.CFENetwork;
import net.sinedkadis.terracompositio.api.cfe.CFENetworkAction;
import net.sinedkadis.terracompositio.api.cfe.CFESource;

public interface TerraCompositioAPI {
    TerraCompositioAPI INSTANCE = new TerraCompositioAPIImpl();
    static TerraCompositioAPI instance(){
        return INSTANCE;
    }
    CFENetwork getCFENetworkInstance();
    void fireCFENetworkEvent(CFESource source, CFENetworkAction action);
}
