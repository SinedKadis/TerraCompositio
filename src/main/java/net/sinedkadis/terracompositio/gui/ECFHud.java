package net.sinedkadis.terracompositio.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.api.TCCapabilities;
import net.sinedkadis.terracompositio.api.dummies.DummyECFHandler;
import net.sinedkadis.terracompositio.api.networks.ecf.IECFHandler;
import org.lwjgl.opengl.GL11;

public class ECFHud {
    public static final ResourceLocation ECF_HUD_RL = TerraCompositio.modLoc("textures/gui/ecf_hud.png");
    public static final ResourceLocation ECF_HUD_SHADOW_RL = TerraCompositio.modLoc("textures/gui/ecf_hud_shadow.png");

    public static void render(ForgeGui ignoredGui,
                              GuiGraphics guiGraphics,
                              float ignoredPartialTick,
                              int screenWidth,
                              int screenHeight) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        IECFHandler playerHandler = player.getCapability(TCCapabilities.ECF).orElse(DummyECFHandler.instance).getMainHandler();
        int cfeTotal = playerHandler.getECF();
        int cfeMaxTotal = playerHandler.getMaxECF();

        if (cfeMaxTotal <= 0) return;

        int width = 64;
        int x = screenWidth / 2 + 90;
        int y = screenHeight - 20;

        float width1 = width * ((float) cfeTotal / cfeMaxTotal);
        width = Math.round(width1);

        float saturation = (float) Math.min(1F, Math.sin(Util.getMillis() / 200D) * 0.1 + 1F);
        int color = Mth.hsvToRgb(0.55F, saturation, 1F);
        int r = (color >> 16 & 0xFF);
        int g = (color >> 8 & 0xFF);
        int b = color & 0xFF;

        guiGraphics.blit(ECF_HUD_SHADOW_RL, x + 1, y, 0, 0, 0, 64, 40, 256, 256);

        RenderSystem.setShaderColor(r / 255F, g / 255F, b / 255F, 1 - (r / 255F));

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        guiGraphics.blit(ECF_HUD_RL, x + 1, y, 0, 0, 0, width, 40, 256, 256);

        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1, 1, 1, 1);
    }
}
