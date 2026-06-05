package net.sinedkadis.terracompositio.cfe.burst;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.config.TCInnerConfig;
import net.sinedkadis.terracompositio.util.helpers.ParticleHelper;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@OnlyIn(Dist.CLIENT)
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CFEBurstRenderer extends EntityRenderer<CFEBurstProjectileEntity> {
    private static final float MIN_CAMERA_DISTANCE_SQUARED = 12.25F;

    public CFEBurstRenderer(EntityRendererProvider.Context pContext) {
        super(pContext);
    }

    public void render(CFEBurstProjectileEntity pEntity, float pEntityYaw, float pPartialTicks, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
        int tickCount = pEntity.tickCount;
        if (tickCount >= 1 || !(this.entityRenderDispatcher.camera.getEntity().distanceToSqr(pEntity) < MIN_CAMERA_DISTANCE_SQUARED)) {
            int cfe = pEntity.getCFE();
            int count = TCInnerConfig.RENDER_COUNT_FUNCTION.applyAsInt(cfe);
            Vector3f[] offsets1 = getOffsets(pEntity);
            if (offsets1 == null || offsets1.length < count) genOffsets(pEntity);
            offsets1 = getOffsets(pEntity);
            assert offsets1 != null;
            var renderType = RenderType.entityTranslucentEmissive(getTextureLocation(pEntity));
            var buffer = pBuffer.getBuffer(renderType);

            boolean isEnd = tickCount >= 60;
            float pDelta = (float) (tickCount - 60) / 40f;

            for (int i = 0; i < count; i++) {

                pPoseStack.pushPose();

                var offset = offsets1[i];
                float oX = offset.x();
                float oY = offset.y();
                float oZ = offset.z();

                if (isEnd) {
                    pPoseStack.translate(
                            Mth.lerp(pDelta, oX, -oX * 0.1f),
                            Mth.lerp(pDelta, oY, -oY * 0.1f),
                            Mth.lerp(pDelta, oZ, -oZ * 0.1f)
                    );
                } else {
                    pPoseStack.translate(oX, oY, oZ);
                }
                pPoseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
                pPoseStack.mulPose(Axis.YP.rotationDegrees(180.0F));

                ParticleHelper.drawCfeParticle(pPoseStack, pPackedLight, buffer);

                pPoseStack.popPose();
            }
            super.render(pEntity, pEntityYaw, pPartialTicks, pPoseStack, pBuffer, pPackedLight);
        }
    }

    private void genOffsets(CFEBurstProjectileEntity entity) {
        int cfe = entity.getCFE();
        float count = TCInnerConfig.RENDER_COUNT_FUNCTION.applyAsInt(cfe);
        if (count > 100000) throw new RuntimeException("Particles amount is suspicious large: " + count);
        Vector3f[] offsets1 = getOffsets(entity);
        if (offsets1 == null || offsets1.length < count) {
            offsets1 = new Vector3f[(int) Math.ceil(count)];
            for (int i = 0; i < count; i++) {
                offsets1[i] = ParticleHelper.getSpreadParticleOffset(entity.level().random, (int) (count)).toVector3f();
            }
            setOffsets(entity,offsets1);
        }
    }


    /**
     * Returns the location of an entity's texture.
     */
    public ResourceLocation getTextureLocation(CFEBurstProjectileEntity pEntity) {
        return TerraCompositio.modLoc("textures/particle/cfe_particle.png");
    }

    @Nullable
    public Vector3f[] getOffsets(CFEBurstProjectileEntity pEntity) {
        return pEntity.getOffsets();
    }

    public void setOffsets(CFEBurstProjectileEntity pEntity,Vector3f[] offsets) {
        pEntity.setOffsets(offsets);
    }
}