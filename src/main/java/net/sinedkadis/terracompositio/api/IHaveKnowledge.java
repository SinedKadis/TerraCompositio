package net.sinedkadis.terracompositio.api;

import net.minecraft.network.chat.Component;

import java.util.List;

public interface IHaveKnowledge {

    void collectKnowledgeData(IKnowledgeData data);

    void addTooltipLines(IKnowledgeData data, List<Component> tooltip, boolean isShifting);
}
