package net.sinedkadis.terracompositio.api;

import net.minecraft.world.item.ItemStack;
import net.sinedkadis.terracompositio.util.KnowledgeData;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
public interface IKnowledgeData {
    KnowledgeData addText(String translationKey);

    KnowledgeData addText(String translationKey, Object... args);

    KnowledgeData addItem(ItemStack stack);

    List<KnowledgeData.Entry> entries();
}
