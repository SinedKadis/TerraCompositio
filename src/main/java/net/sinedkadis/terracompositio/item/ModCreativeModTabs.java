package net.sinedkadis.terracompositio.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.block.ModBlocks;
import net.sinedkadis.terracompositio.fluid.ModFluids;

public class ModCreativeModTabs {
    public static final DeferredRegister<CreativeModeTab> Creative_mode_tabs =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TerraCompositio.MOD_ID);
    public static final RegistryObject<CreativeModeTab> Terra_Compositio = Creative_mode_tabs.register("terra_compositio",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.PEBBLE.get()))
                    .title(Component.translatable("creativetab.terra_compositio"))
                    .displayItems((pParameters, pOutput) -> {
                        pOutput.accept(ModItems.PEBBLE.get());
                        pOutput.accept(ModItems.STONE_STAFF.get());
                        pOutput.accept(ModItems.OAK_STAFF.get());
                        pOutput.accept(ModBlocks.FLOW_CEDAR_LOG.get());
                        pOutput.accept(ModBlocks.STRIPPED_FLOW_CEDAR_LOG.get());
                        pOutput.accept(ModBlocks.FLOW_CEDAR_WOOD.get());
                        pOutput.accept(ModBlocks.STRIPPED_FLOW_CEDAR_WOOD.get());
                        pOutput.accept(ModBlocks.FLOW_PORT.get());
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
                        pOutput.accept(ModBlocks.FLOW_EXTRACTOR.get());

                        pOutput.accept(ModItems.FLOW_CEDAR_HELMET.get());
                        pOutput.accept(ModItems.FLOW_CEDAR_CHESTPLATE.get());
                        pOutput.accept(ModItems.FLOW_CEDAR_LEGGINGS.get());
                        pOutput.accept(ModItems.FLOW_CEDAR_BOOTS.get());
                        pOutput.accept(ModBlocks.CREATIVE_CFE_SOURCE.get());
                        pOutput.accept(ModBlocks.FLOW_INFUSER.get());


                    })
                    .build());

    public static void register(IEventBus eventBus) {
        Creative_mode_tabs.register(eventBus);
    }
}
