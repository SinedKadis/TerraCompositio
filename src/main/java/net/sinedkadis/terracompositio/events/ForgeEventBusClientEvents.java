package net.sinedkadis.terracompositio.events;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHandEvent;
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
            renderOriginalBucket(event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight(), event.getHand(),event.getPartialTick());
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

    private static void renderOriginalBucket(PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight, InteractionHand hand, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null) return;

        ItemStack stack = player.getItemInHand(hand);
        if (stack.isEmpty()) return;

        Optional<IFluidHandlerItem> fluidHandler = FluidUtil.getFluidHandler(stack).resolve();
        if (fluidHandler.isPresent()) {
            FluidStack fluidStack = fluidHandler.get().getFluidInTank(0);
            ItemStack toRender = fluidStack.getFluid().getBucket().getDefaultInstance();
            ItemInHandRenderer itemInHandRenderer = minecraft.getEntityRenderDispatcher().getItemInHandRenderer();


            poseStack.pushPose();
            player.setItemInHand(hand,toRender);
            GameRenderer gameRenderer = minecraft.gameRenderer;
            Camera camera = gameRenderer.getMainCamera();
            poseStack.mulPose(Axis.XP.rotationDegrees(camera.getXRot()));
            poseStack.mulPose(Axis.YP.rotationDegrees(camera.getYRot()));

            itemInHandRenderer.renderHandsWithItems(partialTick,poseStack, minecraft.renderBuffers().bufferSource(),player,minecraft.getEntityRenderDispatcher().getPackedLightCoords(player, partialTick));
            player.setItemInHand(hand,stack);
            poseStack.popPose();


//            poseStack.pushPose();
//
//
//            GameRenderer gameRenderer = minecraft.gameRenderer;
//            Camera camera = gameRenderer.getMainCamera();
////
////            poseStack.mulPose(Axis.XP.rotationDegrees(camera.getXRot()));
////            poseStack.mulPose(Axis.YP.rotationDegrees(camera.getYRot()));
////            poseStack.mulPose(Axis.XP.rotationDegrees(camera.getXRot()));
//            //poseStack.mulPose(Axis.YP.rotationDegrees(camera.getYRot()));
//            //poseStack.mulPose(Axis.YP.rotationDegrees(camera.getYRot() + 180.0F));
//
//
//            if (minecraft.getCameraEntity() instanceof LivingEntity livingentity) {
//                float f = (float)livingentity.hurtTime - partialTick;
//                if (livingentity.isDeadOrDying()) {
//                    float f1 = Math.min((float)livingentity.deathTime + partialTick, 20.0F);
//                    poseStack.mulPose(Axis.ZP.rotationDegrees(40.0F - 8000.0F / (f1 + 200.0F)));
//                }
//
////                if (f < 0.0F) {
////                    return;
////                }
//
//                f /= (float)livingentity.hurtDuration;
//                f = Mth.sin(f * f * f * f * (float)Math.PI);
//                float f3 = livingentity.getHurtDir();
//                poseStack.mulPose(Axis.YP.rotationDegrees(-f3));
//                float f2 = (float)((double)(-f) * 14.0D * minecraft.options.damageTiltStrength().get());
//                poseStack.mulPose(Axis.ZP.rotationDegrees(f2));
//                poseStack.mulPose(Axis.YP.rotationDegrees(f3));
//            }
//            if (minecraft.options.bobView().get()) {
//                if (minecraft.getCameraEntity() instanceof Player player1) {
//                    float f = player1.walkDist - player1.walkDistO;
//                    float f1 = -(player1.walkDist + f * partialTick);
//                    float f2 = Mth.lerp(partialTick, player1.oBob, player1.bob);
//                    poseStack.translate(Mth.sin(f1 * (float)Math.PI) * f2 * 0.5F, -Math.abs(Mth.cos(f1 * (float)Math.PI) * f2), 0.0F);
//                    poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.sin(f1 * (float)Math.PI) * f2 * 3.0F));
//                    poseStack.mulPose(Axis.XP.rotationDegrees(Math.abs(Mth.cos(f1 * (float)Math.PI - 0.2F) * f2) * 5.0F));
//                }
//            }
//            float f2 = Mth.lerp(partialTick, player.xBobO, player.xBob);
//            float f3 = Mth.lerp(partialTick, player.yBobO, player.yBob);
//            poseStack.mulPose(Axis.XP.rotationDegrees((player.getViewXRot(partialTick) - f2) * 0.1F));
//            poseStack.mulPose(Axis.YP.rotationDegrees((player.getViewYRot(partialTick) - f3) * 0.1F));
//
//
//            int i = hand == InteractionHand.MAIN_HAND ? 1 : -1;
//            poseStack.translate((float)i * 0.56F, -0.52F + 1 * -0.6F, -0.72F);
//
//            poseStack.translate(0,0.4f,-0.4f);
//
//            poseStack.translate((float)i * -0.4785682F, -0.094387F, 0.05731531F);
//            poseStack.mulPose(Axis.XP.rotationDegrees(-11.935F));
//            poseStack.mulPose(Axis.YP.rotationDegrees((float)i * 65.3F));
//            poseStack.mulPose(Axis.ZP.rotationDegrees((float)i * -9.785F));
//            float f9 = (float)toRender.getUseDuration() - ((float)minecraft.player.getUseItemRemainingTicks() - partialTick + 1.0F);
//            float f13 = f9 / (float) CrossbowItem.getChargeDuration(toRender);
//            if (f13 > 1.0F) {
//                f13 = 1.0F;
//            }
//
//            if (f13 > 0.1F) {
//                float f16 = Mth.sin((f9 - 0.1F) * 1.3F);
//                float f10 = f13 - 0.1F;
//                float f4 = f16 * f10;
//                poseStack.translate(f4 * 0.0F, f4 * 0.004F, f4 * 0.0F);
//            }
//
//            poseStack.translate(f13 * 0.0F, f13 * 0.0F, f13 * 0.04F);
//            poseStack.scale(1.0F, 1.0F, 1.0F + f13 * 0.2F);
//            poseStack.mulPose(Axis.YN.rotationDegrees((float)i * 45.0F));
//
//            if (hand.equals(InteractionHand.MAIN_HAND)) {
//                itemInHandRenderer.renderItem(player, toRender, ItemDisplayContext.FIRST_PERSON_RIGHT_HAND,
//                        false, poseStack, multiBufferSource, packedLight);
//            } else {
//                itemInHandRenderer.renderItem(player, toRender, ItemDisplayContext.FIRST_PERSON_LEFT_HAND,
//                        true, poseStack, multiBufferSource, packedLight);
//            }
//            poseStack.popPose();
        }


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
