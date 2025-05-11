package net.sinedkadis.terracompositio.util;

import net.sinedkadis.terracompositio.api.FluidNetwork;
import net.sinedkadis.terracompositio.api.TerraCompositioAPI;
import net.sinedkadis.terracompositio.api.cfe.CFENetwork;

public class TerraCompositioAPIImpl implements TerraCompositioAPI {
    @Override
    public CFENetwork getCFENetworkInstance() {
        return CFENetworkHandler.instance;
    }

    @Override
    public FluidNetwork getFluidNetworkInstance() {
        return FluidNetworkHandler.instance;
    }

}
