package net.sinedkadis.terracompositio.block.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.level.block.state.BlockState;
import net.sinedkadis.terracompositio.block.entity.PathPointerBlockEntity;
import net.sinedkadis.terracompositio.registries.TCBlocks;
import org.joml.Quaternionf;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;

@ParametersAreNonnullByDefault
public class PathPointerBlockEntityRenderer implements BlockEntityRenderer<PathPointerBlockEntity> {
    private final Map<PathPointerBlockEntity.PPPart, BakedModel> modelMap;

    public PathPointerBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        BlockRenderDispatcher blockRenderer = context.getBlockRenderDispatcher();
        this.modelMap = Map.of(
                PathPointerBlockEntity.PPPart.RECEIVER, blockRenderer.getBlockModel(TCBlocks.PP_RECEIVER.get().defaultBlockState()),
                PathPointerBlockEntity.PPPart.COLLECTOR, blockRenderer.getBlockModel(TCBlocks.PP_COLLECTOR.get().defaultBlockState()),
                PathPointerBlockEntity.PPPart.SENDER, blockRenderer.getBlockModel(TCBlocks.PP_SENDER.get().defaultBlockState()),
                PathPointerBlockEntity.PPPart.EMITTER, blockRenderer.getBlockModel(TCBlocks.PP_EMITTER.get().defaultBlockState()),
                PathPointerBlockEntity.PPPart.INFUSER, blockRenderer.getBlockModel(TCBlocks.PP_INFUSER.get().defaultBlockState()),
                PathPointerBlockEntity.PPPart.EXTRACTOR, blockRenderer.getBlockModel(TCBlocks.PP_EXTRACTOR.get().defaultBlockState())
        );
    }

    @Override
    public void render(PathPointerBlockEntity blockEntity, float partialTicks, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        BlockState state = blockEntity.getBlockState();


        PathPointerBlockEntity.PPPart basePart = blockEntity.parts.get(0);
        PathPointerBlockEntity.PPPart additionalPart = blockEntity.parts.get(1);

        renderModel(poseStack, bufferSource, state, modelMap.get(basePart), packedLight, packedOverlay,
                blockEntity.rotationYaw, blockEntity.rotationPitch, blockEntity.rotationRoll, partialTicks);


        if (!additionalPart.equals(PathPointerBlockEntity.PPPart.NONE)) {
            renderModel(poseStack, bufferSource, state, modelMap.get(additionalPart), packedLight, packedOverlay,
                    blockEntity.rotationYaw, blockEntity.rotationPitch, blockEntity.rotationRoll, partialTicks);
        }
    }

    public static float toRadians(float degrees) {
        return (float) (degrees / 180F * Math.PI);
    }

    private void renderModel(PoseStack poseStack, MultiBufferSource bufferSource, BlockState state,
                             BakedModel model, int packedLight, int packedOverlay,
                              float rotationY, float rotationX, float rotationZ, float partialTicks) {
        poseStack.pushPose();

        double time = Minecraft.getInstance().level != null ?
                Minecraft.getInstance().level.getGameTime() + partialTicks : 0;

        poseStack.translate(0.5, 0.5, 0.5);

        poseStack.translate(0, Math.sin(time / 20f) * 0.05f, 0);


        poseStack.mulPose(new Quaternionf().rotateY(toRadians(rotationY))); // yaw
        poseStack.mulPose(new Quaternionf().rotateX(toRadians(-rotationX)));       // pitch
        poseStack.mulPose(new Quaternionf().rotateZ(toRadians(-rotationZ)));       // roll


        poseStack.translate(-0.5, -0.5, -0.5);


        VertexConsumer buffer = bufferSource.getBuffer(RenderType.cutout());

        //noinspection deprecation
        Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(
                poseStack.last(),
                buffer,
                state,
                model,
                1, 1, 1,
                packedLight,
                packedOverlay
        );

        poseStack.popPose();
    }
}
