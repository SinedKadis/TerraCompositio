package net.sinedkadis.terracompositio.block.entity.renderer;// Made with Blockbench 4.12.5
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.sinedkadis.terracompositio.block.entity.EntStatueBlockEntity;
import net.sinedkadis.terracompositio.registries.TCItems;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FlowCedarEntStatueModel extends Model implements HeadedModel {
	private final ModelPart ent;
	private final ModelPart head;
	private final ModelPart mini_crown;

    public FlowCedarEntStatueModel(ModelPart root) {
        super(RenderType::entityCutoutNoCull);
        this.ent = root.getChild("ent");
		this.head = this.ent.getChild("head");
		this.mini_crown = this.head.getChild("mini_crown");
		this.ent.getChild("body");
		this.ent.getChild("left_arm");
		this.ent.getChild("right_arm");
		this.ent.getChild("left_leg");
		this.ent.getChild("front_leg");
		this.ent.getChild("back_leg");
//        ModelPart body =      this.ent.getChild("body");
//        ModelPart left_arm =  this.ent.getChild("left_arm");
//        ModelPart right_arm = this.ent.getChild("right_arm");
//        ModelPart left_leg =  this.ent.getChild("left_leg");
//        ModelPart front_leg = this.ent.getChild("front_leg");
//        ModelPart back_leg =  this.ent.getChild("back_leg");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition ent = partdefinition.addOrReplaceChild("ent", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		CubeDeformation pCubeDeformation = new CubeDeformation(0.0F);

		PartDefinition head = ent.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 12).addBox(-2.0F, -3.0F, -2.0F, 4.0F, 3.0F, 4.0F, pCubeDeformation)
				.texOffs(24, 0).addBox(-2.0F, -7.0F, -2.0F, 2.0F, 4.0F, 2.0F, pCubeDeformation), PartPose.offset(0.0F, -14.0F, 0.0F));

		PartDefinition mini_crown = head.addOrReplaceChild("mini_crown", CubeListBuilder.create().texOffs(50, 2).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 1.0F, 1.0F, pCubeDeformation)
				.texOffs(52, 12).addBox(-3.0F, -1.0F, -3.0F, 6.0F, 1.0F, 0.0F, pCubeDeformation)
				.texOffs(46, 2).addBox(-3.0F, -2.0F, -3.0F, 1.0F, 1.0F, 0.0F, pCubeDeformation)
				.texOffs(46, 0).addBox(0.0F, -2.0F, -3.0F, 3.0F, 1.0F, 0.0F, pCubeDeformation)
				.texOffs(52, 1).addBox(-3.0F, -1.0F, 3.0F, 6.0F, 1.0F, 0.0F, pCubeDeformation)
				.texOffs(46, 1).addBox(-1.0F, -2.0F, 3.0F, 3.0F, 1.0F, 0.0F, pCubeDeformation)
				.texOffs(50, 9).addBox(-3.0F, 0.0F, 2.0F, 6.0F, 1.0F, 1.0F, pCubeDeformation)
				.texOffs(54, 13).addBox(2.0F, 0.0F, -2.0F, 1.0F, 1.0F, 4.0F, pCubeDeformation)
				.texOffs(54, 4).addBox(-3.0F, 0.0F, -2.0F, 1.0F, 1.0F, 4.0F, pCubeDeformation), PartPose.offsetAndRotation(-0.75F, -4.0F, -0.5F, 0.1705F, 0.0376F, -0.215F));

        mini_crown.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(46, 4).addBox(-5.0F, -1.0F, -1.0F, 2.0F, 1.0F, 0.0F, pCubeDeformation), PartPose.offsetAndRotation(-2.0F, -1.0F, -6.0F, 0.0F, 1.5708F, 0.0F));

        mini_crown.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(46, 6).addBox(-5.0F, -1.0F, -1.0F, 2.0F, 1.0F, 0.0F, pCubeDeformation)
                .texOffs(52, 0).addBox(-5.0F, 0.0F, -1.0F, 6.0F, 1.0F, 0.0F, pCubeDeformation), PartPose.offsetAndRotation(-2.0F, -1.0F, -2.0F, 0.0F, 1.5708F, 0.0F));

        mini_crown.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(46, 5).addBox(-2.0F, -1.0F, -1.0F, 2.0F, 1.0F, 0.0F, pCubeDeformation), PartPose.offsetAndRotation(4.0F, -1.0F, 1.0F, 0.0F, 1.5708F, 0.0F));

        mini_crown.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(46, 3).addBox(-2.0F, -1.0F, -1.0F, 2.0F, 1.0F, 0.0F, pCubeDeformation)
                .texOffs(52, 11).addBox(-5.0F, 0.0F, -1.0F, 6.0F, 1.0F, 0.0F, pCubeDeformation), PartPose.offsetAndRotation(4.0F, -1.0F, -2.0F, 0.0F, 1.5708F, 0.0F));

        ent.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -8.0F, -2.0F, 4.0F, 8.0F, 4.0F, pCubeDeformation), PartPose.offset(0.0F, -6.0F, 0.0F));

        ent.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(8, 19).addBox(0.0F, -2.0F, -1.0F, 2.0F, 6.0F, 2.0F, pCubeDeformation), PartPose.offset(2.0F, -13.0F, 0.0F));

        ent.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(16, 20).addBox(-2.0F, -1.0F, -1.0F, 2.0F, 6.0F, 2.0F, pCubeDeformation), PartPose.offset(-2.0F, -12.0F, 0.0F));

        ent.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(16, 0).addBox(2.0F, -8.0F, -1.0F, 2.0F, 8.0F, 2.0F, pCubeDeformation), PartPose.offset(0.0F, 0.0F, 0.0F));

        ent.addOrReplaceChild("front_leg", CubeListBuilder.create().texOffs(16, 10).addBox(-1.0F, 0.0F, -2.0F, 2.0F, 8.0F, 2.0F, pCubeDeformation), PartPose.offset(-2.0F, -8.0F, -2.0F));

        ent.addOrReplaceChild("back_leg", CubeListBuilder.create().texOffs(0, 19).addBox(-1.0F, 0.0F, 0.0F, 2.0F, 8.0F, 2.0F, pCubeDeformation), PartPose.offset(-1.0F, -8.0F, 2.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
	}



	public void setupAnim(EntStatueBlockEntity entity) {
		this.root().getAllParts().forEach(ModelPart::resetPose);





		entity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(iItemHandler -> {
			boolean flag = false;
			ItemStack stack = iItemHandler.getStackInSlot(0);
			if (stack.is(TCItems.TECHNETIUM_CROWN.get())){
				flag = true;
			}
			this.mini_crown.visible = flag;
		});

//		this.animateWalk(FlowCedarEntAnimations.WALK, limbSwing, limbSwingAmount, 2f, 2f);
//		this.animate(entity1.idleAnimationState, FlowCedarEntAnimations.IDLE, ageInTicks, 1f);
//		this.animate(entity1.cfeHoldState, FlowCedarEntAnimations.CFE_HOLD, ageInTicks, 1f);
//		this.animate(entity1.extractionAnimationState, FlowCedarEntAnimations.TREE_EXTRACT,ageInTicks,1f);
//		this.animate(entity1.extractionCompleteAnimationState, FlowCedarEntAnimations.EXTRACTION_COMPLETE,ageInTicks,1f);

	}



	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		ent.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);

	}
	public ModelPart root() {
		return ent;
	}

	@Override
	public ModelPart getHead() {
		return head;
	}
}