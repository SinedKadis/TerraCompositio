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
import net.sinedkadis.terracompositio.registries.TCBlocks;
import net.sinedkadis.terracompositio.registries.TCItems;
import org.jetbrains.annotations.NotNull;


import java.util.Set;

public class TCBlockLootTables extends BlockLootSubProvider {
    public TCBlockLootTables() {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags());
    }

    @Override
    protected void generate() {
        this.dropSelf(TCBlocks.FLOW_CEDAR_LOG.get());
        this.dropSelf(TCBlocks.FLOW_CEDAR_WOOD.get());
        this.dropSelf(TCBlocks.FLOW_CEDAR_PLANKS.get());
        this.dropSelf(TCBlocks.STRIPPED_FLOW_CEDAR_LOG.get());
        this.dropSelf(TCBlocks.STRIPPED_FLOW_CEDAR_WOOD.get());
        this.dropOther(TCBlocks.FLOW_CEDAR_PORT.get(), TCBlocks.FLOW_CEDAR_LOG.get());
        this.dropSelf(TCBlocks.CREATIVE_CFE_SOURCE.get());
        this.dropSelf(TCBlocks.CFE_TRASH_CAN.get());

        this.dropSelf(TCBlocks.FLOW_CEDAR_STAIRS.get());
        this.dropSelf(TCBlocks.FLOW_CEDAR_BUTTON.get());
        this.dropSelf(TCBlocks.FLOW_CEDAR_PRESSURE_PLATE.get());
        this.dropSelf(TCBlocks.FLOW_CEDAR_FENCE.get());
        this.dropSelf(TCBlocks.FLOW_CEDAR_FENCE_GATE.get());
        this.dropSelf(TCBlocks.FLOW_CEDAR_TRAPDOOR.get());
        this.dropSelf(TCBlocks.WEDGE.get());
        this.dropOther(TCBlocks.FLOW_INFUSER.get(), TCBlocks.FLOW_CEDAR_LOG.get());
        this.dropSelf(TCBlocks.TECHNETIUM_RAW_ORE_BLOCK.get());

        this.dropSelf(TCBlocks.FLOW_CEDAR_SAPLING.get());
        this.dropSelf(TCBlocks.CONSTRUCTION_DESORBER.get());
        this.dropSelf(TCBlocks.CULTIVATION_DESORBER.get());
        this.dropSelf(TCBlocks.TIME_PASSAGE_DESORBER.get());

        this.dropSelf(TCBlocks.FLOW_CEDAR_TANK.get());
        this.dropOther(TCBlocks.FLOW_CEDAR_PEDESTAL.get(), TCBlocks.FLOW_CEDAR_SAPLING.get());

        this.dropOther(TCBlocks.FLOW_CEDAR_CASING.get(), TCBlocks.FLOW_CEDAR_LOG.get());
        this.dropSelf(TCBlocks.MATTER_INFUSER_PORT.get());
        this.dropSelf(TCBlocks.MATTER_INFUSER_IO.get());

        this.add(TCBlocks.FLOW_CEDAR_SLAB.get(),
                block -> createSlabItemTable(TCBlocks.FLOW_CEDAR_SLAB.get()));
        this.add(TCBlocks.FLOW_CEDAR_DOOR.get(),
                block -> createDoorTable(TCBlocks.FLOW_CEDAR_DOOR.get()));

        this.dropSelf(TCBlocks.FLOW_CEDAR_LEAVES.get());

        this.add(TCBlocks.TECHNETIUM_ORE.get(),
                block -> createCopperLikeOreDrops(TCBlocks.TECHNETIUM_ORE.get(), TCItems.RAW_TECHNETIUM.get()));
        this.add(TCBlocks.TECHNETIUM_DEEPSLATE_ORE.get(),
                block -> createCopperLikeOreDrops(TCBlocks.TECHNETIUM_DEEPSLATE_ORE.get(), TCItems.RAW_TECHNETIUM.get()));
        /*this.add(ModBlocks.NETHER_SAPPHIRE_ORE.get(),
                block -> createCopperLikeOreDrops(ModBlocks.NETHER_SAPPHIRE_ORE.get(), ModItems.RAW_SAPPHIRE.get()));
        this.add(ModBlocks.END_STONE_SAPPHIRE_ORE.get(),
                block -> createCopperLikeOreDrops(ModBlocks.END_STONE_SAPPHIRE_ORE.get(), ModItems.RAW_SAPPHIRE.get()));
        */
        this.dropOther(TCBlocks.FLOW_CEDAR_PORT.get(), TCBlocks.FLOW_CEDAR_PORT.get());
        this.dropOther(TCBlocks.FLOW_CAULDRON.get(), Blocks.CAULDRON);
        this.dropOther(TCBlocks.BIRCH_JUICE_CAULDRON.get(), Blocks.CAULDRON);

        this.add(TCBlocks.FLOW_CEDAR_SIGN.get(), block ->
                createSingleItemTable(TCItems.FLOW_CEDAR_SIGN.get()));
        this.add(TCBlocks.FLOW_CEDAR_WALL_SIGN.get(), block ->
                createSingleItemTable(TCItems.FLOW_CEDAR_SIGN.get()));
        this.add(TCBlocks.FLOW_CEDAR_HANGING_SIGN.get(), block ->
                createSingleItemTable(TCItems.FLOW_CEDAR_HANGING_SIGN.get()));
        this.add(TCBlocks.FLOW_CEDAR_WALL_HANGING_SIGN.get(), block ->
                createSingleItemTable(TCItems.FLOW_CEDAR_HANGING_SIGN.get()));

        this.dropSelf(TCBlocks.PP_COLLECTOR.get());
        this.dropSelf(TCBlocks.PP_EMITTER.get());
        this.dropSelf(TCBlocks.PP_RECEIVER.get());
        this.dropSelf(TCBlocks.PP_SENDER.get());

        this.dropSelf(TCBlocks.TECHNETIUM_BLOCK.get());
        this.dropSelf(TCBlocks.FLOW_CEDAR_ENT_STATUE.get());

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
        return TCBlocks.BLOCKS.getEntries().stream().map(RegistryObject::get)::iterator;
    }
}
