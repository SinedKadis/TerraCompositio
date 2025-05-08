package net.sinedkadis.terracompositio.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.registries.ModBlocks;
import net.sinedkadis.terracompositio.registries.ModItems;
import net.sinedkadis.terracompositio.registries.ModTags;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModItemTagGenerator extends ItemTagsProvider {
    public ModItemTagGenerator(PackOutput p_275343_, CompletableFuture<HolderLookup.Provider> p_275729_,
                               CompletableFuture<TagLookup<Block>> p_275322_, @Nullable ExistingFileHelper existingFileHelper) {
        super(p_275343_, p_275729_, p_275322_, TerraCompositio.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider pProvider) {
        this.tag(ItemTags.LOGS_THAT_BURN)
                .add(ModBlocks.FLOW_CEDAR_LOG.get().asItem(),
                        ModBlocks.FLOW_CEDAR_WOOD.get().asItem(),
                        ModBlocks.FLOW_CEDAR_PORT.get().asItem(),
                        ModBlocks.STRIPPED_FLOW_CEDAR_LOG.get().asItem(),
                        ModBlocks.STRIPPED_FLOW_CEDAR_WOOD.get().asItem(),
                        ModBlocks.FLOW_CEDAR_CASING.get().asItem(),
                        ModBlocks.FLOW_INFUSER.get().asItem());
        this.tag(ItemTags.PLANKS)
                .add(ModBlocks.FLOW_CEDAR_PLANKS.get().asItem());
        this.tag(ItemTags.LEAVES)
                .add(ModBlocks.FLOW_CEDAR_LEAVES.get().asItem());
        this.tag(ItemTags.STAIRS)
                .add(ModBlocks.FLOW_CEDAR_STAIRS.get().asItem());
        this.tag(ItemTags.DOORS)
                .add(ModBlocks.FLOW_CEDAR_DOOR.get().asItem());
        this.tag(ItemTags.TRAPDOORS)
                .add(ModBlocks.FLOW_CEDAR_TRAPDOOR.get().asItem());
        this.tag(ItemTags.BUTTONS)
                .add(ModBlocks.FLOW_CEDAR_BUTTON.get().asItem());
        this.tag(ItemTags.WOODEN_PRESSURE_PLATES)
                .add(ModBlocks.FLOW_CEDAR_PRESSURE_PLATE.get().asItem());
        this.tag(ItemTags.FENCES)
                .add(ModBlocks.FLOW_CEDAR_FENCE.get().asItem());
        this.tag(ItemTags.FENCE_GATES)
                .add(ModBlocks.FLOW_CEDAR_FENCE_GATE.get().asItem());
        this.tag(ModTags.Items.FLOW_CEDAR_LOGS)
                .add(ModBlocks.FLOW_CEDAR_LOG.get().asItem(),
                        ModBlocks.FLOW_CEDAR_WOOD.get().asItem());
        this.tag(ItemTags.TRIMMABLE_ARMOR)
                .add(ModItems.FLOW_CEDAR_HELMET.get(),
                        ModItems.FLOW_CEDAR_CHESTPLATE.get(),
                        ModItems.FLOW_CEDAR_LEGGINGS.get(),
                        ModItems.FLOW_CEDAR_BOOTS.get(),
                        ModItems.FLOWING_FLOW_CEDAR_HELMET.get(),
                        ModItems.FLOWING_FLOW_CEDAR_CHESTPLATE.get(),
                        ModItems.FLOWING_FLOW_CEDAR_LEGGINGS.get(),
                        ModItems.FLOWING_FLOW_CEDAR_BOOTS.get());
        this.tag(ItemTags.BOATS)
                .add(ModItems.FLOW_CEDAR_BOAT.get());
        this.tag(ItemTags.CHEST_BOATS)
                .add(ModItems.FLOW_CEDAR_CHEST_BOAT.get());
        this.tag(Tags.Items.INGOTS)
                .add(ModItems.INFUSED_IRON_INGOT.get());
        this.tag(Tags.Items.NUGGETS)
                .add(ModItems.COPPER_NUGGET.get());
        this.tag(Tags.Items.RODS)
                .add(ModItems.INFUSED_IRON_ROD.get(),
                        ModItems.GOLD_ROD.get(),
                        ModItems.COPPER_ROD.get());
        this.tag(ItemTags.AXES)
                .add(ModItems.WRENCH_AXE.get());
        this.tag(ModTags.Items.UNSTABLE_TECHNETIUM)
                .add(ModItems.RAW_TECHNETIUM.get(),
                        ModItems.LOW_ENRICHED_TECHNETIUM.get(),
                        ModItems.HIGH_ENRICHED_TECHNETIUM.get(),
                        ModItems.MEDIUM_ENRICHED_TECHNETIUM.get(),
                        ModBlocks.TECHNETIUM_ORE.get().asItem(),
                        ModBlocks.TECHNETIUM_DEEPSLATE_ORE.get().asItem(),
                        ModBlocks.TECHNETIUM_RAW_ORE_BLOCK.get().asItem());
    }
}
