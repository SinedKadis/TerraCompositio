package net.sinedkadis.terracompositio.registries;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.sinedkadis.terracompositio.TerraCompositio;

public class ModCreativeModTabs {
    public static final DeferredRegister<CreativeModeTab> Creative_mode_tabs =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TerraCompositio.MOD_ID);
    public static final RegistryObject<CreativeModeTab> Terra_Compositio = Creative_mode_tabs.register("terra_compositio",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModBlocks.FLOW_CEDAR_LOG.get()))
                    .title(Component.translatable("creativetab.terra_compositio"))
                    .displayItems((pParameters, pOutput) -> {
                        pOutput.accept(ModItems.PEBBLE.get());
                        pOutput.accept(ModItems.STONE_STAFF.get());
                        pOutput.accept(ModItems.OAK_STAFF.get());
                        pOutput.accept(ModItems.WRENCH_AXE.get());
                        pOutput.accept(ModBlocks.FLOW_CEDAR_LOG.get());
                        pOutput.accept(ModBlocks.STRIPPED_FLOW_CEDAR_LOG.get());
                        pOutput.accept(ModBlocks.FLOW_CEDAR_WOOD.get());
                        pOutput.accept(ModBlocks.STRIPPED_FLOW_CEDAR_WOOD.get());
                        pOutput.accept(ModBlocks.FLOW_CEDAR_PORT.get());
                        pOutput.accept(ModBlocks.FLOW_CEDAR_LEAVES.get());
                        pOutput.accept(ModBlocks.FLOW_CEDAR_PLANKS.get());
                        pOutput.accept(ModBlocks.FLOW_CEDAR_STAIRS.get());
                        pOutput.accept(ModBlocks.FLOW_CEDAR_SLAB.get());
                        pOutput.accept(ModBlocks.FLOW_CEDAR_BUTTON.get());
                        pOutput.accept(ModBlocks.FLOW_CEDAR_PRESSURE_PLATE.get());
                        pOutput.accept(ModBlocks.FLOW_CEDAR_FENCE.get());
                        pOutput.accept(ModBlocks.FLOW_CEDAR_FENCE_GATE.get());
                        pOutput.accept(ModBlocks.FLOW_CEDAR_DOOR.get());
                        pOutput.accept(ModBlocks.FLOW_CEDAR_TRAPDOOR.get());

                        pOutput.accept(ModItems.FLOW_CEDAR_SIGN.get());
                        pOutput.accept(ModItems.FLOW_CEDAR_HANGING_SIGN.get());

                        pOutput.accept(ModItems.FLOW_CEDAR_BOAT.get());
                        pOutput.accept(ModItems.FLOW_CEDAR_CHEST_BOAT.get());

                        pOutput.accept(ModFluids.FLOW_FLUID.bucket.get());
                        pOutput.accept(ModItems.FLOW_BOTTLE.get());
                        pOutput.accept(ModFluids.BIRCH_JUICE_FLUID.bucket.get());
                        pOutput.accept(ModBlocks.WEDGE.get());

                        pOutput.accept(ModItems.FLOW_CEDAR_HELMET.get());
                        pOutput.accept(ModItems.FLOW_CEDAR_CHESTPLATE.get());
                        pOutput.accept(ModItems.FLOW_CEDAR_LEGGINGS.get());
                        pOutput.accept(ModItems.FLOW_CEDAR_BOOTS.get());
                        pOutput.accept(ModBlocks.CREATIVE_CFE_SOURCE.get());
                        pOutput.accept(ModBlocks.FLOW_INFUSER.get());

                        pOutput.accept(ModItems.INFUSED_IRON_INGOT.get());
                        pOutput.accept(ModItems.INFUSED_IRON_ROD.get());
                        pOutput.accept(ModItems.GOLD_ROD.get());
                        pOutput.accept(ModItems.COPPER_ROD.get());

                        pOutput.accept(ModItems.COPPER_NUGGET.get());
                        pOutput.accept(ModItems.FLOW_INFUSER_KIT.get());
                        pOutput.accept(ModItems.RAW_TECHNETIUM.get());
                        pOutput.accept(ModBlocks.TECHNETIUM_RAW_ORE_BLOCK.get());
                        pOutput.accept(ModItems.LOW_ENRICHED_TECHNETIUM.get());
                        pOutput.accept(ModItems.MEDIUM_ENRICHED_TECHNETIUM.get());
                        pOutput.accept(ModItems.HIGH_ENRICHED_TECHNETIUM.get());
                        pOutput.accept(ModBlocks.TECHNETIUM_ORE.get());
                        pOutput.accept(ModBlocks.TECHNETIUM_DEEPSLATE_ORE.get());

                        pOutput.accept(ModBlocks.FLOW_CEDAR_SAPLING.get());

                        pOutput.accept(ModBlocks.FLOW_CEDAR_CASING.get());
                        pOutput.accept(ModBlocks.MATTER_INFUSER_PORT.get());
                        pOutput.accept(ModBlocks.MATTER_INFUSER_IO.get());
                        pOutput.accept(ModItems.INPUT_BUS.get());
                        pOutput.accept(ModItems.OUTPUT_BUS.get());

                        pOutput.accept(ModBlocks.CONSTRUCTION_DESORBER.get());
                        pOutput.accept(ModBlocks.CULTIVATION_DESORBER.get());
                        pOutput.accept(ModBlocks.TIME_PASSAGE_DESORBER.get());

                        pOutput.accept(ModItems.SHIELDED_BUNDLE.get());

                        pOutput.accept(ModBlocks.FLOW_CEDAR_PEDESTAL.get());
                        pOutput.accept(ModBlocks.FLOW_CEDAR_TANK.get());

                    })
                    .build());

    public static void register(IEventBus eventBus) {
        Creative_mode_tabs.register(eventBus);
    }
}
