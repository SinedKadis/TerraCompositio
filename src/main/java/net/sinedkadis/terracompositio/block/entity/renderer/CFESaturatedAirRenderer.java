package net.sinedkadis.terracompositio.block.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.block.entity.CFESaturatedAirBlockEntity;
import net.sinedkadis.terracompositio.util.TCUtil;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.function.Function;

@OnlyIn(Dist.CLIENT)
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CFESaturatedAirRenderer implements BlockEntityRenderer<CFESaturatedAirBlockEntity> {
    private static final float MIN_CAMERA_DISTANCE_SQUARED = 3.25F;
    private static final int REDUCTION = 1;
    private final BlockEntityRenderDispatcher entityRenderDispatcher;
    private final Function<Level,Vec3> offsetGen = (level) -> new Vec3(level.random.nextFloat()-0.5f,level.random.nextFloat()-0.5f,level.random.nextFloat()-0.5f);

    public CFESaturatedAirRenderer(BlockEntityRendererProvider.Context context) {
        entityRenderDispatcher = context.getBlockEntityRenderDispatcher();
    }

    @Override
    public void render(CFESaturatedAirBlockEntity pEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        List<Vec3> offsets = pEntity.offsets;
        //if (pEntity.getLevel() == null) return;
        if (!offsets.isEmpty())
            genOffsets(pEntity);
        if (!(entityRenderDispatcher.camera.getEntity().getEyePosition()
                .distanceToSqr(pEntity.getPos().getCenter()) <= MIN_CAMERA_DISTANCE_SQUARED)) {
            var renderType = RenderType.entityTranslucentEmissive(getTextureLocation());
            var buffer = pBuffer.getBuffer(renderType);
            int cfe = pEntity.getCfeContainer().getCFE();
            for (int i = 0; i < cfe / REDUCTION; i++) {
                if (i > offsets.size() - 1) offsets.add(offsetGen.apply(entityRenderDispatcher.level));
                Vec3 vec3 = offsets.get(i);

                pPoseStack.pushPose();

                pPoseStack.translate(vec3.x, vec3.y, vec3.z);
                pPoseStack.mulPose(entityRenderDispatcher.camera.rotation());
                pPoseStack.mulPose(Axis.YP.rotationDegrees(180.0F));

                TCUtil.drawCfeParticle(pPoseStack, pPackedLight, buffer);

                pPoseStack.popPose();
            }
        }
    }


    private void genOffsets(CFESaturatedAirBlockEntity entity) {
        List<Vec3> particlePlace = entity.offsets;
        int cfe = entity.getCfeContainer().getCFE();
        Level level = entityRenderDispatcher.level;
        //if (level == null) return;
        for (int i = 0; i < cfe/REDUCTION; i++) {


            //Vector3f offsetGen = TCUtil.getSpreadParticleOffset(level.random, cfe / REDUCTION).toVector3f();


            particlePlace.add(offsetGen.apply(level));
        }
    }


    /**
     * Returns the location of an entity's texture.
     */
    public ResourceLocation getTextureLocation() {
        return TerraCompositio.modLoc("textures/particle/cfe_particle.png");
    }
}