package net.sinedkadis.terracompositio.util;

import net.sinedkadis.terracompositio.api.networks.cfe.ECFNetwork;
import net.sinedkadis.terracompositio.api.networks.fluid.FluidNetwork;
import net.sinedkadis.terracompositio.api.TerraCompositioAPI;
import net.sinedkadis.terracompositio.ecf.ECFNetworkHandler;
import net.sinedkadis.terracompositio.fluid.FluidNetworkHandler;

public class TerraCompositioAPIImpl implements TerraCompositioAPI {
    @Override
    public ECFNetwork getECFNetworkInstance() {
        return ECFNetworkHandler.INSTANCE;
    }

    @Override
    public FluidNetwork getFluidNetworkInstance() {
        return FluidNetworkHandler.INSTANCE;
    }

}
