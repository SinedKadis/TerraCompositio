package net.sinedkadis.terracompositio.events;

import com.google.common.base.MoreObjects;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fml.common.Mod;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.block.entity.PathPointerBlockEntity;
import net.sinedkadis.terracompositio.item.custom.WrenchAxeItem;
import net.sinedkadis.terracompositio.particle.CFEParticleData;
import net.sinedkadis.terracompositio.registries.TCEffects;
import net.sinedkadis.terracompositio.registries.TCItems;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;

@Mod.EventBusSubscriber(modid = TerraCompositio.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE,value = Dist.CLIENT)
public class ForgeEventBusClientEvents {
    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        ItemStack mainHandStack = player.getMainHandItem();
        ItemStack offHandStack = player.getOffhandItem();

        if (mainHandStack.getItem() instanceof WrenchAxeItem && offHandStack.getItem() instanceof AxeItem && player.getUseItem().is(TCItems.WRENCH_AXE.get())) {
            event.setCanceled(true);
            renderAxes(event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight(), event.getHand());
        }
        if (player.getItemInHand(event.getHand()).is(TCItems.FLUID_APPLIER.get())) {
            renderOriginalBucket(event.getPoseStack(), event.getPackedLight(), event.getHand(),event.getPartialTick());
        }
    }



    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Player player = event.player;
        Level level = player.level();

        // Проверяем, что в руке палка
        if (!player.getMainHandItem().is(TCItems.WRENCH_AXE.get())) return;

        // Получаем блок, на который смотрит игрок
        HitResult hit = Minecraft.getInstance().hitResult;
        if (!(hit instanceof BlockHitResult blockHit)) return;

        BlockPos pos = blockHit.getBlockPos();
        if (pos.distSqr(player.blockPosition()) > 25) return;

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof PathPointerBlockEntity pointer) {
            pointer.highlightNodes();
        }
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (event.getEntity().level() instanceof ClientLevel clientLevel) {
            LivingEntity livingEntity = event.getEntity();
            RandomSource random = clientLevel.getRandom();
            Vec3 pos = livingEntity.position().add(
                    0,
                    Mth.lerp(random.nextFloat(),0, livingEntity.getBbHeight()),
                    0
            );
            if (livingEntity.hasEffect(TCEffects.FLOW_SATURATION.get())) {

                clientLevel.addParticle(new CFEParticleData(1/20f),
                        pos.x,
                        pos.y,
                        pos.z,
                        random.nextFloat(),
                        random.nextFloat(),
                        random.nextFloat());}
        }
    }

    @SubscribeEvent
    public static <M extends EntityModel<LivingEntity> & ArmedModel> void onRenderEntity(RenderLivingEvent<LivingEntity, EntityModel<LivingEntity>> event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null) return;
        LivingEntityRenderer<LivingEntity, EntityModel<LivingEntity>> renderer0 = event.getRenderer();
        if (!(renderer0.getModel() instanceof ArmedModel)) return;
        @SuppressWarnings("unchecked")
        LivingEntityRenderer<LivingEntity, M> renderer = (LivingEntityRenderer<LivingEntity, M>)(Object)renderer0;
        ItemInHandRenderer itemInHandRenderer = minecraft.getEntityRenderDispatcher().getItemInHandRenderer();

        renderer.addLayer(new ItemInHandLayer<>(renderer, itemInHandRenderer){
            @Override
            @ParametersAreNonnullByDefault
            public void render(PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, LivingEntity pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
                boolean flag = pLivingEntity.getMainArm() == HumanoidArm.RIGHT;
                ItemStack itemstack = flag ? pLivingEntity.getOffhandItem() : pLivingEntity.getMainHandItem();
                ItemStack itemstack1 = flag ? pLivingEntity.getMainHandItem() : pLivingEntity.getOffhandItem();
                if (itemstack.is(TCItems.FLUID_APPLIER.get()) || itemstack1.is(TCItems.FLUID_APPLIER.get())) {
                    pPoseStack.pushPose();
                    if (this.getParentModel().young) {
                        pPoseStack.translate(0.0F, 0.75F, 0.0F);
                        pPoseStack.scale(0.5F, 0.5F, 0.5F);
                    }

                    Optional<IFluidHandlerItem> fluidHandler = FluidUtil.getFluidHandler(itemstack1).resolve();
                    if (fluidHandler.isPresent()) {
                        FluidStack fluidStack = fluidHandler.get().getFluidInTank(0);
                        ItemStack toRender = fluidStack.getFluid().getBucket().getDefaultInstance();
                        this.renderArmWithItem(pLivingEntity, toRender, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, HumanoidArm.RIGHT, pPoseStack, pBuffer, pPackedLight);

                    }

                    Optional<IFluidHandlerItem> fluidHandler1 = FluidUtil.getFluidHandler(itemstack).resolve();
                    if (fluidHandler1.isPresent()) {
                        FluidStack fluidStack = fluidHandler1.get().getFluidInTank(0);
                        ItemStack toRender = fluidStack.getFluid().getBucket().getDefaultInstance();
                        this.renderArmWithItem(pLivingEntity, toRender, ItemDisplayContext.THIRD_PERSON_LEFT_HAND, HumanoidArm.LEFT, pPoseStack, pBuffer, pPackedLight);

                    }

                    pPoseStack.popPose();
                }
            }
        });
        renderer.addLayer(new ItemInHandLayer<>(renderer, itemInHandRenderer));
    }

    private static void renderOriginalBucket(PoseStack poseStack, int packedLight, InteractionHand hand, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null) return;

        ItemStack stack = player.getItemInHand(hand);
        if (stack.isEmpty()) return;

        Optional<IFluidHandlerItem> fluidHandler = FluidUtil.getFluidHandler(stack).resolve();
        if (fluidHandler.isPresent()) {
            FluidStack fluidStack = fluidHandler.get().getFluidInTank(0);
            if (fluidStack.isEmpty()) return;
            ItemStack toRender = fluidStack.getFluid().getBucket().getDefaultInstance();
            ItemInHandRenderer itemInHandRenderer = minecraft.getEntityRenderDispatcher().getItemInHandRenderer();

            renderItem(partialTick,poseStack, minecraft.renderBuffers().bufferSource(),player,packedLight,toRender,itemInHandRenderer,hand);

        }


    }

    public static void renderItem(float pPartialTicks, PoseStack pPoseStack, MultiBufferSource.BufferSource pBuffer, LocalPlayer pPlayerEntity, int pCombinedLight,ItemStack item,ItemInHandRenderer renderer,InteractionHand hand) {
        float f = pPlayerEntity.getAttackAnim(pPartialTicks);
        InteractionHand interactionhand = MoreObjects.firstNonNull(pPlayerEntity.swingingArm, InteractionHand.MAIN_HAND);
        float f1 = Mth.lerp(pPartialTicks, pPlayerEntity.xRotO, pPlayerEntity.getXRot());
        ItemInHandRenderer.HandRenderSelection iteminhandrenderer$handrenderselection = ItemInHandRenderer.HandRenderSelection.onlyForHand(hand);
        float f2 = Mth.lerp(pPartialTicks, pPlayerEntity.xBobO, pPlayerEntity.xBob);
        float f3 = Mth.lerp(pPartialTicks, pPlayerEntity.yBobO, pPlayerEntity.yBob);
        pPoseStack.mulPose(Axis.XP.rotationDegrees((pPlayerEntity.getViewXRot(pPartialTicks) - f2) * 0.1F));
        pPoseStack.mulPose(Axis.YP.rotationDegrees((pPlayerEntity.getViewYRot(pPartialTicks) - f3) * 0.1F));
        if (iteminhandrenderer$handrenderselection.renderMainHand) {
            float f4 = interactionhand == InteractionHand.MAIN_HAND ? f : 0.0F;
            float f5 = 1.0F - Mth.lerp(pPartialTicks, renderer.oMainHandHeight, renderer.mainHandHeight);
            renderer.renderArmWithItem(pPlayerEntity, pPartialTicks, f1, InteractionHand.MAIN_HAND, f4, item, f5, pPoseStack, pBuffer, pCombinedLight);
        }

        if (iteminhandrenderer$handrenderselection.renderOffHand) {
            float f6 = interactionhand == InteractionHand.OFF_HAND ? f : 0.0F;
            float f7 = 1.0F - Mth.lerp(pPartialTicks, renderer.oOffHandHeight, renderer.offHandHeight);
            renderer.renderArmWithItem(pPlayerEntity, pPartialTicks, f1, InteractionHand.OFF_HAND, f6, item, f7, pPoseStack, pBuffer, pCombinedLight);
        }

        pBuffer.endBatch();
    }


    private static void renderAxes(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, InteractionHand hand) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) return;

        ItemStack stack = player.getItemInHand(hand);
        if (stack.isEmpty()) return;
        int duration = Math.min(player.getTicksUsingItem(), 60);

        ItemInHandRenderer itemInHandRenderer = minecraft.getEntityRenderDispatcher().getItemInHandRenderer();
        if (hand == InteractionHand.MAIN_HAND) {
            poseStack.pushPose();
            poseStack.translate(0.35F, -0.1F, -0.3F);
            poseStack.mulPose(Axis.ZP.rotationDegrees(180));
            poseStack.mulPose(Axis.XN.rotationDegrees(90 + duration*1.5F));


            itemInHandRenderer.renderItem(player, stack, ItemDisplayContext.FIRST_PERSON_RIGHT_HAND,
                    true, poseStack, bufferSource, packedLight);
            poseStack.popPose();
        } else {
            poseStack.pushPose();
            poseStack.translate(-0.35F, -0.1F, -0.3F);
            poseStack.mulPose(Axis.ZP.rotationDegrees(180));
            poseStack.mulPose(Axis.XN.rotationDegrees(90 + duration*1.5F));

            itemInHandRenderer.renderItem(player, stack, ItemDisplayContext.FIRST_PERSON_LEFT_HAND,
                    false, poseStack, bufferSource, packedLight);
            poseStack.popPose();
        }
    }
}
