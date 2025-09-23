package net.sinedkadis.terracompositio.item.models;
// Made with Blockbench 4.12.6
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.sinedkadis.terracompositio.registries.TCModelLayers;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TechnetiumCrownModel extends HumanoidModel<LivingEntity> {

	public static TechnetiumCrownModel bakedInstance;

	public TechnetiumCrownModel(ModelPart root) {
        super(root);
    }

	public static void bake(EntityRendererProvider.Context pContext) {
		bakedInstance = new TechnetiumCrownModel(pContext.bakeLayer(TCModelLayers.TECHNETIUM_CROWN_LAYER));
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

		PartDefinition armorHead = partdefinition.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offset(0.0F, -8.0F, 0.0F));

        armorHead.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(16, 28).addBox(-5.0F, -5.0F, 5.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(12, 28).addBox(-5.0F, -5.0F, 3.0F, 0.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 26).addBox(-5.0F, -5.0F, -2.0F, 0.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(22, 24).addBox(5.0F, -5.0F, -2.0F, 0.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(14, 26).addBox(0.0F, -5.0F, 5.0F, 4.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(6, 26).addBox(1.0F, -5.0F, -5.0F, 4.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(6, 28).addBox(-5.0F, -5.0F, -5.0F, 3.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 11).addBox(-5.0F, -3.0F, -5.0F, 0.0F, 1.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(5.0F, -3.0F, -5.0F, 0.0F, 1.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(22, 23).addBox(-5.0F, -3.0F, 5.0F, 10.0F, 1.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(20, 10).addBox(-5.0F, -2.0F, -4.0F, 1.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(20, 0).addBox(4.0F, -2.0F, -4.0F, 1.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(0, 23).addBox(-5.0F, -2.0F, 4.0F, 10.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(20, 20).addBox(-5.0F, -2.0F, -5.0F, 10.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 22).addBox(-5.0F, -3.0F, -5.0F, 10.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -7.0F, 0.0F, 0.0F, (float) (Math.PI/2f), -0.0873F));
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

		this.head.xRot = ((float) Math.PI / 180F) * entityIn.getHeadPose().getX();
		this.head.yRot = ((float) Math.PI / 180F) * entityIn.getHeadPose().getY();
		this.head.zRot = ((float) Math.PI / 180F) * entityIn.getHeadPose().getZ();
		this.head.setPos(0.0F, 1.0F, 0.0F);
//		this.body.xRot = ((float) Math.PI / 180F) * entityIn.getBodyPose().getX();
//		this.body.yRot = ((float) Math.PI / 180F) * entityIn.getBodyPose().getY();
//		this.body.zRot = ((float) Math.PI / 180F) * entityIn.getBodyPose().getZ();
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
		this.hat.copyFrom(this.head);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		setPartVisibility();
		super.renderToBuffer(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
	// [VanillaCopy] HumanoidArmorLayer
	private void setPartVisibility() {
		setAllVisible(false);
		head.visible = true;
		hat.visible = true;

	}
}