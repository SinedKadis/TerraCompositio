package net.sinedkadis.terracompositio.events;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
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
import net.minecraftforge.fml.common.Mod;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.block.entity.PathPointerBlockEntity;
import net.sinedkadis.terracompositio.item.custom.WrenchAxeItem;
import net.sinedkadis.terracompositio.particle.CFEParticleData;
import net.sinedkadis.terracompositio.registries.TCEffects;
import net.sinedkadis.terracompositio.registries.TCItems;

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
            renderCustomItem(event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight(), event.getHand());
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

    private static void renderCustomItem(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, InteractionHand hand) {
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
