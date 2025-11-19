package net.sinedkadis.terracompositio.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.api.dummies.DummyCFEHandler;
import net.sinedkadis.terracompositio.api.networks.cfe.CFECapability;
import net.sinedkadis.terracompositio.api.networks.cfe.ICFEHandler;
import org.lwjgl.opengl.GL11;

public class TCGui {
    public static final ResourceLocation CFE_HUD = TerraCompositio.modLoc("textures/gui/cfe_hud.png");

    public static void cfeHud(ForgeGui gui,
                              GuiGraphics guiGraphics,
                              float partialTick,
                              int screenWidth,
                              int screenHeight) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        Inventory inventory = player.getInventory();
        ICFEHandler playerHandler = player.getCapability(CFECapability.CFE).orElse(DummyCFEHandler.instance);
        int cfeTotal = playerHandler.getCFE();
        int cfeMaxTotal = playerHandler.getMaxCFE();
        for (ItemStack itemStack : inventory.armor) {
            ICFEHandler handler = itemStack.getCapability(CFECapability.CFE).orElse(DummyCFEHandler.instance);
            if (!(handler instanceof DummyCFEHandler)) {
                cfeTotal += handler.getCFE()-1;
                cfeMaxTotal += handler.getMaxCFE()-1;
            }
        }

        int width = 50;
        int x = screenWidth / 2 + 90;
        int y = screenHeight - 15;

        if (cfeMaxTotal == 0) {
            width = 0;
        } else {
            float width1 = width * ( (float) cfeTotal /  cfeMaxTotal);
            width = Math.round(width1);
        }

        if (width == 0) {
            if (cfeTotal > 0) {
                width = 1;
            } else {
                return;
            }
        }

        int color = Mth.hsvToRgb(0.55F, (float) Math.min(1F, Math.sin(Util.getMillis() / 200D) * 0.5 + 1F), 1F);
        int r = (color >> 16 & 0xFF);
        int g = (color >> 8 & 0xFF);
        int b = color & 0xFF;
        RenderSystem.setShaderColor(r / 255F, g / 255F, b / 255F, 1 - (r / 255F));

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        guiGraphics.blit(CFE_HUD, x+10, y, 0, 0, 0, width,40, 256, 256);
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1, 1, 1, 1);
    }
}
