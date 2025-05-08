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
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.sinedkadis.terracompositio.block.entity.FlowCedarPortBlockEntity;
import org.jetbrains.annotations.NotNull;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;

public class FlowPortBlockEntityRenderer implements BlockEntityRenderer<FlowCedarPortBlockEntity> {
    public FlowPortBlockEntityRenderer(BlockEntityRendererProvider.Context context) {

    }


    @Override
    public void render(FlowCedarPortBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, @NotNull MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        ItemStack itemStack = pBlockEntity.getRenderStack();
        Direction facing = pBlockEntity.getBlockState().getValue(HORIZONTAL_FACING);
//        ItemRenderer itemRenderer2 = Minecraft.getInstance().getItemRenderer();
//        ItemStack itemStack2 = pBlockEntity.getRenderStack();
//        ItemRenderer itemRenderer3 = Minecraft.getInstance().getItemRenderer();
//        ItemStack itemStack3 = pBlockEntity.getRenderStack();
//        ItemRenderer itemRenderer4 = Minecraft.getInstance().getItemRenderer();
//        ItemStack itemStack4 = pBlockEntity.getRenderStack();


        pPoseStack.pushPose();
        switch (facing){
            case WEST -> pPoseStack.translate(0f, 0.5f, 0.5f);
            case SOUTH -> pPoseStack.translate(0.5f, 0.5f, 1.f);
            case EAST -> pPoseStack.translate(1.f, 0.5f, 0.5f);
            default -> pPoseStack.translate(0.5f, 0.5f, 0f);
        }
        pPoseStack.scale(0.7f, 0.7f, 0.7f);
        pPoseStack.mulPose(Axis.YN.rotationDegrees(switch (facing){
            case EAST -> 90;
            case SOUTH -> 180;
            case WEST -> 270;
            default -> 0;
        }));

        itemRenderer.renderStatic(itemStack, ItemDisplayContext.FIXED, getLightLevel(pBlockEntity.getLevel(), pBlockEntity.getBlockPos(),facing),
                OverlayTexture.NO_OVERLAY, pPoseStack, pBuffer, pBlockEntity.getLevel(), 1);
        pPoseStack.popPose();

//        pPoseStack.pushPose();
//        pPoseStack.translate(0f, 0.5f, 0.5f);
//        pPoseStack.scale(0.7f, 0.7f, 0.7f);
//        pPoseStack.mulPose(Axis.YP.rotationDegrees(90));
//
//        itemRenderer2.renderStatic(itemStack2, ItemDisplayContext.FIXED, getLightLevel(pBlockEntity.getLevel(), pBlockEntity.getBlockPos(),2),
//                OverlayTexture.NO_OVERLAY, pPoseStack, pBuffer, pBlockEntity.getLevel(), 1);
//        pPoseStack.popPose();
//
//        pPoseStack.pushPose();
//        pPoseStack.translate(0.5f, 0.5f, 1.f);
//        pPoseStack.scale(0.7f, 0.7f, 0.7f);
//        pPoseStack.mulPose(Axis.YP.rotationDegrees(180));
//
//        itemRenderer3.renderStatic(itemStack3, ItemDisplayContext.FIXED, getLightLevel(pBlockEntity.getLevel(), pBlockEntity.getBlockPos(),3),
//                OverlayTexture.NO_OVERLAY, pPoseStack, pBuffer, pBlockEntity.getLevel(), 1);
//        pPoseStack.popPose();
//
//        pPoseStack.pushPose();
//        pPoseStack.translate(1.f, 0.5f, 0.5f);
//        pPoseStack.scale(0.7f, 0.7f, 0.7f);
//        pPoseStack.mulPose(Axis.YP.rotationDegrees(270));
//
//        itemRenderer4.renderStatic(itemStack4, ItemDisplayContext.FIXED, getLightLevel(pBlockEntity.getLevel(), pBlockEntity.getBlockPos(),0),
//                OverlayTexture.NO_OVERLAY, pPoseStack, pBuffer, pBlockEntity.getLevel(), 1);
//        pPoseStack.popPose();

    }

    private int getLightLevel(Level level, BlockPos pos, Direction facing) {
        int bLight;
        int sLight = switch (facing) {
            case EAST -> {
                bLight = level.getBrightness(LightLayer.BLOCK, pos.east());
                yield level.getBrightness(LightLayer.SKY, pos.east());
            }
            case NORTH -> {
                bLight = level.getBrightness(LightLayer.BLOCK, pos.north());
                yield level.getBrightness(LightLayer.SKY, pos.north());
            }
            case WEST -> {
                bLight = level.getBrightness(LightLayer.BLOCK, pos.west());
                yield level.getBrightness(LightLayer.SKY, pos.west());
            }
            case SOUTH -> {
                bLight = level.getBrightness(LightLayer.BLOCK, pos.south());
                yield level.getBrightness(LightLayer.SKY, pos.south());
            }
            default -> {
                bLight = level.getBrightness(LightLayer.BLOCK, pos);
                yield level.getBrightness(LightLayer.SKY, pos);
            }
        };

        return LightTexture.pack(bLight, sLight);
    }
}
