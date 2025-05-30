package net.sinedkadis.terracompositio.events;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.model.BoatModel;
import net.minecraft.client.model.ChestBoatModel;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.HangingSignRenderer;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.block.entity.renderer.*;
import net.sinedkadis.terracompositio.entity.ModEntities;
import net.sinedkadis.terracompositio.entity.client.ModBoatRenderer;
import net.sinedkadis.terracompositio.entity.client.ModModelLayers;
import net.sinedkadis.terracompositio.item.custom.ShieldedBundleItem;
import net.sinedkadis.terracompositio.item.custom.WrenchAxeItem;
import net.sinedkadis.terracompositio.registries.*;
import net.sinedkadis.terracompositio.screen.FlowBlockPortScreen;
import net.sinedkadis.terracompositio.screen.ModMenuTypes;

@Mod.EventBusSubscriber(modid = TerraCompositio.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD,value = Dist.CLIENT)
public class ModEventBusClientEvents {
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


        event.enqueueWork(() -> ItemProperties.register(
                ModItems.WRENCH_AXE.get(),
                ResourceLocation.parse("wrench_mode"),
                (stack, level, entity, seed) -> WrenchAxeItem.getMode(stack).ordinal()
        ));
        event.enqueueWork(() -> ItemProperties.register(
                ModItems.SHIELDED_BUNDLE.get(),
                ResourceLocation.parse("filled"),
                (stack, level, entity, seed) -> ShieldedBundleItem.getFullnessDisplay(stack)
        ));
    }

    @SubscribeEvent
    public static void registerLayer(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ModModelLayers.PINE_BOAT_LAYER, BoatModel::createBodyModel);
        event.registerLayerDefinition(ModModelLayers.PINE_CHEST_BOAT_LAYER, ChestBoatModel::createBodyModel);
    }

    @SubscribeEvent
    public static void registerBER(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.FLOW_PORT_BE.get(), FlowPortBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.FLOW_CEDAR_CASING_BE.get(), FlowCedarCasingBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.FLOW_INFUSER_BE.get(), FlowInfuserBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MOD_SIGN.get(), SignRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MOD_HANGING_SIGN.get(), HangingSignRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.CONSTRUCTION_DESORBER_BE.get(), ConstructionDesorberBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.CULTIVATION_DESORBER_BE.get(), CultivationDesorberBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.TIME_PASSAGE_DESORBER_BE.get(), TimePassageDesorberBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.FLOW_CEDAR_TANK_BE.get(), FlowCedarTankBlockEntityRenderer::new);

    }
}
