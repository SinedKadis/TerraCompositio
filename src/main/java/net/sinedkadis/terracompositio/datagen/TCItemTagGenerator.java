package net.sinedkadis.terracompositio.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.fml.ModList;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.registries.TCBlocks;
import net.sinedkadis.terracompositio.registries.TCItems;
import net.sinedkadis.terracompositio.registries.TCTags;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class TCItemTagGenerator extends ItemTagsProvider {
    public TCItemTagGenerator(PackOutput p_275343_, CompletableFuture<HolderLookup.Provider> p_275729_,
                              CompletableFuture<TagLookup<Block>> p_275322_, @Nullable ExistingFileHelper existingFileHelper) {
        super(p_275343_, p_275729_, p_275322_, TerraCompositio.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider pProvider) {
        this.tag(ItemTags.LOGS_THAT_BURN)
                .add(TCBlocks.FLOW_CEDAR_LOG.get().asItem(),
                        TCBlocks.FLOW_CEDAR_WOOD.get().asItem(),
                        TCBlocks.FLOW_CEDAR_PORT.get().asItem(),
                        TCBlocks.STRIPPED_FLOW_CEDAR_LOG.get().asItem(),
                        TCBlocks.STRIPPED_FLOW_CEDAR_WOOD.get().asItem(),
                        TCBlocks.FLOW_CEDAR_CASING.get().asItem(),
                        TCBlocks.FLOW_INFUSER.get().asItem());
        this.tag(ItemTags.PLANKS)
                .add(TCBlocks.FLOW_CEDAR_PLANKS.get().asItem());
        this.tag(ItemTags.LEAVES)
                .add(TCBlocks.FLOW_CEDAR_LEAVES.get().asItem());
        this.tag(ItemTags.STAIRS)
                .add(TCBlocks.FLOW_CEDAR_STAIRS.get().asItem());
        this.tag(ItemTags.DOORS)
                .add(TCBlocks.FLOW_CEDAR_DOOR.get().asItem());
        this.tag(ItemTags.TRAPDOORS)
                .add(TCBlocks.FLOW_CEDAR_TRAPDOOR.get().asItem());
        this.tag(ItemTags.BUTTONS)
                .add(TCBlocks.FLOW_CEDAR_BUTTON.get().asItem());
        this.tag(ItemTags.WOODEN_PRESSURE_PLATES)
                .add(TCBlocks.FLOW_CEDAR_PRESSURE_PLATE.get().asItem());
        this.tag(ItemTags.FENCES)
                .add(TCBlocks.FLOW_CEDAR_FENCE.get().asItem());
        this.tag(ItemTags.FENCE_GATES)
                .add(TCBlocks.FLOW_CEDAR_FENCE_GATE.get().asItem());
        this.tag(TCTags.Items.FLOW_CEDAR_LOGS)
                .add(TCBlocks.FLOW_CEDAR_LOG.get().asItem(),
                        TCBlocks.FLOW_CEDAR_WOOD.get().asItem());
        this.tag(ItemTags.TRIMMABLE_ARMOR)
                .add(TCItems.FLOW_CEDAR_HELMET.get(),
                        TCItems.FLOW_CEDAR_CHESTPLATE.get(),
                        TCItems.FLOW_CEDAR_LEGGINGS.get(),
                        TCItems.FLOW_CEDAR_BOOTS.get(),
                        TCItems.FLOWING_FLOW_CEDAR_HELMET.get(),
                        TCItems.FLOWING_FLOW_CEDAR_CHESTPLATE.get(),
                        TCItems.FLOWING_FLOW_CEDAR_LEGGINGS.get(),
                        TCItems.FLOWING_FLOW_CEDAR_BOOTS.get(),
                        TCItems.TECHNETIUM_CHESTPLATE.get(),
                        TCItems.TECHNETIUM_LEGGINGS.get(),
                        TCItems.TECHNETIUM_BOOTS.get());
        this.tag(ItemTags.BOATS)
                .add(TCItems.FLOW_CEDAR_BOAT.get());
        this.tag(ItemTags.CHEST_BOATS)
                .add(TCItems.FLOW_CEDAR_CHEST_BOAT.get());
        this.tag(Tags.Items.INGOTS)
                .add(TCItems.INFUSED_IRON_INGOT.get());
        this.tag(Tags.Items.NUGGETS)
                .add(TCItems.COPPER_NUGGET.get(),
                        TCItems.INFUSED_IRON_NUGGET.get(),
                        TCItems.TECHNETIUM_NUGGET.get());
        this.tag(Tags.Items.RODS)
                .add(TCItems.INFUSED_IRON_ROD.get(),
                        TCItems.GOLD_ROD.get(),
                        TCItems.COPPER_ROD.get(),
                        TCItems.TECHNETIUM_ROD.get());
        this.tag(ItemTags.AXES)
                .add(TCItems.WRENCH_AXE.get());
        this.tag(TCTags.Items.UNSTABLE_TECHNETIUM)
                .add(TCItems.RAW_TECHNETIUM.get(),
                        TCItems.LOW_ENRICHED_TECHNETIUM.get(),
                        TCItems.HIGH_ENRICHED_TECHNETIUM.get(),
                        TCItems.MEDIUM_ENRICHED_TECHNETIUM.get(),
                        TCBlocks.TECHNETIUM_ORE.get().asItem(),
                        TCBlocks.TECHNETIUM_DEEPSLATE_ORE.get().asItem(),
                        TCBlocks.TECHNETIUM_RAW_ORE_BLOCK.get().asItem());
        this.tag(TCTags.Items.WRENCHES)
                .add(TCItems.WRENCH_TAG_HOLDER.get());
        this.tag(TCTags.Items.TORCHES)
                .add(Items.TORCH,
                        Items.REDSTONE_TORCH,
                        Items.SOUL_TORCH);
        this.tag(TCTags.Items.GOLD_RODS)
                .add(TCItems.GOLD_ROD.get());
        this.tag(TCTags.Items.CHAIN_RIDEABLE)
                .add(TCItems.WRENCH_AXE.get());

        if (ModList.get().isLoaded("create")) {
            TerraCompositio.createCompat.getDataGen().addItemTags(this, pProvider);
        }
    }
}
