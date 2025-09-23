package net.sinedkadis.terracompositio.block.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.block.entity.EntStatueBlockEntity;
import net.sinedkadis.terracompositio.registries.TCModelLayers;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class EntStatueBlockEntityRenderer implements BlockEntityRenderer<EntStatueBlockEntity> {
    private static final ResourceLocation RESOURCE_LOCATION = TerraCompositio.modLoc("textures/block/flow_cedar_ent_statue.png");

    private final FlowCedarEntStatueModel statueModel;

    public EntStatueBlockEntityRenderer(BlockEntityRendererProvider.Context context) {

        this.statueModel = new FlowCedarEntStatueModel(context.bakeLayer(TCModelLayers.FLOW_CEDAR_ENT_STATUE_LAYER));
    }

    @Override
    public void render(@NotNull EntStatueBlockEntity blockEntity, float partialTicks, PoseStack poseStack,
                       @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay) {

        poseStack.pushPose();


        //poseStack.translate(0.5, 0.5, 0.5);


        poseStack.translate(0.5, 1.5, 0.5);
        poseStack.mulPose(Axis.XP.rotationDegrees(180));


        this.statueModel.setupAnim(blockEntity);
        this.statueModel.renderToBuffer(
                poseStack,
                buffer.getBuffer(RenderType.entityCutout(RESOURCE_LOCATION)),
                packedLight,
                packedOverlay,
                1.0F, 1.0F, 1.0F, 1.0f
        );

        poseStack.popPose();
    }

}
