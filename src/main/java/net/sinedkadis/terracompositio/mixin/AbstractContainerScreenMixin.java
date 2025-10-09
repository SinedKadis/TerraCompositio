package net.sinedkadis.terracompositio.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
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

@Mixin(AbstractContainerScreen.class)
public class AbstractContainerScreenMixin extends Screen {


    @Shadow
    private ItemStack draggingItem;

    protected AbstractContainerScreenMixin(Component pTitle) {
        super(pTitle);
    }

    @Inject(
            method = "renderFloatingItem(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V",
            at = @At("HEAD")
    )
    private void onRenderItemHead(GuiGraphics pGuiGraphics, ItemStack pStack, int pX, int pY, String pText, CallbackInfo ci) {
        if (pStack.is(TCItems.FLUID_APPLIER.get())) {
            Optional<IFluidHandlerItem> fluidHandler = FluidUtil.getFluidHandler(pStack).resolve();
            if (fluidHandler.isPresent()) {
                FluidStack fluidStack = fluidHandler.get().getFluidInTank(0);
                if (fluidStack.isEmpty()) return;
                ItemStack toRender = fluidStack.getFluid().getBucket().getDefaultInstance();
                pGuiGraphics.pose().pushPose();
                pGuiGraphics.pose().translate(0.0F, 0.0F, 232.0F);
                pGuiGraphics.renderItem(toRender, pX, pY);
                var font = net.minecraftforge.client.extensions.common.IClientItemExtensions.of(pStack).getFont(pStack, net.minecraftforge.client.extensions.common.IClientItemExtensions.FontContext.ITEM_COUNT);
                pGuiGraphics.renderItemDecorations(font == null ? this.font : font, pStack, pX, pY - (this.draggingItem.isEmpty() ? 0 : 8), pText);
                pGuiGraphics.renderItemDecorations(Minecraft.getInstance().font,toRender,pX,pY, String.valueOf((fluidStack.getAmount()/1000f)));
                pGuiGraphics.pose().popPose();
            }
        }
    }
}
