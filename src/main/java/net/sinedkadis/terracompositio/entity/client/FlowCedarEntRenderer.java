package net.sinedkadis.terracompositio.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.api.networks.ecf.IECFHandler;
import net.sinedkadis.terracompositio.api.registries.TCCapabilities;
import net.sinedkadis.terracompositio.entity.custom.FlowCedarEntEntity;
import net.sinedkadis.terracompositio.item.models.TechnetiumBootsModel;
import net.sinedkadis.terracompositio.item.models.TechnetiumChestplateModel;
import net.sinedkadis.terracompositio.item.models.TechnetiumCrownModel;
import net.sinedkadis.terracompositio.registries.TCModelLayers;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class FlowCedarEntRenderer extends MobRenderer<FlowCedarEntEntity,FlowCedarEntModel<FlowCedarEntEntity>> {

    private static final ResourceLocation ENT_TEXTURE = TerraCompositio.modLoc("textures/entity/flow_cedar_ent.png");
    private static final ResourceLocation CUBE_TEXTURE = TerraCompositio.modLoc("textures/entity/ecf_cube.png");
    private final ECFCubeModel<FlowCedarEntEntity> ECFCubeModel;

    public FlowCedarEntRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, new FlowCedarEntModel<>(pContext.bakeLayer(TCModelLayers.FLOW_CEDAR_ENT_LAYER)), 0.5f);
        this.ECFCubeModel = new ECFCubeModel<>(pContext.bakeLayer(TCModelLayers.ECF_CUBE_LAYER));

        bakeHomeless(pContext);
    }

    private static void bakeHomeless(EntityRendererProvider.Context pContext) {
        //Please, forge, I need this!
        //My models are kinda rendererless
        TechnetiumCrownModel.bake(pContext);
        TechnetiumChestplateModel.bake(pContext);
        TechnetiumBootsModel.Humanoid.bake(pContext);
    }

    @Override
    public ResourceLocation getTextureLocation(FlowCedarEntEntity pEntity) {
        return ENT_TEXTURE;
    }

    @Override
    public void render(FlowCedarEntEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);


        int energy = entity.getSyncedECF();
        Optional<IECFHandler> icfeHandler = entity.getCapability(TCCapabilities.ECF).resolve();
        if (energy > 0 && icfeHandler.isPresent()) {
            float alpha = 0.8f;
            alpha += Mth.map(energy,1000,10000,0,0.2f);


            float scale = (0.1f + (energy / (float) icfeHandler.get().getMaxECF())) * 10;
            poseStack.pushPose();

            float yOffset = entity.getBbHeight() + scale * 0.2f;
            poseStack.translate(0.0D, yOffset, 0.0D);
            poseStack.scale(scale,scale,scale);


            this.ECFCubeModel.setupAnim(entity, 0, 0, entity.tickCount + partialTicks, 0, 0);
            this.ECFCubeModel.renderToBuffer(
                    poseStack,
                    buffer.getBuffer(RenderType.entityTranslucent(CUBE_TEXTURE)),
                    packedLight,
                    getOverlayCoords(entity, 0.0F),
                    1.0F, 1.0F, 1.0F, alpha
            );

            poseStack.popPose();
        }
    }
}
