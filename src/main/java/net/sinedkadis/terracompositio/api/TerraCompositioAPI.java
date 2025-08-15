package net.sinedkadis.terracompositio.api;

import net.sinedkadis.terracompositio.api.dummies.DummyCFENetwork;
import net.sinedkadis.terracompositio.api.dummies.DummyFluidNetwork;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetwork;
import net.sinedkadis.terracompositio.api.networks.fluid.FluidNetwork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface TerraCompositioAPI {
    String MOD_ID = "terracompositio";
    Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    TerraCompositioAPI INSTANCE = ServiceUtil.findService(TerraCompositioAPI.class, () -> new TerraCompositioAPI() {});
    static TerraCompositioAPI instance(){
        return INSTANCE;
    }
    default CFENetwork getCFENetworkInstance(){
        return DummyCFENetwork.instance;
    }
    default FluidNetwork getFluidNetworkInstance(){
        return DummyFluidNetwork.instance;
    }
}
