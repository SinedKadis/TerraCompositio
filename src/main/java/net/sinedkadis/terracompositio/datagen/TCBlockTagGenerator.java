package net.sinedkadis.terracompositio.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;

import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.registries.TCBlocks;
import net.sinedkadis.terracompositio.registries.TCTags;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class TCBlockTagGenerator extends BlockTagsProvider {
    public TCBlockTagGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, TerraCompositio.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider pProvider) {
        //this.tag(ModTags.Blocks.METAL_DETECTOR_VALUABLES)
        //        .add(ModBlocks.FLOW_LOG.get()).addTag(Tags.Blocks.NEEDS_NETHERITE_TOOL);

        this.tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(TCBlocks.TECHNETIUM_RAW_ORE_BLOCK.get(),
                        TCBlocks.TECHNETIUM_ORE.get(),
                        TCBlocks.TECHNETIUM_DEEPSLATE_ORE.get(),
                        TCBlocks.MATTER_INFUSER_PORT.get(),
                        TCBlocks.MATTER_INFUSER_IO.get(),
                        TCBlocks.CONSTRUCTION_DESORBER.get(),
                        TCBlocks.CULTIVATION_DESORBER.get(),
                        TCBlocks.TIME_PASSAGE_DESORBER.get(),
                        TCBlocks.PP_SENDER.get(),
                        TCBlocks.PP_EMITTER.get(),
                        TCBlocks.PP_COLLECTOR.get(),
                        TCBlocks.PP_RECEIVER.get(),
                        TCBlocks.TECHNETIUM_BLOCK.get());
        this.tag(BlockTags.MINEABLE_WITH_AXE)
                .add(TCBlocks.FLOW_CEDAR_LOG.get(),
                        TCBlocks.FLOW_CEDAR_LEAVES.get(),
                        TCBlocks.FLOW_CEDAR_PORT.get(),
                        TCBlocks.FLOW_INFUSER.get(),
                        TCBlocks.FLOW_CEDAR_WOOD.get(),
                        TCBlocks.FLOW_CEDAR_PLANKS.get(),
                        TCBlocks.STRIPPED_FLOW_CEDAR_LOG.get(),
                        TCBlocks.STRIPPED_FLOW_CEDAR_WOOD.get(),
                        TCBlocks.FLOW_CEDAR_CASING.get(),
                        TCBlocks.FLOW_CEDAR_PEDESTAL.get(),
                        TCBlocks.FLOW_CEDAR_TANK.get(),
                        TCBlocks.PP_SENDER.get(),
                        TCBlocks.PP_EMITTER.get(),
                        TCBlocks.PP_COLLECTOR.get(),
                        TCBlocks.PP_RECEIVER.get(),
                        TCBlocks.FLOW_CEDAR_ENT_STATUE.get());


        this.tag(BlockTags.NEEDS_IRON_TOOL)
                .add(TCBlocks.FLOW_CEDAR_LOG.get(),
                        TCBlocks.FLOW_CEDAR_LEAVES.get(),
                        TCBlocks.FLOW_CEDAR_PORT.get(),
                        TCBlocks.FLOW_CEDAR_WOOD.get());

        this.tag(BlockTags.NEEDS_STONE_TOOL)
                .add(TCBlocks.PP_SENDER.get(),
                        TCBlocks.PP_EMITTER.get(),
                        TCBlocks.PP_COLLECTOR.get(),
                        TCBlocks.PP_RECEIVER.get());
        this.tag(BlockTags.LOGS_THAT_BURN)
                .add(TCBlocks.FLOW_CEDAR_LOG.get(),
                        TCBlocks.FLOW_CEDAR_WOOD.get(),
                        TCBlocks.FLOW_CEDAR_PORT.get(),
                        TCBlocks.STRIPPED_FLOW_CEDAR_LOG.get(),
                        TCBlocks.STRIPPED_FLOW_CEDAR_WOOD.get(),
                        TCBlocks.FLOW_INFUSER.get(),
                        TCBlocks.FLOW_CEDAR_CASING.get());
        this.tag(BlockTags.PLANKS)
                .add(TCBlocks.FLOW_CEDAR_PLANKS.get());
        this.tag(BlockTags.LEAVES)
                .add(TCBlocks.FLOW_CEDAR_LEAVES.get());

        this.tag(BlockTags.STAIRS)
                .add(TCBlocks.FLOW_CEDAR_STAIRS.get());
        this.tag(BlockTags.DOORS)
                .add(TCBlocks.FLOW_CEDAR_DOOR.get());
        this.tag(BlockTags.TRAPDOORS)
                .add(TCBlocks.FLOW_CEDAR_TRAPDOOR.get());
        this.tag(BlockTags.BUTTONS)
                .add(TCBlocks.FLOW_CEDAR_BUTTON.get());
        this.tag(BlockTags.PRESSURE_PLATES)
                .add(TCBlocks.FLOW_CEDAR_PRESSURE_PLATE.get());
        this.tag(BlockTags.FENCES)
                .add(TCBlocks.FLOW_CEDAR_FENCE.get());
        this.tag(BlockTags.FENCE_GATES)
                .add(TCBlocks.FLOW_CEDAR_FENCE_GATE.get());
        this.tag(TCTags.Blocks.FLOW_CEDAR_LOGS)
                .add(TCBlocks.FLOW_CEDAR_LOG.get(),
                        TCBlocks.FLOW_CEDAR_WOOD.get(),
                        TCBlocks.FLOW_CEDAR_PORT.get());
        //this.tag(BlockTags.NEEDS_DIAMOND_TOOL)
        //        .add(ModBlocks.RAW_SAPPHIRE_BLOCK.get());

        //this.tag(BlockTags.NEEDS_STONE_TOOL)
        //        .add(ModBlocks.NETHER_SAPPHIRE_ORE.get());

        //this.tag(Tags.Blocks.NEEDS_NETHERITE_TOOL)
        //        .add(ModBlocks.END_STONE_SAPPHIRE_ORE.get());

        this.tag(BlockTags.ALL_HANGING_SIGNS)
                .add(TCBlocks.FLOW_CEDAR_HANGING_SIGN.get(),
                        TCBlocks.FLOW_CEDAR_WALL_HANGING_SIGN.get());
        this.tag(BlockTags.WALL_HANGING_SIGNS)
                .add(TCBlocks.FLOW_CEDAR_WALL_HANGING_SIGN.get());
        this.tag(BlockTags.CEILING_HANGING_SIGNS)
                .add(TCBlocks.FLOW_CEDAR_HANGING_SIGN.get());
        this.tag(BlockTags.ALL_SIGNS)
                .add(TCBlocks.FLOW_CEDAR_SIGN.get(),
                        TCBlocks.FLOW_CEDAR_WALL_SIGN.get());
        this.tag(BlockTags.WALL_SIGNS)
                .add(TCBlocks.FLOW_CEDAR_WALL_SIGN.get());
        this.tag(BlockTags.STANDING_SIGNS)
                .add(TCBlocks.FLOW_CEDAR_SIGN.get());
        this.tag(BlockTags.SAPLINGS)
                .add(TCBlocks.FLOW_CEDAR_SAPLING.get());
//        this.tag(TCTags.Blocks.CREATE_WRENCH_PICKUP)
//                .add(Blocks.PISTON);

    }
}
