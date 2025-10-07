package net.sinedkadis.terracompositio.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(GuiGraphics.class)
public class GuiGraphicsMixin {

    @Inject(
            method = "renderItem(Lnet/minecraft/world/item/ItemStack;III)V",
            at = @At("HEAD")
    )
    private void onRenderItemHead(ItemStack pStack, int pX, int pY, int pSeed, CallbackInfo ci) {
        if (!pStack.isEmpty()) {
            Optional<IFluidHandlerItem> fluidHandler = FluidUtil.getFluidHandler(pStack).resolve();
            if (fluidHandler.isPresent()) {
                FluidStack fluidStack = fluidHandler.get().getFluidInTank(0);
                if (fluidStack.isEmpty()) return;
                ItemStack toRender = fluidStack.getFluid().getBucket().getDefaultInstance();
                GuiGraphics guiGraphics = (GuiGraphics) (Object) this;
                guiGraphics.renderItem(Minecraft.getInstance().player, Minecraft.getInstance().level, toRender, pX, pY, pSeed);
                guiGraphics.renderItemDecorations(Minecraft.getInstance().font,toRender,pX,pY, String.valueOf((fluidStack.getAmount()/1000f)));

            }
        }
    }
}
