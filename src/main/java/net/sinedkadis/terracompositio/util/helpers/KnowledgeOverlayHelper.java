package net.sinedkadis.terracompositio.util.helpers;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;


public class KnowledgeOverlayHelper {

    public static void appendToLastComponent(List<Component> iTooltip, Component... components) {
        MutableComponent mutableComponent = (MutableComponent) iTooltip.get(iTooltip.size() - 1);

        for (Component current : components) {
            mutableComponent.append(current);
        }

        iTooltip.set(iTooltip.size() - 1, mutableComponent);
    }

}
