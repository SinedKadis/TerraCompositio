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
import net.minecraftforge.network.NetworkRegistry;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.block.entity.renderer.FlowExtractorBlockEntityRenderer;
import net.sinedkadis.terracompositio.block.entity.renderer.FlowInfuserBlockEntityRenderer;
import net.sinedkadis.terracompositio.block.entity.renderer.FlowPortBlockEntityRenderer;
import net.sinedkadis.terracompositio.block.entity.renderer.MatterInfuserPortBlockEntityRenderer;
import net.sinedkadis.terracompositio.entity.ModEntities;
import net.sinedkadis.terracompositio.entity.client.ModBoatRenderer;
import net.sinedkadis.terracompositio.entity.client.ModModelLayers;
import net.sinedkadis.terracompositio.item.custom.WrenchAxeItem;
import net.sinedkadis.terracompositio.network.packets.SyncItemHandlerPacket;
import net.sinedkadis.terracompositio.registries.ModBlockEntities;
import net.sinedkadis.terracompositio.registries.ModFluids;
import net.sinedkadis.terracompositio.registries.ModItems;
import net.sinedkadis.terracompositio.registries.ModWoodTypes;
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
                ModItems.FLOW_ROTATING_AXE.get(),
                new ResourceLocation("wrench_mode"),
                (stack, level, entity, seed) -> WrenchAxeItem.getMode(stack).ordinal()
        ));

        TerraCompositio.CHANNEL = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation(TerraCompositio.MOD_ID, "sync_item_handler"))
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .networkProtocolVersion(() -> "1.0")
                .simpleChannel();

        TerraCompositio.CHANNEL.messageBuilder(SyncItemHandlerPacket.class, 0)
                .encoder(SyncItemHandlerPacket::encode)
                .decoder(SyncItemHandlerPacket::decode)
                .consumerMainThread(SyncItemHandlerPacket::handle)
                .add();
    }

    @SubscribeEvent
    public static void registerLayer(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ModModelLayers.PINE_BOAT_LAYER, BoatModel::createBodyModel);
        event.registerLayerDefinition(ModModelLayers.PINE_CHEST_BOAT_LAYER, ChestBoatModel::createBodyModel);
    }

    @SubscribeEvent
    public static void registerBER(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.FLOW_PORT_BE.get(), FlowPortBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MATTER_INFUSER_PORT_BE.get(), MatterInfuserPortBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.FLOW_INFUSER_BE.get(), FlowInfuserBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.FLOW_EXTRACTOR_BE.get(), FlowExtractorBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MOD_SIGN.get(), SignRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MOD_HANGING_SIGN.get(), HangingSignRenderer::new);
    }
}
