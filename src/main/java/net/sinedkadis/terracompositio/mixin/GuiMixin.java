package net.sinedkadis.terracompositio.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(Gui.class)
public class GuiMixin {

    @Inject(
            method = "renderSlot(Lnet/minecraft/client/gui/GuiGraphics;IIFLnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;I)V",
            at = @At("HEAD")
    )
    private void beforeRenderItem(GuiGraphics pGuiGraphics, int pX, int pY, float pPartialTick, Player pPlayer, ItemStack pStack, int pSeed, CallbackInfo ci) {
        if (!pStack.isEmpty()) {
            Optional<IFluidHandlerItem> fluidHandler = FluidUtil.getFluidHandler(pStack).resolve();
            if (fluidHandler.isPresent()) {
                float f = (float)pStack.getPopTime() - pPartialTick;
                if (f > 0.0F) {
                    float f1 = 1.0F + f / 5.0F;
                    pGuiGraphics.pose().pushPose();
                    pGuiGraphics.pose().translate((float)(pX + 8), (float)(pY + 12), 0.0F);
                    pGuiGraphics.pose().scale(1.0F / f1, (f1 + 1.0F) / 2.0F, 1.0F);
                    pGuiGraphics.pose().translate((float)(-(pX + 8)), (float)(-(pY + 12)), 0.0F);
                }
                FluidStack fluidStack = fluidHandler.get().getFluidInTank(0);
                if (fluidStack.isEmpty()) return;
                ItemStack toRender = fluidStack.getFluid().getBucket().getDefaultInstance();
                pGuiGraphics.renderItem(pPlayer, toRender, pX, pY, pSeed);
                pGuiGraphics.renderItemDecorations(Minecraft.getInstance().font,toRender,pX,pY, String.valueOf((fluidStack.getAmount()/1000f)));
                if (f > 0.0F) {
                    pGuiGraphics.pose().popPose();
                }

                //pGuiGraphics.renderItemDecorations(this.minecraft.font, pStack, pX, pY);
            }
        }
    }
}
