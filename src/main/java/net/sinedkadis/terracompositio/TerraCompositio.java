package net.sinedkadis.terracompositio;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.sinedkadis.terracompositio.registries.*;
import net.sinedkadis.terracompositio.util.CFENetworkHandler;
import net.sinedkadis.terracompositio.effect.ModEffects;
import net.sinedkadis.terracompositio.entity.ModEntities;
import net.sinedkadis.terracompositio.events.CFENetworkEvent;
import net.sinedkadis.terracompositio.screen.ModMenuTypes;
import net.sinedkadis.terracompositio.sound.ModSounds;
import net.sinedkadis.terracompositio.worldgen.tree.ModFoliagePlacers;
import net.sinedkadis.terracompositio.worldgen.tree.ModTrunkPlacers;


@Mod(TerraCompositio.MOD_ID)
public class TerraCompositio
{
    public static final String MOD_ID = "terracompositio";
    public static ResourceLocation modLoc(String location){
        return new ResourceLocation(MOD_ID,location);
    }
    //public static SimpleChannel CHANNEL;

    public TerraCompositio() {

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addCreative);

        MinecraftForge.EVENT_BUS.register(this);
        ModFluids.FLUIDS.register(modEventBus);
        ModFluids.FLUID_TYPES.register(modEventBus);

        ModCreativeModTabs.register(modEventBus);
        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModParticles.register(modEventBus);
        ModEffects.register(modEventBus);
        ModPotions.register(modEventBus);
        ModSounds.register(modEventBus);
        ModEntities.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModRecipes.register(modEventBus);
        ModMenuTypes.register(modEventBus);
        ModTrunkPlacers.register(modEventBus);
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
        if(event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(Items.BUNDLE);
        }
    }

}
