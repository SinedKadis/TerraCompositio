package net.sinedkadis.terracompositio.api;

import net.sinedkadis.terracompositio.api.networks.cfe.CFENetwork;
import net.sinedkadis.terracompositio.api.networks.fluid.FluidNetwork;
import net.sinedkadis.terracompositio.util.TerraCompositioAPIImpl;

public interface TerraCompositioAPI {
    TerraCompositioAPI INSTANCE = new TerraCompositioAPIImpl();
    static TerraCompositioAPI instance(){
        return INSTANCE;
    }
    CFENetwork getCFENetworkInstance();
    FluidNetwork getFluidNetworkInstance();
}
