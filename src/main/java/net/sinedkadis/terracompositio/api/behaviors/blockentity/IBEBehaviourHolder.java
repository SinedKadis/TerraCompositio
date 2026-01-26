package net.sinedkadis.terracompositio.api.behaviors.blockentity;

import java.util.List;

public interface IBEBehaviourHolder {
    void addBEBehaviours(List<IBEBehaviour> behaviourList);

    List<IBEBehaviour> getBehaviours();
}
