package net.sinedkadis.terracompositio.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.sinedkadis.terracompositio.registries.TCItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(ItemEntityRenderer.class)
public abstract class ItemEntityRendererMixin {

    @Shadow
    public abstract void render(ItemEntity pEntity, float pEntityYaw, float pPartialTicks, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight);

    @Inject(
            method = "render(Lnet/minecraft/world/entity/item/ItemEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("HEAD")
    )
    private void beforeRenderItem(ItemEntity pEntity, float pEntityYaw, float pPartialTicks, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, CallbackInfo ci) {
        ItemStack pItemStack = pEntity.getItem();
        if (pItemStack.is(TCItems.FLUID_APPLIER.get())) {
            Optional<IFluidHandlerItem> fluidHandler = FluidUtil.getFluidHandler(pItemStack).resolve();
            if (fluidHandler.isPresent()) {
                FluidStack fluidStack = fluidHandler.get().getFluidInTank(0);
                if (fluidStack.isEmpty()) return;
                ItemStack toRender = fluidStack.getFluid().getBucket().getDefaultInstance();
                ItemEntity pEntity1 = new ItemEntity(pEntity);
                pEntity1.setItem(toRender);
                this.render(pEntity1,pEntityYaw,pPartialTicks,pPoseStack,pBuffer,pPackedLight);
            }
        }
    }
}
