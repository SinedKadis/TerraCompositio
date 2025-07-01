package net.sinedkadis.terracompositio.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.entity.custom.FlowCedarEntEntity;
import net.sinedkadis.terracompositio.registries.TCModelLayers;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class FlowCedarEntRenderer extends MobRenderer<FlowCedarEntEntity,FlowCedarEntModel<FlowCedarEntEntity>> {

    private static final ResourceLocation ENT_TEXTURE = TerraCompositio.modLoc("textures/entity/flow_cedar_ent.png");
    private static final ResourceLocation CUBE_TEXTURE = TerraCompositio.modLoc("textures/entity/cfe_cube.png");
    private final CFECubeModel<FlowCedarEntEntity> cfeCubeModel;

    public FlowCedarEntRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, new FlowCedarEntModel<>(pContext.bakeLayer(TCModelLayers.FLOW_CEDAR_ENT_LAYER)), 0.5f);
        this.cfeCubeModel = new CFECubeModel<>(pContext.bakeLayer(TCModelLayers.CFE_CUBE_LAYER));
    }

    @Override
    public ResourceLocation getTextureLocation(FlowCedarEntEntity pEntity) {
        return ENT_TEXTURE;
    }

    @Override
    public void render(FlowCedarEntEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);

        int energy = entity.getSyncedCFE();
        if (energy > 0) {
            energy = Math.min(energy,1000);
            float scale = 0.5f + (energy / (float) entity.getLimit()) * 0.1f;
            poseStack.pushPose();

            float yOffset = entity.getBbHeight() + scale * 0.2f;
            poseStack.translate(0.0D, yOffset, 0.0D);
            poseStack.scale(scale,scale,scale);


            this.cfeCubeModel.setupAnim(entity, 0, 0, entity.tickCount + partialTicks, 0, 0);
            this.cfeCubeModel.renderToBuffer(
                    poseStack,
                    buffer.getBuffer(RenderType.entityTranslucent(CUBE_TEXTURE)),
                    packedLight,
                    getOverlayCoords(entity, 0.0F),
                    1.0F, 1.0F, 1.0F, 0.8F
            );

            poseStack.popPose();
        }
    }
}
