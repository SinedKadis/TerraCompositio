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
import net.sinedkadis.terracompositio.events.FluidNetworkEvent;
import net.sinedkadis.terracompositio.registries.*;
import net.sinedkadis.terracompositio.cfe.CFENetworkHandler;
import net.sinedkadis.terracompositio.registries.TCEffects;
import net.sinedkadis.terracompositio.registries.TCEntities;
import net.sinedkadis.terracompositio.events.CFENetworkEvent;
import net.sinedkadis.terracompositio.screen.TCMenuTypes;
import net.sinedkadis.terracompositio.registries.TCSounds;
import net.sinedkadis.terracompositio.fluid.FluidNetworkHandler;
import net.sinedkadis.terracompositio.worldgen.biome.TCSurfaceRules;
import net.sinedkadis.terracompositio.worldgen.biome.TCTerrablender;
import net.sinedkadis.terracompositio.worldgen.tree.TCFoliagePlacers;
import net.sinedkadis.terracompositio.worldgen.tree.TCTrunkPlacers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import terrablender.api.SurfaceRuleManager;

//6011371823902440939
@Mod(TerraCompositio.MOD_ID)
public class TerraCompositio
{
    public static final String MOD_ID = "terracompositio";
    public static ResourceLocation modLoc(String location){
        return ResourceLocation.tryBuild(MOD_ID,location);
    }
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public TerraCompositio() {

        @SuppressWarnings("removal")
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addCreative);

        MinecraftForge.EVENT_BUS.register(this);
        TCFluids.FLUIDS.register(modEventBus);
        TCFluids.FLUID_TYPES.register(modEventBus);

        TCCreativeModeTabs.register(modEventBus);
        TCItems.register(modEventBus);
        TCBlocks.register(modEventBus);
        TCParticles.register(modEventBus);
        TCEffects.register(modEventBus);
        TCPotions.register(modEventBus);
        TCSounds.register(modEventBus);
        TCEntities.register(modEventBus);
        TCBlockEntities.register(modEventBus);
        TCRecipes.register(modEventBus);
        TCMenuTypes.register(modEventBus);
        TCTrunkPlacers.register(modEventBus);
        TCFoliagePlacers.register(modEventBus);
        TCTerrablender.registerBiomes();

        TCGameRules.init();
    }


    private void commonSetup(final FMLCommonSetupEvent event) {
        IEventBus bus = MinecraftForge.EVENT_BUS;
        bus.addListener((CFENetworkEvent e) -> CFENetworkHandler.instance.onNetworkEvent(e.getSource(),e.getAction()));
        bus.addListener((FluidNetworkEvent e) -> FluidNetworkHandler.instance.onNetworkEvent(e.getSource(),e.getAction()));
        //SurfaceRuleManager.addSurfaceRules(SurfaceRuleManager.RuleCategory.OVERWORLD, MOD_ID, TCSurfaceRules.makeRules());
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if(event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(Items.BUNDLE);
        }
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            event.accept(TCItems.FLOW_CEDAR_ENT_SPAWN_EGG);
        }
    }

}
