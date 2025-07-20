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
import net.sinedkadis.terracompositio.block.custom.PathPointerBlock;
import net.sinedkadis.terracompositio.block.entity.PathPointerBlockEntity;
import net.sinedkadis.terracompositio.registries.TCBlockStateProperties;
import net.sinedkadis.terracompositio.registries.TCBlocks;
import net.sinedkadis.terracompositio.util.VecHelper;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;

@ParametersAreNonnullByDefault
public class PathPointerBlockEntityRenderer implements BlockEntityRenderer<PathPointerBlockEntity> {
    private final Map<PathPointerBlock.PPPart, BakedModel> modelMap;

    public PathPointerBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        BlockRenderDispatcher blockRenderer = context.getBlockRenderDispatcher();
        this.modelMap = Map.of(
                PathPointerBlock.PPPart.RECEIVER, blockRenderer.getBlockModel(TCBlocks.PP_RECEIVER.get().defaultBlockState()),
                PathPointerBlock.PPPart.COLLECTOR, blockRenderer.getBlockModel(TCBlocks.PP_COLLECTOR.get().defaultBlockState()),
                PathPointerBlock.PPPart.SENDER, blockRenderer.getBlockModel(TCBlocks.PP_SENDER.get().defaultBlockState()),
                PathPointerBlock.PPPart.EMITTER, blockRenderer.getBlockModel(TCBlocks.PP_EMITTER.get().defaultBlockState())
        );
    }

    @Override
    public void render(@NotNull PathPointerBlockEntity blockEntity, float partialTicks, PoseStack poseStack,
                       @NotNull MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        BlockState state = blockEntity.getBlockState();


        PathPointerBlock.PPPart basePart = state.getValue(TCBlockStateProperties.BASE_PART);
        PathPointerBlock.PPPart additionalPart = state.getValue(TCBlockStateProperties.ADDITIONAL_PART);

        renderModel(poseStack, bufferSource, state, modelMap.get(basePart), packedLight, packedOverlay,
                blockEntity.rotationYaw, blockEntity.rotationPitch, blockEntity.rotationRoll, partialTicks);


        if (!additionalPart.equals(PathPointerBlock.PPPart.NONE)) {
            renderModel(poseStack, bufferSource, state, modelMap.get(additionalPart), packedLight, packedOverlay,
                    blockEntity.rotationYaw, blockEntity.rotationPitch, blockEntity.rotationRoll, partialTicks);
        }
    }

    private void renderModel(PoseStack poseStack, MultiBufferSource bufferSource, BlockState state,
                             BakedModel model, int packedLight, int packedOverlay,
                              float rotationY, float rotationX, float rotationZ, float partialTicks) {
        poseStack.pushPose();

        double time = Minecraft.getInstance().level != null ?
                Minecraft.getInstance().level.getGameTime() + partialTicks : 0;

        poseStack.translate(0.5, 0.5, 0.5);

        poseStack.translate(0, Math.sin(time / 20f) * 0.05f, 0);
        //poseStack.mulPose(VecHelper.rotateY(-rotationY + 90F)); // yaw
        poseStack.mulPose(VecHelper.rotateY(rotationY)); // yaw
        poseStack.mulPose(VecHelper.rotateX(-rotationX));       // pitch
        poseStack.mulPose(VecHelper.rotateZ(-rotationZ));       // roll


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
