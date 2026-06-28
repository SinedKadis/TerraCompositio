package net.sinedkadis.terracompositio.util.helpers;

import net.sinedkadis.terracompositio.api.helpers.ECFHelper;
import net.sinedkadis.terracompositio.api.networks.AnyNetworkMember;
import net.sinedkadis.terracompositio.block.entity.PathPointerBlockEntity;
import net.sinedkadis.terracompositio.ecf.PPECFMemberProxy;

public class ECFHelperInternal {
    public static boolean validPPProxy(AnyNetworkMember target) {
        if (target instanceof PPECFMemberProxy proxy) {
            if (proxy.proxy().parts.contains(PathPointerBlockEntity.PPPart.COLLECTOR)) {
                if (proxy.proxy().getOutputPos() == null) return false;
            }
            target = proxy.target();
        }
        return ECFHelper.validMember(target);
    }
}
