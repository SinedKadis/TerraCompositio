package net.sinedkadis.terracompositio.api;

import net.minecraft.network.chat.Component;
import net.sinedkadis.terracompositio.util.KnowledgeData;

import java.util.List;

public interface IHaveKnowledge {

    void collectKnowledgeData(KnowledgeData data);


    void addTooltipLines(KnowledgeData data, List<Component> tooltip, boolean isShifting);
}
