package net.sinedkadis.terracompositio.api;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

import java.util.List;

public interface IHaveKnowledge {

    void collectKnowledgeData(CompoundTag data);

    void addTooltipLines(CompoundTag data, List<Component> tooltip, boolean isShifting);
}
