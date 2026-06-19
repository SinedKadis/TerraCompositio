package net.sinedkadis.terracompositio.registries;

import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.DeferredRegister;
import net.sinedkadis.terracompositio.TerraCompositio;

public class TCCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> Creative_mode_tabs =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TerraCompositio.MOD_ID);
    static {
        Creative_mode_tabs.register("terra_compositio",
                () -> CreativeModeTab.builder().icon(() -> new ItemStack(TCBlocks.FLOW_CEDAR_LOG.get()))
                        .title(Component.translatable("creativetab.terra_compositio"))
                        .displayItems((pParameters, pOutput) -> {
                            //Creative
                            pOutput.accept(TCBlocks.CREATIVE_CFE_SOURCE.get());
                            pOutput.accept(TCBlocks.CFE_TRASH_CAN.get());


                            //Useful
                            pOutput.accept(TCItems.WRENCH_AXE.get());
                            addBooks(pOutput);
                            pOutput.accept(TCItems.APPLE_OF_KNOWLEDGE.get());
                            pOutput.accept(TCItems.APPLE_OF_IGNORANCE.get());

                            pOutput.accept(TCFluids.FLOW_FLUID.bucket.get());
                            pOutput.accept(TCItems.FLOW_BOTTLE.get());
                            pOutput.accept(TCFluids.BIRCH_JUICE_FLUID.bucket.get());
                            pOutput.accept(TCBlocks.WEDGE.get());
                            pOutput.accept(TCItems.SHIELDED_BUNDLE.get());
                            addFluidAppliers(pOutput);
                            pOutput.accept(TCItems.CFE_BALL.get());
                            pOutput.accept(TCItems.INFUSED_FERTILIZER.get());

                            //Cedar blocks
                            pOutput.accept(TCBlocks.FLOW_CEDAR_LOG.get());
                            pOutput.accept(TCBlocks.STRIPPED_FLOW_CEDAR_LOG.get());
                            pOutput.accept(TCBlocks.FLOW_CEDAR_WOOD.get());
                            pOutput.accept(TCBlocks.STRIPPED_FLOW_CEDAR_WOOD.get());
                            pOutput.accept(TCBlocks.FLOW_CEDAR_SAPLING.get());

                            pOutput.accept(TCBlocks.FLOW_CEDAR_LEAVES.get());
                            pOutput.accept(TCBlocks.FLOW_CEDAR_PLANKS.get());
                            pOutput.accept(TCBlocks.FLOW_CEDAR_STAIRS.get());
                            pOutput.accept(TCBlocks.FLOW_CEDAR_SLAB.get());
                            pOutput.accept(TCBlocks.FLOW_CEDAR_BUTTON.get());
                            pOutput.accept(TCBlocks.FLOW_CEDAR_PRESSURE_PLATE.get());
                            pOutput.accept(TCBlocks.FLOW_CEDAR_FENCE.get());
                            pOutput.accept(TCBlocks.FLOW_CEDAR_FENCE_GATE.get());
                            pOutput.accept(TCBlocks.FLOW_CEDAR_DOOR.get());
                            pOutput.accept(TCBlocks.FLOW_CEDAR_TRAPDOOR.get());
                            pOutput.accept(TCItems.FLOW_CEDAR_SIGN.get());
                            pOutput.accept(TCItems.FLOW_CEDAR_HANGING_SIGN.get());

                            pOutput.accept(TCItems.FLOW_CEDAR_BOAT.get());
                            pOutput.accept(TCItems.FLOW_CEDAR_CHEST_BOAT.get());

                            pOutput.accept(TCItems.FLOW_CEDAR_HELMET.get());
                            pOutput.accept(TCItems.FLOW_CEDAR_CHESTPLATE.get());
                            pOutput.accept(TCItems.FLOW_CEDAR_LEGGINGS.get());
                            pOutput.accept(TCItems.FLOW_CEDAR_BOOTS.get());


                            //Materials

                            //  Copper
                            pOutput.accept(TCItems.COPPER_NUGGET.get());
                            pOutput.accept(TCItems.COPPER_ROD.get());

                            //  Gold
                            pOutput.accept(TCItems.GOLD_ROD.get());

                            //  Infused iron
                            pOutput.accept(TCItems.INFUSED_IRON_INGOT.get());
                            pOutput.accept(TCItems.INFUSED_IRON_NUGGET.get());
                            pOutput.accept(TCBlocks.INFUSED_IRON_BLOCK.get());
                            pOutput.accept(TCItems.INFUSED_IRON_ROD.get());

                            //  Technetium
                            pOutput.accept(TCItems.TECHNETIUM_INGOT.get());
                            pOutput.accept(TCItems.TECHNETIUM_NUGGET.get());
                            pOutput.accept(TCBlocks.TECHNETIUM_BLOCK.get());
                            pOutput.accept(TCItems.TECHNETIUM_ROD.get());

                            pOutput.accept(TCItems.RAW_TECHNETIUM.get());
                            pOutput.accept(TCBlocks.TECHNETIUM_RAW_ORE_BLOCK.get());
                            pOutput.accept(TCItems.LOW_ENRICHED_TECHNETIUM.get());
                            pOutput.accept(TCItems.MEDIUM_ENRICHED_TECHNETIUM.get());
                            pOutput.accept(TCItems.HIGH_ENRICHED_TECHNETIUM.get());
                            pOutput.accept(TCBlocks.TECHNETIUM_ORE.get());
                            pOutput.accept(TCBlocks.TECHNETIUM_DEEPSLATE_ORE.get());

                            pOutput.accept(TCBlocks.CFE_BOARD.get());

                            //  armor
                            pOutput.accept(TCItems.TECHNETIUM_CROWN.get());
                            pOutput.accept(TCItems.TECHNETIUM_CHESTPLATE.get());
                            pOutput.accept(TCItems.TECHNETIUM_LEGGINGS.get());
                            pOutput.accept(TCItems.TECHNETIUM_BOOTS.get());


                            //be
                            pOutput.accept(TCBlocks.FLOW_CEDAR_ALTAR.get());
                            pOutput.accept(TCItems.FLOW_INFUSER_KIT.get());
                            pOutput.accept(TCBlocks.FLOW_INFUSER.get());
                            pOutput.accept(TCBlocks.AIR_SATURATOR.get());

                            //matter infuser
                            pOutput.accept(TCBlocks.FLOW_CEDAR_CASING.get());
                            pOutput.accept(TCBlocks.MATTER_INFUSER_PORT.get());
                            pOutput.accept(TCBlocks.MATTER_INFUSER_UNIT.get());
                            pOutput.accept(TCItems.INPUT_BUS.get());
                            pOutput.accept(TCItems.OUTPUT_BUS.get());

                            //desorbers
                            pOutput.accept(TCBlocks.CONSTRUCTION_DESORBER.get());
                            pOutput.accept(TCBlocks.CULTIVATION_DESORBER.get());
                            pOutput.accept(TCBlocks.TIME_PASSAGE_DESORBER.get());

                            //Cedar tank
                            pOutput.accept(TCBlocks.FLOW_CEDAR_PEDESTAL.get());
                            pOutput.accept(TCBlocks.FLOW_CEDAR_TANK.get());

                            //pp
                            pOutput.accept(TCBlocks.PP_COLLECTOR.get());
                            pOutput.accept(TCBlocks.PP_EMITTER.get());
                            pOutput.accept(TCBlocks.PP_RECEIVER.get());
                            pOutput.accept(TCBlocks.PP_SENDER.get());
                            pOutput.accept(TCBlocks.PP_EXTRACTOR.get());
                            pOutput.accept(TCBlocks.PP_INFUSER.get());

                            //Floating redstone
                            pOutput.accept(TCBlocks.FLOATING_REDSTONE.get());
                            pOutput.accept(TCBlocks.FLOATING_REPEATER.get());
                            pOutput.accept(TCBlocks.FLOATING_COMPARATOR.get());
                            pOutput.accept(TCBlocks.FLOATING_TORCH_HOLDER.get());
                            pOutput.accept(TCBlocks.INFUSED_IRON_PRESSURE_PLATE.get());
                            pOutput.accept(TCBlocks.INFUSED_IRON_DOOR.get());
                            pOutput.accept(TCBlocks.FLOATING_BUTTON.get());
                            pOutput.accept(TCBlocks.FLOATING_LEVER.get());




                            //misc
                            pOutput.accept(TCItems.FLOW_CEDAR_ENT_SPAWN_EGG.get());
                            pOutput.accept(TCBlocks.FLOW_CEDAR_ENT_STATUE.get());


                            //create compat
                            if (ModList.get().isLoaded("create"))
                                TerraCompositio.createCompat.addCreativeTab(pOutput);

                        })
                        .build());
    }

    private static void addFluidAppliers(CreativeModeTab.Output pOutput) {
        pOutput.accept(TCItems.FLUID_APPLIER.get());
        ItemStack itemStack = new ItemStack(TCItems.FLUID_APPLIER.get());
        CompoundTag tag = itemStack.getOrCreateTag();
        tag.putInt("Amount", 8000);
        tag.putString("FluidName", "terracompositio:flow_source");
        pOutput.accept(itemStack);
    }

    private static void addBooks(CreativeModeTab.Output pOutput) {
        for (int i = 1; i <= 5; i++) {
            ItemStack itemStack = new ItemStack(TCItems.CREATION_FLOW_JOURNAL.get());
            itemStack.getOrCreateTag().putInt("day", i);
            pOutput.accept(itemStack);
        }
    }

    public static void register(IEventBus eventBus) {
        Creative_mode_tabs.register(eventBus);
    }
}
