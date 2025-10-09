package net.sinedkadis.terracompositio.item.models;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.sinedkadis.terracompositio.registries.TCModelLayers;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * @author wiiv
 * thanks bro, your model is cool
 */
@ParametersAreNonnullByDefault
public class TechnetiumCloakModel extends HumanoidModel<LivingEntity> {

    public static TechnetiumCloakModel bakedInstance;

//    private final ModelPart collar;
//    private final ModelPart sideL;
//    private final ModelPart sideR;

    public TechnetiumCloakModel(ModelPart root) {
        super(root);

//        collar = root.getChild("collar");
//        sideL = root.getChild("sideL");
//        sideR = root.getChild("sideR");
    }

    public static void bake(EntityRendererProvider.Context pContext) {
        bakedInstance = new TechnetiumCloakModel(pContext.bakeLayer(TCModelLayers.TECHNETIUM_CLOAK_LAYER));
    }
    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        var deformation = new CubeDeformation(0.01F);

        partdefinition.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        partdefinition.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO);
        partdefinition.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.ZERO);
        partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.ZERO);

        var body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create()
                .addBox(-1.0F, 0.0F, -1.0F, 2, 2, 2, deformation), PartPose.ZERO);
        body.addOrReplaceChild("belt", CubeListBuilder.create().texOffs(0, 65)
                .addBox(-4.5F, 8.0F, -3.0F, 9, 5, 6, deformation), PartPose.ZERO);

        partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 76)
                        .mirror()
                        .addBox(-2.39F, -0.01F, -2.49F, 5, 6, 5, deformation),
                PartPose.offset(1.9F, 12.0F, 0.0F));
        partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 76)
                        .addBox(-2.61F, -0.01F, -2.51F, 5, 6, 5, deformation),
                PartPose.offset(-1.9F, 12.0F, 0.0F));

        PartDefinition cloakPart = body.addOrReplaceChild("cloak", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        cloakPart.addOrReplaceChild("collar", CubeListBuilder.create()
                        .addBox(-5.5F, 0.0F, -1.5F, 11, 5, 11, deformation),
                PartPose.offsetAndRotation(0.0F, -3.0F, -4.5F, 0.0873F, 0.0F, 0.0F));
        cloakPart.addOrReplaceChild("sideL", CubeListBuilder.create().texOffs(0, 16).mirror()
                        .addBox(-0.5F, -0.5F, -5.5F, 11, 21, 10, deformation),
                PartPose.rotation(0.0873F, -0.0873F, -0.1745F));
        cloakPart.addOrReplaceChild("sideR", CubeListBuilder.create().texOffs(0, 16)
                        .addBox(-10.5F, -0.5F, -5.5F, 11, 21, 10, deformation),
                PartPose.rotation(0.0873F, 0.0873F, 0.1745F));
        return LayerDefinition.create(meshdefinition, 64, 64);

    }

    // [VanillaCopy] ArmorStandArmorModel.setupAnim because armor stands are dumb
    // This fixes the armor "breathing" and helmets always facing south on armor stands
    @Override
    public void setupAnim(LivingEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        if (!(entity instanceof ArmorStand entityIn)) {
            super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            return;
        }

//        this.head.xRot = ((float) Math.PI / 180F) * entityIn.getHeadPose().getX();
//        this.head.yRot = ((float) Math.PI / 180F) * entityIn.getHeadPose().getY();
//        this.head.zRot = ((float) Math.PI / 180F) * entityIn.getHeadPose().getZ();
//        this.head.setPos(0.0F, 1.0F, 0.0F);
		this.body.xRot = ((float) Math.PI / 180F) * entityIn.getBodyPose().getX();
		this.body.yRot = ((float) Math.PI / 180F) * entityIn.getBodyPose().getY();
		this.body.zRot = ((float) Math.PI / 180F) * entityIn.getBodyPose().getZ();
//		this.leftArm.xRot = ((float) Math.PI / 180F) * entityIn.getLeftArmPose().getX();
//		this.leftArm.yRot = ((float) Math.PI / 180F) * entityIn.getLeftArmPose().getY();
//		this.leftArm.zRot = ((float) Math.PI / 180F) * entityIn.getLeftArmPose().getZ();
//		this.rightArm.xRot = ((float) Math.PI / 180F) * entityIn.getRightArmPose().getX();
//		this.rightArm.yRot = ((float) Math.PI / 180F) * entityIn.getRightArmPose().getY();
//		this.rightArm.zRot = ((float) Math.PI / 180F) * entityIn.getRightArmPose().getZ();
//		this.leftLeg.xRot = ((float) Math.PI / 180F) * entityIn.getLeftLegPose().getX();
//		this.leftLeg.yRot = ((float) Math.PI / 180F) * entityIn.getLeftLegPose().getY();
//		this.leftLeg.zRot = ((float) Math.PI / 180F) * entityIn.getLeftLegPose().getZ();
//		this.leftLeg.setPos(1.9F, 11.0F, 0.0F);
//		this.rightLeg.xRot = ((float) Math.PI / 180F) * entityIn.getRightLegPose().getX();
//		this.rightLeg.yRot = ((float) Math.PI / 180F) * entityIn.getRightLegPose().getY();
//		this.rightLeg.zRot = ((float) Math.PI / 180F) * entityIn.getRightLegPose().getZ();
//		this.rightLeg.setPos(-1.9F, 11.0F, 0.0F);
//      this.hat.copyFrom(this.head);
    }

    @Override
    public void renderToBuffer(PoseStack ms, VertexConsumer buffer, int light, int overlay, float r, float g, float b, float a) {
//        collar.render(ms, buffer, light, overlay, r, g, b, a);
//        sideL.render(ms, buffer, light, overlay, r, g, b, a);
//        sideR.render(ms, buffer, light, overlay, r, g, b, a);
        setPartVisibility();
        super.renderToBuffer(ms, buffer, light, overlay, r, g, b, a);

    }

    private void setPartVisibility() {
        setAllVisible(false);
        body.visible = true;

    }
}
