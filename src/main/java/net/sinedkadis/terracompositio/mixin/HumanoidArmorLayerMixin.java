package net.sinedkadis.terracompositio.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.sinedkadis.terracompositio.item.custom.TechnetiumArmorItem;
import net.sinedkadis.terracompositio.item.models.TechnetiumBootsModel;
import net.sinedkadis.terracompositio.registries.TCItems;
import net.sinedkadis.terracompositio.util.OffsetVConsumer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(HumanoidArmorLayer.class)
public abstract class HumanoidArmorLayerMixin<T extends LivingEntity, M extends HumanoidModel<T>, A extends HumanoidModel<T>> extends RenderLayer<T, M> {
    @Final
    @Shadow
    private TextureAtlas armorTrimAtlas;

    @Shadow protected abstract A getArmorModel(EquipmentSlot pSlot);

    @Shadow protected abstract void renderArmorPiece(PoseStack pPoseStack, MultiBufferSource pBuffer, T pLivingEntity, EquipmentSlot pSlot, int pPackedLight, A pModel);

    public HumanoidArmorLayerMixin(RenderLayerParent<T, M> pRenderer) {
        super(pRenderer);
    }

    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V", at = @At("HEAD"),cancellable = true)
    private  void onRender(PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, T pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch, CallbackInfo ci) {
        A armorModel1 = getArmorModel(EquipmentSlot.FEET);
        if (armorModel1 instanceof TechnetiumBootsModel.Humanoid) return;

        ItemStack itemstack = pLivingEntity.getItemBySlot(EquipmentSlot.FEET);
        @SuppressWarnings("UnstableApiUsage")
        net.minecraft.client.model.Model model = net.minecraftforge.client.ForgeHooksClient.getArmorModel(pLivingEntity, itemstack, EquipmentSlot.FEET, armorModel1);
        if (model instanceof TechnetiumBootsModel.Humanoid armorModel) {
            armorModel.setupAnim(pLivingEntity,pLimbSwing,pLimbSwingAmount,pAgeInTicks,pNetHeadYaw,pHeadPitch);
            this.renderArmorPiece(pPoseStack, pBuffer, pLivingEntity, EquipmentSlot.CHEST, pPackedLight, this.getArmorModel(EquipmentSlot.CHEST));
            this.renderArmorPiece(pPoseStack, pBuffer, pLivingEntity, EquipmentSlot.LEGS, pPackedLight, this.getArmorModel(EquipmentSlot.LEGS));
            //noinspection unchecked
            this.renderArmorPiece(pPoseStack, pBuffer, pLivingEntity, EquipmentSlot.FEET, pPackedLight, (A) armorModel);
            this.renderArmorPiece(pPoseStack, pBuffer, pLivingEntity, EquipmentSlot.HEAD, pPackedLight, this.getArmorModel(EquipmentSlot.HEAD));
            ci.cancel();
        }

    }

    @Shadow
    protected abstract void setPartVisibility(A pModel, EquipmentSlot pSlot);


    @Inject(
            method = "renderArmorPiece(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;ILnet/minecraft/client/model/HumanoidModel;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onRenderArmorPiece(PoseStack pPoseStack,
                                    MultiBufferSource pBuffer,
                                    T pLivingEntity,
                                    EquipmentSlot pSlot,
                                    int pPackedLight,
                                    A pModel,
                                    CallbackInfo ci) {

        ItemStack itemstack = pLivingEntity.getItemBySlot(pSlot);
        if (!itemstack.is(TCItems.TECHNETIUM_CHESTPLATE.get())) return;
        Item item = itemstack.getItem();
        if (item instanceof TechnetiumArmorItem armorItem) {
            if (armorItem.getEquipmentSlot() == pSlot) {
                this.getParentModel().copyPropertiesTo(pModel);
                this.setPartVisibility(pModel, pSlot);
                @SuppressWarnings("UnstableApiUsage")
                Model model = net.minecraftforge.client.ForgeHooksClient.getArmorModel(pLivingEntity, itemstack, pSlot, pModel);

                String armorTexture = armorItem.getArmorTexture(itemstack, pLivingEntity, pSlot, "");
                if (armorTexture == null) return;

                VertexConsumer vertexconsumer = new OffsetVConsumer(pBuffer.getBuffer(RenderType.armorCutoutNoCull(
                        ResourceLocation.parse(armorTexture))), 32f
                );
                model.renderToBuffer(pPoseStack, vertexconsumer, pPackedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1.0F);

                VertexConsumer normal = TechnetiumArmorItem.getTrimVertexConsumer(armorTrimAtlas, pBuffer, pLivingEntity, itemstack, true);
                if (normal == null) return;

                pModel.renderToBuffer(
                        pPoseStack, normal,
                        pPackedLight, OverlayTexture.NO_OVERLAY,
                        1.0F, 1.0F, 1.0F, 1.0F
                );

//                VertexConsumer shifted = TechnetiumArmorItem.getTrimVertexConsumer(armorTrimAtlas,pBuffer, pLivingEntity, itemstack, false);
//                if (shifted == null) return;
//
//                model.renderToBuffer(
//                        pPoseStack, shifted,
//                        pPackedLight, OverlayTexture.NO_OVERLAY,
//                        1.0F, 1.0F, 1.0F, 1.0F
//                );

                // I have some skill issue with this fucking vertex consumers, so no trims on energy shield
                if (itemstack.hasFoil()) {
                    model.renderToBuffer(pPoseStack, pBuffer.getBuffer(RenderType.armorEntityGlint()), pPackedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
                }
                ci.cancel();
            }
        }
    }

}
