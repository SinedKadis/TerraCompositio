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
public class TechnetiumChestplateModel extends HumanoidModel<LivingEntity> {

    public static TechnetiumChestplateModel bakedInstance;

//    private final ModelPart collar;
//    private final ModelPart sideL;
//    private final ModelPart sideR;

    public TechnetiumChestplateModel(ModelPart root) {
        super(root);

//        collar = root.getChild("collar");
//        sideL = root.getChild("sideL");
//        sideR = root.getChild("sideR");
    }

    public static void bake(EntityRendererProvider.Context pContext) {
        bakedInstance = new TechnetiumChestplateModel(pContext.bakeLayer(TCModelLayers.TECHNETIUM_CHESTPLATE_LAYER));
    }
    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        var deformation = new CubeDeformation(0.01F);

        partdefinition.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        partdefinition.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO);
        partdefinition.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(48, 0).mirror().addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.75F)).mirror(false), PartPose.offset(5.0F, 2.0F, 0.0F));

        partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(48, 0).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.75F)), PartPose.offset(-5.0F, 2.0F, 0.0F));

        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(40, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.75F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 76)
                        .mirror()
                        .addBox(-2.39F, -0.01F, -2.49F, 5, 6, 5, deformation),
                PartPose.offset(1.9F, 12.0F, 0.0F));
        partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 76)
                        .addBox(-2.61F, -0.01F, -2.51F, 5, 6, 5, deformation),
                PartPose.offset(-1.9F, 12.0F, 0.0F));

        PartDefinition cloak = body.addOrReplaceChild("cloak", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        cloak.addOrReplaceChild("left",
                CubeListBuilder.create().texOffs(0, 35).addBox(0.0F, -2.0F, -6.0F, 12.0F, 17.0F, 12.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.1745F));

        cloak.addOrReplaceChild("right",
                CubeListBuilder.create().texOffs(0, 35).mirror().addBox(-12.0F, -2.0F, -6.0F, 12.0F, 17.0F, 12.0F, new CubeDeformation(0.0F)).mirror(false),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.1745F));

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
        leftArm.visible = true;
        rightArm.visible = true;
    }
}
