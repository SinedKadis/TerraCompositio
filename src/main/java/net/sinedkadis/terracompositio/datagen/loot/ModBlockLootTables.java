package net.sinedkadis.terracompositio.datagen.loot;


import net.minecraft.data.loot.BlockLootSubProvider;

import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;

import net.minecraft.world.item.enchantment.Enchantments;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraftforge.registries.RegistryObject;
import net.sinedkadis.terracompositio.registries.ModBlocks;
import net.sinedkadis.terracompositio.registries.ModItems;
import org.jetbrains.annotations.NotNull;


import java.util.Set;

public class ModBlockLootTables extends BlockLootSubProvider {
    public ModBlockLootTables() {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags());
    }

    @Override
    protected void generate() {
        this.dropSelf(ModBlocks.FLOW_CEDAR_LOG.get());
        this.dropSelf(ModBlocks.FLOW_CEDAR_WOOD.get());
        this.dropSelf(ModBlocks.FLOW_CEDAR_PLANKS.get());
        this.dropSelf(ModBlocks.STRIPPED_FLOW_CEDAR_LOG.get());
        this.dropSelf(ModBlocks.STRIPPED_FLOW_CEDAR_WOOD.get());
        this.dropSelf(ModBlocks.FLOW_PORT.get());
        this.dropSelf(ModBlocks.CREATIVE_CFE_SOURCE.get());

        this.dropSelf(ModBlocks.FLOW_CEDAR_STAIRS.get());
        this.dropSelf(ModBlocks.FLOW_CEDAR_BUTTON.get());
        this.dropSelf(ModBlocks.FLOW_CEDAR_PRESSURE_PLATE.get());
        this.dropSelf(ModBlocks.FLOW_CEDAR_FENCE.get());
        this.dropSelf(ModBlocks.FLOW_CEDAR_FENCE_GATE.get());
        this.dropSelf(ModBlocks.FLOW_CEDAR_TRAPDOOR.get());
        this.dropSelf(ModBlocks.WEDGE.get());
        this.dropOther(ModBlocks.FLOW_INFUSER.get(), ModBlocks.FLOW_CEDAR_LOG.get());
        this.dropSelf(ModBlocks.FLOW_CONTAINING_RAW_ORE_BLOCK.get());
        this.dropSelf(ModBlocks.FLOW_CEDAR_BIG_SAPLING.get());
        this.dropSelf(ModBlocks.FLOW_CEDAR_SAPLING.get());
        this.dropSelf(ModBlocks.CONSTRUCTION_DESORBER.get());
        this.dropSelf(ModBlocks.CULTIVATION_DESORBER.get());
        this.dropSelf(ModBlocks.TIME_PASSAGE_DESORBER.get());


        this.dropOther(ModBlocks.FLOW_CEDAR_CASING.get(),ModBlocks.FLOW_CEDAR_LOG.get());
        this.dropSelf(ModBlocks.MATTER_INFUSER_PORT.get());
        this.dropSelf(ModBlocks.MATTER_INFUSER_IO.get());

        this.add(ModBlocks.FLOW_CEDAR_SLAB.get(),
                block -> createSlabItemTable(ModBlocks.FLOW_CEDAR_SLAB.get()));
        this.add(ModBlocks.FLOW_CEDAR_DOOR.get(),
                block -> createDoorTable(ModBlocks.FLOW_CEDAR_DOOR.get()));


        this.add(ModBlocks.FLOW_CONTAINING_ORE.get(),
                block -> createCopperLikeOreDrops(ModBlocks.FLOW_CONTAINING_ORE.get(), ModItems.FLOW_CONTAINING_RAW_ORE.get()));
        this.add(ModBlocks.FLOW_CONTAINING_DEEPSLATE_ORE.get(),
                block -> createCopperLikeOreDrops(ModBlocks.FLOW_CONTAINING_DEEPSLATE_ORE.get(), ModItems.FLOW_CONTAINING_RAW_ORE.get()));
        /*this.add(ModBlocks.NETHER_SAPPHIRE_ORE.get(),
                block -> createCopperLikeOreDrops(ModBlocks.NETHER_SAPPHIRE_ORE.get(), ModItems.RAW_SAPPHIRE.get()));
        this.add(ModBlocks.END_STONE_SAPPHIRE_ORE.get(),
                block -> createCopperLikeOreDrops(ModBlocks.END_STONE_SAPPHIRE_ORE.get(), ModItems.RAW_SAPPHIRE.get()));
        */
        this.dropOther(ModBlocks.FLOW_PORT.get(),ModBlocks.FLOW_PORT.get());
        this.dropOther(ModBlocks.FLOW_CAULDRON.get(), Blocks.CAULDRON);
        this.dropOther(ModBlocks.BIRCH_JUICE_CAULDRON.get(), Blocks.CAULDRON);

        this.add(ModBlocks.FLOW_CEDAR_SIGN.get(), block ->
                createSingleItemTable(ModItems.FLOW_CEDAR_SIGN.get()));
        this.add(ModBlocks.FLOW_CEDAR_WALL_SIGN.get(), block ->
                createSingleItemTable(ModItems.FLOW_CEDAR_SIGN.get()));
        this.add(ModBlocks.FLOW_CEDAR_HANGING_SIGN.get(), block ->
                createSingleItemTable(ModItems.FLOW_CEDAR_HANGING_SIGN.get()));
        this.add(ModBlocks.FLOW_CEDAR_WALL_HANGING_SIGN.get(), block ->
                createSingleItemTable(ModItems.FLOW_CEDAR_HANGING_SIGN.get()));
    }

    protected LootTable.Builder createCopperLikeOreDrops(Block pBlock, Item item) {
        return createSilkTouchDispatchTable(pBlock,
                this.applyExplosionDecay(pBlock,
                        LootItem.lootTableItem(item)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0F, 5.0F)))
                                .apply(ApplyBonusCount.addOreBonusCount(Enchantments.BLOCK_FORTUNE))));
    }

    @Override
    protected @NotNull Iterable<Block> getKnownBlocks() {
        return ModBlocks.BLOCKS.getEntries().stream().map(RegistryObject::get)::iterator;
    }
}
