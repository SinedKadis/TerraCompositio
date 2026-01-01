package net.sinedkadis.terracompositio.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.sinedkadis.terracompositio.TerraCompositio;

import net.sinedkadis.terracompositio.entity.custom.CFECloudEntity;
import net.sinedkadis.terracompositio.util.TCUtil;
import org.joml.Vector3f;

import javax.annotation.ParametersAreNonnullByDefault;

@OnlyIn(Dist.CLIENT)
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CFECloudRenderer extends EntityRenderer<CFECloudEntity> {
    //private static final float MIN_CAMERA_DISTANCE_SQUARED = 3.25F;
    private static final int REDUCTION = 10;
    private final String x = "x";
    private final String y = "y";
    private final String z = "z";

    public CFECloudRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(CFECloudEntity pEntity, float pEntityYaw, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
        super.render(pEntity, pEntityYaw, pPartialTick, pPoseStack, pBuffer, pPackedLight);
        CompoundTag persistentData = pEntity.getPersistentData();
        if (!persistentData.contains("offsets")) genOffsets(pEntity);

        CompoundTag offsets = (CompoundTag) persistentData.get("offsets");
        assert offsets != null;
        var renderType = RenderType.entityTranslucentEmissive(getTextureLocation(pEntity));
        var buffer = pBuffer.getBuffer(renderType);
        int cfe = pEntity.getSyncedCFE();
        float k = (float) Math.log10(cfe);
        for (int i = 0; i < cfe / REDUCTION; i++) {
            CompoundTag offset = (CompoundTag) offsets.get(String.valueOf(i));
            assert offset != null;
            float oX = offset.getFloat(x);
            float oY = offset.getFloat(y);
            float oZ = offset.getFloat(z);
            pPoseStack.pushPose();
            pPoseStack.translate(oX*k, oY*k, oZ*k);
            pPoseStack.mulPose(entityRenderDispatcher.camera.rotation());
            pPoseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            TCUtil.drawCfeParticle(pPoseStack, pPackedLight, buffer);
            pPoseStack.popPose();
        }

    }




    private void genOffsets(CFECloudEntity entity) {
        CompoundTag offsets = new CompoundTag();
        int cfe = entity.getSyncedCFE();
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
    @Override
    public ResourceLocation getTextureLocation(CFECloudEntity pEntity) {
        return TerraCompositio.modLoc("textures/particle/cfe_particle.png");
    }
}