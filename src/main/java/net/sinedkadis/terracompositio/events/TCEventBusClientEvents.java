package net.sinedkadis.terracompositio.events;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.BoatModel;
import net.minecraft.client.model.ChestBoatModel;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.HangingSignRenderer;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.block.entity.renderer.*;
import net.sinedkadis.terracompositio.ecf.burst.ECFBurstRenderer;
import net.sinedkadis.terracompositio.entity.client.ECFCloudRenderer;
import net.sinedkadis.terracompositio.entity.client.ECFCubeModel;
import net.sinedkadis.terracompositio.entity.client.FlowCedarEntModel;
import net.sinedkadis.terracompositio.entity.client.FlowCedarEntRenderer;
import net.sinedkadis.terracompositio.gui.CFEBarRenderer;
import net.sinedkadis.terracompositio.gui.ECFHud;
import net.sinedkadis.terracompositio.gui.KnowledgeOverlay;
import net.sinedkadis.terracompositio.item.custom.CreationFlowJournalItem;
import net.sinedkadis.terracompositio.item.custom.ShieldedBundleItem;
import net.sinedkadis.terracompositio.item.custom.TechnetiumArmorItem;
import net.sinedkadis.terracompositio.item.custom.WrenchAxeItem;
import net.sinedkadis.terracompositio.item.models.TechnetiumBootsModel;
import net.sinedkadis.terracompositio.item.models.TechnetiumChestplateModel;
import net.sinedkadis.terracompositio.item.models.TechnetiumCrownModel;
import net.sinedkadis.terracompositio.particle.custom.*;
import net.sinedkadis.terracompositio.registries.*;

import java.util.Map;
import java.util.Optional;

