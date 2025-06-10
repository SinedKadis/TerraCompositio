package net.sinedkadis.terracompositio.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.sinedkadis.terracompositio.datagen.loot.TCBlockLootTables;

import java.util.List;
import java.util.Set;

public class TCLootTableProvider {
    public static LootTableProvider create(PackOutput output) {
        return new LootTableProvider(output, Set.of(), List.of(
                new LootTableProvider.SubProviderEntry(TCBlockLootTables::new, LootContextParamSets.BLOCK)
        ));
    }
}
