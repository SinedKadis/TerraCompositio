package net.sinedkadis.terracompositio.api;

import net.minecraft.network.chat.Component;

import java.util.List;

public interface IHaveKnowledge {

    void addToKnowledgeTooltip(List<Component> tooltip, boolean isShifting);
}
