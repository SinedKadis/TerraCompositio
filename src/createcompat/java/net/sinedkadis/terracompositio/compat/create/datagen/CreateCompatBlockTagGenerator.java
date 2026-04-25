package net.sinedkadis.terracompositio.compat.create.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.compat.create.TCCreateCompat;
import net.sinedkadis.terracompositio.compat.create.registries.CreateBlocks;
import net.sinedkadis.terracompositio.registries.TCTags;
import org.jetbrains.annotations.NotNull;

public class CreateCompatBlockTagGenerator {


    public static void addTags(BlockTagsProvider instance, HolderLookup.@NotNull Provider ignoredPProvider) {
        CreateBlocks blocks = ((TCCreateCompat) TerraCompositio.createCompat).blocks;
        if (blocks.CEDAR_GEARBOX == null) return;

        instance.tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(blocks.CEDAR_GEARBOX.get());
        instance.tag(BlockTags.MINEABLE_WITH_AXE)
                .add(blocks.CEDAR_GEARBOX.get());
        instance.tag(BlockTags.NEEDS_STONE_TOOL)
                .add(blocks.CEDAR_GEARBOX.get());

        instance.tag(TCTags.Blocks.CREATE_WRENCH_PICKUP)
                .add(blocks.CEDAR_GEARBOX.get());

    }
}
