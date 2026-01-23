package net.sinedkadis.terracompositio.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.sinedkadis.terracompositio.TerraCompositio;

import net.sinedkadis.terracompositio.entity.custom.CFECloudEntity;
import net.sinedkadis.terracompositio.util.TCConfig;
import net.sinedkadis.terracompositio.util.TCUtil;
import org.joml.Vector3f;

import javax.annotation.ParametersAreNonnullByDefault;

@OnlyIn(Dist.CLIENT)
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CFECloudRenderer extends EntityRenderer<CFECloudEntity> {
    private Vector3f[] offsets;


    public CFECloudRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(CFECloudEntity pEntity, float pEntityYaw, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
        super.render(pEntity, pEntityYaw, pPartialTick, pPoseStack, pBuffer, pPackedLight);
        var renderType = RenderType.entityCutout(getTextureLocation(pEntity));
        var buffer = pBuffer.getBuffer(renderType);
        int cfe = pEntity.getSyncedCFE();
        float k = (float) Math.log10(cfe);
        int count = TCConfig.RENDER_COUNT_FUNCTION.applyAsInt(cfe);
        if (offsets == null || offsets.length < count) genOffsets(pEntity);
        pPoseStack.pushPose();
        var frustum = Minecraft.getInstance().levelRenderer.getFrustum();
        float size = 0.1f;
        for (int i = 0; i < count; i++) {
            var offset = offsets[i];
            float oX = offset.x();
            float oY = offset.y();
            float oZ = offset.z();

            AABB aabb = new AABB(
                    pEntity.getX() + oX*k - size,
                    pEntity.getY() + oY*k - size,
                    pEntity.getZ() + oZ*k - size,
                    pEntity.getX() + oX*k + size,
                    pEntity.getY() + oY*k + size,
                    pEntity.getZ() + oZ*k + size
            );

            if (!frustum.isVisible(aabb)) continue;


            pPoseStack.pushPose();
            pPoseStack.translate(oX*k, oY*k, oZ*k);
            pPoseStack.mulPose(entityRenderDispatcher.camera.rotation());
            pPoseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            TCUtil.drawCfeParticle(pPoseStack, pPackedLight, buffer);
            pPoseStack.popPose();
        }
        pPoseStack.popPose();

    }




    private void genOffsets(CFECloudEntity entity) {
        int cfe = entity.getSyncedCFE();
        float count = TCConfig.RENDER_COUNT_FUNCTION.applyAsInt(cfe);
        if (offsets == null || offsets.length < count) {
            offsets = new Vector3f[(int) Math.ceil(count)];
            for (int i = 0; i < count; i++) {
                offsets[i] = TCUtil.getSpreadParticleOffset(entity.level().random, (int) (count)).toVector3f();
            }
        }
    }


    /**
     * Returns the location of an entity's texture.
     */
    @Override
    public ResourceLocation getTextureLocation(CFECloudEntity pEntity) {
        return TerraCompositio.modLoc("textures/particle/cfe_particle.png");
    }
}