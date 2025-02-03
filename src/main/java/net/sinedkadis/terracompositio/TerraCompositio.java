package net.sinedkadis.terracompositio;

import net.minecraft.Util;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.sinedkadis.terracompositio.block.ModBlocks;
import net.sinedkadis.terracompositio.block.entity.ModBlockEntities;
import net.sinedkadis.terracompositio.api.cfe.CFENetwork;
import net.sinedkadis.terracompositio.util.CFENetworkHandler;
import net.sinedkadis.terracompositio.effect.ModEffects;
import net.sinedkadis.terracompositio.entity.ModEntities;
import net.sinedkadis.terracompositio.entity.client.ModBoatRenderer;
import net.sinedkadis.terracompositio.events.CFENetworkEvent;
import net.sinedkadis.terracompositio.fluid.ModFluids;
import net.sinedkadis.terracompositio.item.ModCreativeModTabs;
import net.sinedkadis.terracompositio.item.ModItems;
import net.sinedkadis.terracompositio.particle.ModParticles;
import net.sinedkadis.terracompositio.potion.ModPotions;
import net.sinedkadis.terracompositio.recipe.ModRecipes;
import net.sinedkadis.terracompositio.screen.FlowBlockPortScreen;
import net.sinedkadis.terracompositio.screen.ModMenuTypes;
import net.sinedkadis.terracompositio.sound.ModSounds;
import net.sinedkadis.terracompositio.util.ModGameRules;
import net.sinedkadis.terracompositio.util.ModWoodTypes;
import net.sinedkadis.terracompositio.worldgen.tree.ModFoliagePlacers;
import net.sinedkadis.terracompositio.worldgen.tree.ModTrunkPlacerTypes;


@Mod(TerraCompositio.MOD_ID)
public class TerraCompositio
{
    public static final String MOD_ID = "terracompositio";

    public TerraCompositio() {

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModCreativeModTabs.register(modEventBus);

        ModItems.register(modEventBus);

        ModFluids.FLUIDS.register(modEventBus);
        ModFluids.FLUID_TYPES.register(modEventBus);

        ModBlocks.register(modEventBus);

        modEventBus.addListener(this::commonSetup);

        ModParticles.register(modEventBus);
        ModEffects.register(modEventBus);
        ModPotions.register(modEventBus);
        ModSounds.register(modEventBus);
        ModEntities.register(modEventBus);

        ModBlockEntities.register(modEventBus);
        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        ModRecipes.register(modEventBus);
        ModMenuTypes.register(modEventBus);
        ModTrunkPlacerTypes.register(modEventBus);
        ModFoliagePlacers.register(modEventBus);


        ModGameRules.init();
    }


    private void commonSetup(final FMLCommonSetupEvent event) {
        IEventBus bus = MinecraftForge.EVENT_BUS;
        bus.addListener((CFENetworkEvent e) -> CFENetworkHandler.instance.onNetworkEvent(e.getReceiver(),e.getAction()));
    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event)
    {
        /*if(event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.Pebble);
            event.accept(ModItems.StoneStaff);
        }*/
    }

    public static String makeDescriptionId(String base, String name) {
        return Util.makeDescriptionId(base, getResource(name));
    }
    public static ResourceLocation getResource(String name) {
        return new ResourceLocation(MOD_ID, name);
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        // Do something when the server starts
        //LOGGER.info("HELLO from server starting");
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            // Some client setup code
            //LOGGER.info("HELLO FROM CLIENT SETUP");
            //LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
            Sheets.addWoodType(ModWoodTypes.FLOW_CEDAR);

            EntityRenderers.register(ModEntities.MOD_BOAT.get(), pContext -> new ModBoatRenderer(pContext, false));
            EntityRenderers.register(ModEntities.MOD_CHEST_BOAT.get(), pContext -> new ModBoatRenderer(pContext, true));

            MenuScreens.register(ModMenuTypes.FLOW_PORT_MENU.get(), FlowBlockPortScreen::new);
            ItemBlockRenderTypes.setRenderLayer(ModFluids.BIRCH_JUICE_FLUID.source.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModFluids.BIRCH_JUICE_FLUID.flowing.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModFluids.FLOW_FLUID.source.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModFluids.FLOW_FLUID.flowing.get(), RenderType.translucent());
        }
    }
    public CFENetwork getCFENetworkInstance(){
        return CFENetworkHandler.instance;
    }
}
