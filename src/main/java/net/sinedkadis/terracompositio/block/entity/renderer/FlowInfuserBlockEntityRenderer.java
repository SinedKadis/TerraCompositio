package net.sinedkadis.terracompositio.block.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.sinedkadis.terracompositio.block.entity.FlowInfuserBlockEntity;
import org.jetbrains.annotations.NotNull;

public class FlowInfuserBlockEntityRenderer implements BlockEntityRenderer<FlowInfuserBlockEntity> {
    public FlowInfuserBlockEntityRenderer(BlockEntityRendererProvider.Context ignoredContext) {

    }


    @Override
    public void render(FlowInfuserBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, @NotNull MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        ItemStack itemStack = pBlockEntity.getRenderStack();


        pPoseStack.pushPose();
        pPoseStack.translate(0.5f, 0.45f, 0.5f);
        pPoseStack.scale(0.7f, 0.7f, 0.7f);
        pPoseStack.mulPose(Axis.XN.rotationDegrees(90));

        Level level = pBlockEntity.getLevel();
        itemRenderer.renderStatic(itemStack, ItemDisplayContext.FIXED, getLightLevel(level, pBlockEntity.getBlockPos()),
                OverlayTexture.NO_OVERLAY, pPoseStack, pBuffer, level, 1);
        pPoseStack.popPose();
    }

    private int getLightLevel(Level level, BlockPos pos) {
        if (level != null) {
            int bLight = level.getBrightness(LightLayer.BLOCK, pos);
            int sLight = level.getBrightness(LightLayer.SKY, pos);
            return LightTexture.pack(bLight, sLight);
        }
        return 1;
    }
}
