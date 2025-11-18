package net.sinedkadis.terracompositio.item.models;
// Made with Blockbench 5.0.3
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.LivingEntity;
import net.sinedkadis.terracompositio.item.models.amimations.TechnetiumArmorAnimations;
import net.sinedkadis.terracompositio.util.LivingEntityAnimationAccessor;
import net.sinedkadis.terracompositio.registries.TCModelLayers;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TechnetiumBootsModel extends HierarchicalModel<LivingEntity> {

    private final ModelPart root;

    public TechnetiumBootsModel(ModelPart root) {
        this.root = root;
    }

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();
		CubeDeformation pCubeDeformation = new CubeDeformation(1F);
		float pYOffset = 0;
		partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, pCubeDeformation), PartPose.offset(0.0F, 0.0F + pYOffset, 0.0F));
		partdefinition.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, pCubeDeformation.extend(0.5F)), PartPose.offset(0.0F, 0.0F + pYOffset, 0.0F));
		partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, pCubeDeformation), PartPose.offset(0.0F, 0.0F + pYOffset, 0.0F));
		partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, pCubeDeformation), PartPose.offset(-5.0F, 2.0F + pYOffset, 0.0F));
		partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(40, 16).mirror().addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, pCubeDeformation), PartPose.offset(5.0F, 2.0F + pYOffset, 0.0F));
		PartDefinition right_leg = partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, pCubeDeformation), PartPose.offset(-1.9F, 12.0F + pYOffset, 0.0F));
		PartDefinition left_leg = partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, pCubeDeformation), PartPose.offset(1.9F, 12.0F + pYOffset, 0.0F));



//		PartDefinition left_shoe = left_leg.addOrReplaceChild("left_shoe", CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.75F)).mirror(false), PartPose.offset(1.9F, 12.0F, 0.0F));
//		PartDefinition left_wing = left_shoe.addOrReplaceChild("left_wing", CubeListBuilder.create().texOffs(-1, -6).addBox(0.0F, -1.0F, -1.0F, 0.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0F, 10.0F, 0.0F, 0.6109F, 0.4363F, 0.0F));
//		PartDefinition right_shoe = right_leg.addOrReplaceChild("right_shoe", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.75F)), PartPose.offset(-1.9F, 12.0F, 0.0F));
//		PartDefinition right_wing = right_shoe.addOrReplaceChild("right_wing", CubeListBuilder.create().texOffs(-1, -6).addBox(0.0F, -1.0F, -1.0F, 0.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, 10.0F, 0.0F, 0.6109F, -0.4363F, 0.0F));

		PartDefinition left_shoe = left_leg.addOrReplaceChild("left_shoe", CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, pCubeDeformation).mirror(false), PartPose.offset(0F, 0F, 0.0F));
		left_shoe.addOrReplaceChild("left_wing", CubeListBuilder.create().texOffs(-1, -6).addBox(0.0F, -1.0F, -1.0F, 0.0F, 3.0F, 6.0F, new CubeDeformation(0.01f)), PartPose.offsetAndRotation(3.0F, 10.0F, 0.0F, 0.6109F, 0.4363F, 0.0F));

		PartDefinition right_shoe = right_leg.addOrReplaceChild("right_shoe", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, pCubeDeformation), PartPose.offset(0F, 0F, 0.0F));
		right_shoe.addOrReplaceChild("right_wing", CubeListBuilder.create().texOffs(-1, -6).addBox(0.0F, -1.0F, -1.0F, 0.0F, 3.0F, 6.0F, new CubeDeformation(0.01f)), PartPose.offsetAndRotation(-3.0F, 10.0F, 0.0F, 0.6109F, -0.4363F, 0.0F));

		return LayerDefinition.create(meshdefinition, 64, 32);
	}

	@Override
	public void setupAnim(LivingEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		AnimationState pAnimationState = ((LivingEntityAnimationAccessor) entity).terraCompositio$getIdleAnimationState();
		this.animate(pAnimationState,
				TechnetiumArmorAnimations.BOOTS_IDLE, ageInTicks, 1f);
	}

	@Override
	public void renderToBuffer(PoseStack ms, VertexConsumer buffer, int light, int overlay, float r, float g, float b, float a) {
		super.renderToBuffer(ms, buffer, light, overlay, r, g, b, a);

	}

	@Override
	public ModelPart root() {
		return root;
	}

	public static class Humanoid extends HumanoidModel<LivingEntity> {

		public static Humanoid bakedInstance;

		public static void bake(EntityRendererProvider.Context pContext) {
			bakedInstance = new Humanoid(new TechnetiumBootsModel(pContext.bakeLayer(TCModelLayers.TECHNETIUM_BOOTS_LAYER)));
		}

        private final TechnetiumBootsModel model;

        public Humanoid(TechnetiumBootsModel model) {
			super(model.root);
            this.model = model;

        }

		@Override
		public void setupAnim(LivingEntity pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
			super.setupAnim(pEntity, pLimbSwing, pLimbSwingAmount, pAgeInTicks, pNetHeadYaw, pHeadPitch);
			model.setupAnim(pEntity, pLimbSwing, pLimbSwingAmount, pAgeInTicks, pNetHeadYaw, pHeadPitch);
		}

		@Override
		public void renderToBuffer(PoseStack pPoseStack, VertexConsumer pBuffer, int pPackedLight, int pPackedOverlay, float pRed, float pGreen, float pBlue, float pAlpha) {
			setPartVisibility();
			model.renderToBuffer(pPoseStack, pBuffer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
		}
		private void setPartVisibility() {
			setAllVisible(false);
			leftLeg.visible = true;
			rightLeg.visible = true;

		}
	}

}