@Mod.EventBusSubscriber(modid = TerraCompositio.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD,value = Dist.CLIENT)
public class TCEventBusClientEvents {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event)
    {

        Sheets.addWoodType(TCWoodTypes.FLOW_CEDAR);

        EntityRenderers.register(TCEntities.TC_BOAT.get(), pContext -> new TCBoatRenderer(pContext, false));
        EntityRenderers.register(TCEntities.TC_CHEST_BOAT.get(), pContext -> new TCBoatRenderer(pContext, true));

        EntityRenderers.register(TCEntities.FLOW_CEDAR_ENT.get(), FlowCedarEntRenderer::new);
        EntityRenderers.register(TCEntities.ECF_BURST_PROJECTILE.get(), ECFBurstRenderer::new);
        EntityRenderers.register(TCEntities.ECF_BALL_PROJECTILE.get(), ThrownItemRenderer::new);
        EntityRenderers.register(TCEntities.ECF_DROP_PROJECTILE.get(), ThrownItemRenderer::new);
        EntityRenderers.register(TCEntities.ECF_CLOUD.get(), ECFCloudRenderer::new);


        ItemBlockRenderTypes.setRenderLayer(TCFluids.BIRCH_JUICE_FLUID.source.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(TCFluids.BIRCH_JUICE_FLUID.flowing.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(TCFluids.FLOW_FLUID.source.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(TCFluids.FLOW_FLUID.flowing.get(), RenderType.translucent());

        //noinspection removal
        ItemBlockRenderTypes.setRenderLayer(TCBlocks.FLOATING_TORCH_HOLDER.get(),RenderType.cutout());

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
        if (ModList.get().isLoaded("create"))
            TerraCompositio.createCompat.clientInit();
    }
    static final Map<String, Integer> HARDCODED_COLORS = Map.of(
            "create:chocolate", 0x5A3A22,
            "create:honey", 0xEAAE2F,
            "terracompositio:flow_source", 0x0B23BA,
            "terracompositio:birch_juice_source", 0x97872F
    );

    @SubscribeEvent
    public static void onRegisterColorHandlers(RegisterColorHandlersEvent.Block event) {
        event.register((p_92616_, p_92617_, p_92618_, p_92619_) ->
                RedStoneWireBlock.getColorForPower(p_92616_.getValue(RedStoneWireBlock.POWER)),
                TCBlocks.FLOATING_REDSTONE.get());
    }

    @SubscribeEvent
    public static void registerItemColorHandlers(RegisterColorHandlersEvent.Item event) {
        event.register((pStack, pTintIndex) -> {
            if (pTintIndex == 1) {
                Optional<IFluidHandlerItem> fluidHandler = FluidUtil.getFluidHandler(pStack).resolve();
                if (fluidHandler.isPresent()) {
                    FluidStack fluid = fluidHandler.get().getFluidInTank(0);
                    if (!fluid.isEmpty()) {
                        if (fluid.getFluid().isSame(Fluids.LAVA)) {
                            return 0xFF7A00; //0xFF7A00
                        }
                        ResourceLocation id = ForgeRegistries.FLUIDS.getKey(fluid.getFluid());
                        if (id != null) {
                            String string = id.toString();
                            if (HARDCODED_COLORS.containsKey(string)) {
                                return HARDCODED_COLORS.get(string);
                            }
                        }
                        IClientFluidTypeExtensions extensions = IClientFluidTypeExtensions.of(fluid.getFluid());

                        return extensions.getTintColor();
                    }
                }
                return 0x555555;
            }

            return 0xFFFFFF;
        },TCItems.FLUID_APPLIER.get());
    }

    @SubscribeEvent
    public static void registerKeyMapping(RegisterKeyMappingsEvent event) {
        //TCKeyMappings.register(event);
    }

    @SubscribeEvent
    public static void onRegisterGuiOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAbove(VanillaGuiOverlay.HOTBAR.id(), "cfe_hud", ECFHud::render);
        event.registerAbove(VanillaGuiOverlay.HOTBAR.id(), "knowledge_hud", KnowledgeOverlay::render);
    }

    @SubscribeEvent
    public static void registerLayer(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(TCModelLayers.PINE_BOAT_LAYER, BoatModel::createBodyModel);
        event.registerLayerDefinition(TCModelLayers.PINE_CHEST_BOAT_LAYER, ChestBoatModel::createBodyModel);

        event.registerLayerDefinition(TCModelLayers.FLOW_CEDAR_ENT_LAYER, FlowCedarEntModel::createBodyLayer);
        event.registerLayerDefinition(TCModelLayers.FLOW_CEDAR_ENT_STATUE_LAYER, FlowCedarEntStatueModel::createBodyLayer);
        event.registerLayerDefinition(TCModelLayers.ECF_CUBE_LAYER, ECFCubeModel::createBodyLayer);

        event.registerLayerDefinition(TCModelLayers.TECHNETIUM_CROWN_LAYER, TechnetiumCrownModel::createBodyLayer);
        event.registerLayerDefinition(TCModelLayers.TECHNETIUM_CHESTPLATE_LAYER, TechnetiumChestplateModel::createBodyLayer);
        event.registerLayerDefinition(TCModelLayers.TECHNETIUM_BOOTS_LAYER, TechnetiumBootsModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerBER(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(TCBlockEntities.FLOW_ALTAR_BE.get(), FlowCedarAltarBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntities.FLOW_CEDAR_CASING_BE.get(), FlowCedarCasingBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntities.FLOW_INFUSER_BE.get(), FlowInfuserBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntities.MOD_SIGN.get(), SignRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntities.TC_HANGING_SIGN.get(), HangingSignRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntities.CONSTRUCTION_DESORBER_BE.get(), ConstructionDesorberBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntities.CULTIVATION_DESORBER_BE.get(), CultivationDesorberBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntities.TIME_PASSAGE_DESORBER_BE.get(), TimePassageDesorberBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntities.FLOW_CEDAR_TANK_BE.get(), FlowCedarTankBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntities.PATH_POINTER_BE.get(), PathPointerBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntities.ENT_STATUE_BE.get(), EntStatueBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntities.MATTER_INFUSER_IO_BE.get(), MatterInfuserIOBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntities.MATTER_INFUSER_PORT_BE.get(), MatterInfuserPortBlockEntityRenderer::new);


        if (ModList.get().isLoaded("create")) {
            TerraCompositio.createCompat.registerCreateBER(event);
        }
    }

    @SuppressWarnings("deprecation")
    @SubscribeEvent
    public static void registerParticleFactories(final RegisterParticleProvidersEvent event) {
        Minecraft.getInstance().particleEngine.register(TCParticles.FLOW_PARTICLE.get(),
                FlowParticle.Provider::new);
        Minecraft.getInstance().particleEngine.register(TCParticles.CFE_PARTICLE.get(),
                ECFParticle.Provider::new);
        Minecraft.getInstance().particleEngine.register(TCParticles.BIRCH_JUICE_PARTICLE.get(),
                BirchJuiceParticle.Provider::new);
        Minecraft.getInstance().particleEngine.register(TCParticles.FLOW_SPLASH_PARTICLE.get(),
                FlowSplashParticle.Provider::new);
        Minecraft.getInstance().particleEngine.register(TCParticles.BIRCH_JUICE_SPLASH_PARTICLE.get(),
                BirchJuiceSplashParticle.Provider::new);
        Minecraft.getInstance().particleEngine.register(TCParticles.FLUID_FLOW.get(),
                FluidFlowParticle.Provider::new);
    }

    @SubscribeEvent
    public static void onRegisterItemDecorations(RegisterItemDecorationsEvent event) {
        for (Item item : ForgeRegistries.ITEMS) {
            if (item instanceof TechnetiumArmorItem) {
                event.register(item, CFEBarRenderer.INSTANCE);
            }
        }
    }
}


