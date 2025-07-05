package net.sinedkadis.terracompositio.entity.client;// Made with Blockbench 4.12.5
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.sinedkadis.terracompositio.entity.animations.FlowCedarEntAnimations;
import net.sinedkadis.terracompositio.entity.custom.FlowCedarEntEntity;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FlowCedarEntModel<T extends Entity> extends HierarchicalModel<T> {
	private final ModelPart ent;
	private final ModelPart head;
	private final ModelPart body;
	private final ModelPart left_arm;
	private final ModelPart right_arm;
	private final ModelPart left_leg;
	private final ModelPart front_leg;
	private final ModelPart back_leg;

	public FlowCedarEntModel(ModelPart root) {
		this.ent = root.getChild("ent");
		this.head = this.ent.getChild("head");
		this.body = this.ent.getChild("body");
		this.left_arm = this.ent.getChild("left_arm");
		this.right_arm = this.ent.getChild("right_arm");
		this.left_leg = this.ent.getChild("left_leg");
		this.front_leg = this.ent.getChild("front_leg");
		this.back_leg = this.ent.getChild("back_leg");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition ent = partdefinition.addOrReplaceChild("ent", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition head = ent.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 12).addBox(-2.0F, -3.0F, -2.0F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(24, 0).addBox(-2.0F, -7.0F, -2.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -14.0F, 0.0F));

		PartDefinition body = ent.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -8.0F, -2.0F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -6.0F, 0.0F));

		PartDefinition left_arm = ent.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(8, 19).addBox(0.0F, -2.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(2.0F, -13.0F, 0.0F));

		PartDefinition right_arm = ent.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(16, 20).addBox(-2.0F, -1.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.0F, -12.0F, 0.0F));

		PartDefinition left_leg = ent.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(16, 0).addBox(2.0F, -8.0F, -1.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition front_leg = ent.addOrReplaceChild("front_leg", CubeListBuilder.create().texOffs(16, 10).addBox(-1.0F, 0.0F, -2.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.0F, -8.0F, -2.0F));

		PartDefinition back_leg = ent.addOrReplaceChild("back_leg", CubeListBuilder.create().texOffs(0, 19).addBox(-1.0F, 0.0F, 0.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.0F, -8.0F, 2.0F));

		return LayerDefinition.create(meshdefinition, 32, 32);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.root().getAllParts().forEach(ModelPart::resetPose);
		this.applyHeadRotation(netHeadYaw, headPitch, ageInTicks);

		FlowCedarEntEntity entity1 = (FlowCedarEntEntity) entity;

		this.animateWalk(FlowCedarEntAnimations.WALK, limbSwing, limbSwingAmount, 2f, 2f);
		this.animate(entity1.idleAnimationState, FlowCedarEntAnimations.IDLE, ageInTicks, 1f);
		this.animate(entity1.cfeHoldState, FlowCedarEntAnimations.CFE_HOLD, ageInTicks, 1f);
		this.animate(entity1.extractionAnimationState, FlowCedarEntAnimations.TREE_EXTRACT,ageInTicks,1f);
		this.animate(entity1.extractionCompleteAnimationState, FlowCedarEntAnimations.EXTRACTION_COMPLETE,ageInTicks,1f);

	}

	private void applyHeadRotation(float pNetHeadYaw, float pHeadPitch, float pAgeInTicks) {
		pNetHeadYaw = Mth.clamp(pNetHeadYaw, -30.0F, 30.0F);
		pHeadPitch = Mth.clamp(pHeadPitch, -25.0F, 45.0F);

		this.head.yRot = pNetHeadYaw * ((float)Math.PI / 180F);
		this.head.xRot = pHeadPitch * ((float)Math.PI / 180F);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		ent.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);

	}

	@Override
	public ModelPart root() {
		return ent;
	}
}