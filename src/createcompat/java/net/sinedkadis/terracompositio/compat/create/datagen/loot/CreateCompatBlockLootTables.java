package net.sinedkadis.terracompositio.compat.create.datagen.loot;


import net.minecraft.world.level.block.Block;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.compat.create.TCCreateCompat;
import net.sinedkadis.terracompositio.compat.create.registries.CreateBlocks;

import java.util.Set;

public class CreateCompatBlockLootTables {

    public static void dropSelf(Set<Block> blocks) {
        CreateBlocks createBlocks = ((TCCreateCompat) TerraCompositio.createCompat).blocks;
        if (createBlocks.CEDAR_GEARBOX == null) return;
        blocks.add(createBlocks.CEDAR_GEARBOX.get());
    }

}
