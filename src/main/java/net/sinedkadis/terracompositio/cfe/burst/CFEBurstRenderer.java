package net.sinedkadis.terracompositio.cfe.burst;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.util.TCUtil;
import org.joml.Vector3f;

import javax.annotation.ParametersAreNonnullByDefault;

@OnlyIn(Dist.CLIENT)
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CFEBurstRenderer extends EntityRenderer<CFEBurstProjectileEntity> {
    private static final float MIN_CAMERA_DISTANCE_SQUARED = 12.25F;
    private static final int REDUCTION = 10;
    private final String x = "x";
    private final String y = "y";
    private final String z = "z";

    public CFEBurstRenderer(EntityRendererProvider.Context pContext) {
        super(pContext);
    }

    public void render(CFEBurstProjectileEntity pEntity, float pEntityYaw, float pPartialTicks, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
        CompoundTag persistentData = pEntity.getPersistentData();
        if (!persistentData.contains("offsets")) genOffsets(pEntity);
        int tickCount = pEntity.tickCount;
        if (tickCount >= 1 || !(this.entityRenderDispatcher.camera.getEntity().distanceToSqr(pEntity) < MIN_CAMERA_DISTANCE_SQUARED)) {
            CompoundTag offsets = (CompoundTag) persistentData.get("offsets");
            assert offsets != null;
            var renderType = RenderType.entityTranslucentEmissive(getTextureLocation(pEntity));
            var buffer = pBuffer.getBuffer(renderType);
            int cfe = pEntity.getCFE();
            boolean isEnd = tickCount >= 60;
            float pDelta = (float) (tickCount-60)/40f;
            for (int i = 0; i < cfe/REDUCTION; i++) {
                CompoundTag offset = (CompoundTag) offsets.get(String.valueOf(i));
                assert offset != null;

                pPoseStack.pushPose();

                float oX = offset.getFloat(x);
                float oY = offset.getFloat(y);
                float oZ = offset.getFloat(z);

                if (isEnd) {
                    pPoseStack.translate(
                            Mth.lerp(pDelta,oX,-oX*0.1f),
                            Mth.lerp(pDelta,oY,-oY*0.1f),
                            Mth.lerp(pDelta,oZ,-oZ*0.1f)
                    );
                } else {
                    pPoseStack.translate(oX, oY, oZ);
                }
                pPoseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
                pPoseStack.mulPose(Axis.YP.rotationDegrees(180.0F));

                TCUtil.drawCfeParticle(pPoseStack, pPackedLight, buffer);

                pPoseStack.popPose();
            }
            super.render(pEntity, pEntityYaw, pPartialTicks, pPoseStack, pBuffer, pPackedLight);
        }
    }

    private void genOffsets(CFEBurstProjectileEntity entity) {
        CompoundTag offsets = new CompoundTag();
        int cfe = entity.getCFE();
        for (int i = 0; i < cfe/REDUCTION; i++) {
            CompoundTag offset = new CompoundTag();
            Vector3f offsetGen = TCUtil.getSpreadParticleOffset(entity.level().random, cfe / REDUCTION).toVector3f();
            offset.putFloat(x,offsetGen.x);
            offset.putFloat(y,offsetGen.y);
            offset.putFloat(z,offsetGen.z);
            offsets.put(String.valueOf(i),offset);
        }
        entity.getPersistentData().put("offsets",offsets);
    }


    /**
     * Returns the location of an entity's texture.
     */
    public ResourceLocation getTextureLocation(CFEBurstProjectileEntity pEntity) {
        return TerraCompositio.modLoc("textures/particle/cfe_particle.png");
    }
}