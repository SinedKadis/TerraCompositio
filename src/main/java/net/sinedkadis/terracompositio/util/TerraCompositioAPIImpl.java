package net.sinedkadis.terracompositio.util;

import net.sinedkadis.terracompositio.api.networks.fluid.FluidNetwork;
import net.sinedkadis.terracompositio.api.TerraCompositioAPI;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetwork;
import net.sinedkadis.terracompositio.cfe.CFENetworkHandler;
import net.sinedkadis.terracompositio.fluid.FluidNetworkHandler;

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
