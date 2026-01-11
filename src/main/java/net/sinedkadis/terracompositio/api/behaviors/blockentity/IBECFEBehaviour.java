package net.sinedkadis.terracompositio.api.behaviors.blockentity;

import net.sinedkadis.terracompositio.api.networks.cfe.CFENetworkMemberBE;
import net.sinedkadis.terracompositio.api.networks.cfe.ICFEHandler;

public interface IBECFEBehaviour extends IBEBehaviour, CFENetworkMemberBE {
    ICFEHandler getCfeHandler();
}
