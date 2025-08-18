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
import net.sinedkadis.terracompositio.entity.client.CFECubeModel;
import net.sinedkadis.terracompositio.entity.client.FlowCedarEntModel;
import net.sinedkadis.terracompositio.entity.client.FlowCedarEntRenderer;
import net.sinedkadis.terracompositio.item.custom.CreationFlowJournalItem;
import net.sinedkadis.terracompositio.item.models.TechnetiumCrownModel;
import net.sinedkadis.terracompositio.registries.TCEntities;
import net.sinedkadis.terracompositio.registries.TCBoatRenderer;
import net.sinedkadis.terracompositio.registries.TCModelLayers;
import net.sinedkadis.terracompositio.item.custom.ShieldedBundleItem;
import net.sinedkadis.terracompositio.item.custom.WrenchAxeItem;
import net.sinedkadis.terracompositio.registries.*;
import net.sinedkadis.terracompositio.screen.FlowBlockPortScreen;
import net.sinedkadis.terracompositio.screen.TCMenuTypes;

@Mod.EventBusSubscriber(modid = TerraCompositio.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD,value = Dist.CLIENT)
public class TCEventBusClientEvents {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event)
    {
        // Some client setup code
        //LOGGER.info("HELLO FROM CLIENT SETUP");
        //LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        Sheets.addWoodType(TCWoodTypes.FLOW_CEDAR);

        EntityRenderers.register(TCEntities.MOD_BOAT.get(), pContext -> new TCBoatRenderer(pContext, false));
        EntityRenderers.register(TCEntities.MOD_CHEST_BOAT.get(), pContext -> new TCBoatRenderer(pContext, true));

        EntityRenderers.register(TCEntities.FLOW_CEDAR_ENT.get(), FlowCedarEntRenderer::new);

        MenuScreens.register(TCMenuTypes.FLOW_PORT_MENU.get(), FlowBlockPortScreen::new);
        ItemBlockRenderTypes.setRenderLayer(TCFluids.BIRCH_JUICE_FLUID.source.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(TCFluids.BIRCH_JUICE_FLUID.flowing.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(TCFluids.FLOW_FLUID.source.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(TCFluids.FLOW_FLUID.flowing.get(), RenderType.translucent());


        ItemProperties.register(
                TCItems.WRENCH_AXE.get(),
                ResourceLocation.parse("wrench_mode"),
                (stack, level, entity, seed) -> WrenchAxeItem.getMode(stack).ordinal()
        );
        ItemProperties.register(
                TCItems.SHIELDED_BUNDLE.get(),
                ResourceLocation.parse("filled"),
                (stack, level, entity, seed) -> ShieldedBundleItem.getFullnessDisplay(stack)
        );
        ItemProperties.register(
                TCItems.CREATION_FLOW_JOURNAL.get(),
                ResourceLocation.parse("in_hand"),
                (stack, level, entity, seed) ->
                        CreationFlowJournalItem.isInHand(stack,entity) ? 1.0F : 0.0F
        );
        ItemProperties.register(
                TCItems.CREATION_FLOW_JOURNAL.get(),
                ResourceLocation.parse("day"),
                (stack, level, entity, seed) -> CreationFlowJournalItem.getDay(stack)
        );
        ItemProperties.register(
                TCItems.CREATION_FLOW_JOURNAL.get(),
                ResourceLocation.parse("opened"),
                (stack, level, entity, seed) ->
                        CreationFlowJournalItem.isOpen() ? 1.0F : 0.0F
        );
    }

    @SubscribeEvent
    public static void registerLayer(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(TCModelLayers.PINE_BOAT_LAYER, BoatModel::createBodyModel);
        event.registerLayerDefinition(TCModelLayers.PINE_CHEST_BOAT_LAYER, ChestBoatModel::createBodyModel);

        event.registerLayerDefinition(TCModelLayers.FLOW_CEDAR_ENT_LAYER, FlowCedarEntModel::createBodyLayer);
        event.registerLayerDefinition(TCModelLayers.CFE_CUBE_LAYER, CFECubeModel::createBodyLayer);

        event.registerLayerDefinition(TCModelLayers.TECHNETIUM_CROWN_LAYER, TechnetiumCrownModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerBER(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(TCBlockEntities.FLOW_PORT_BE.get(), FlowPortBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntities.FLOW_CEDAR_CASING_BE.get(), FlowCedarCasingBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntities.FLOW_INFUSER_BE.get(), FlowInfuserBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntities.MOD_SIGN.get(), SignRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntities.MOD_HANGING_SIGN.get(), HangingSignRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntities.CONSTRUCTION_DESORBER_BE.get(), ConstructionDesorberBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntities.CULTIVATION_DESORBER_BE.get(), CultivationDesorberBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntities.TIME_PASSAGE_DESORBER_BE.get(), TimePassageDesorberBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntities.FLOW_CEDAR_TANK_BE.get(), FlowCedarTankBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntities.PATH_POINTER_BE.get(), PathPointerBlockEntityRenderer::new);

    }
}
