package net.sinedkadis.terracompositio.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Axis;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.common.MinecraftForge;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.api.IHaveKnowledge;
import net.sinedkadis.terracompositio.api.TCCapabilities;
import net.sinedkadis.terracompositio.api.dummies.DummyCFEHandler;
import net.sinedkadis.terracompositio.api.networks.cfe.ICFEHandler;
import net.sinedkadis.terracompositio.config.TCClientConfigs;
import net.sinedkadis.terracompositio.registries.TCItems;
import net.sinedkadis.terracompositio.util.PlayerKnowledgeAccessor;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class TCGui {
    public static final ResourceLocation CFE_HUD = TerraCompositio.modLoc("textures/gui/cfe_hud.png");

    public static void cfeHud(ForgeGui ignoredGui,
                              GuiGraphics guiGraphics,
                              float ignoredPartialTick,
                              int screenWidth,
                              int screenHeight) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        Inventory inventory = player.getInventory();
        ICFEHandler playerHandler = player.getCapability(TCCapabilities.CFE).orElse(DummyCFEHandler.instance);
        int cfeTotal = playerHandler.getCFE();
        int cfeMaxTotal = playerHandler.getMaxCFE();
        for (ItemStack itemStack : inventory.armor) {
            ICFEHandler handler = itemStack.getCapability(TCCapabilities.CFE).orElse(DummyCFEHandler.instance);
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



    public static int hoverTicks = 0;
    public static BlockPos lastHovered = null;

    public static void knowledgeOverlay(ForgeGui ignoredGui, GuiGraphics graphics, float partialTicks, int width,
                                        int height) {

        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || mc.gameMode == null || mc.gameMode.getPlayerMode() == GameType.SPECTATOR)
            return;

        HitResult objectMouseOver = mc.hitResult;
        if (!(objectMouseOver instanceof BlockHitResult result)) {
            lastHovered = null;
            hoverTicks = 0;
            return;
        }

        ClientLevel level = mc.level;
        if (level == null) return;
        
        BlockPos pos = result.getBlockPos();

        hoverTicks++;
        lastHovered = pos;

        BlockEntity be = level.getBlockEntity(pos);
        LocalPlayer player = mc.player;
        if (player == null) return;

        boolean hasCreationKnowledge = ((PlayerKnowledgeAccessor) player).isCreationAcknowledged();
        boolean isShifting = player.isShiftKeyDown();

        boolean hasKnowledgeInformation = be instanceof IHaveKnowledge;

        ItemStack item = TCItems.APPLE_OF_KNOWLEDGE.get().getDefaultInstance();
        List<Component> tooltip = new ArrayList<>();

        if (hasKnowledgeInformation && hasCreationKnowledge) {
            IHaveKnowledge ihk = (IHaveKnowledge) be;
            ihk.addToKnowledgeTooltip(tooltip, isShifting);
        }

        if (tooltip.isEmpty()) {
            hoverTicks = 0;
            return;
        }

        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        int tooltipTextWidth = 0;
        for (FormattedText textLine : tooltip) {
            int textLineWidth = mc.font.width(textLine);
            if (textLineWidth > tooltipTextWidth)
                tooltipTextWidth = textLineWidth;
        }
        int tooltipHeight = 8;
        if (tooltip.size() > 1) {
            tooltipHeight += 2; // gap between title lines and next lines
            tooltipHeight += (tooltip.size() - 1) * 10;
        }

        int posX = width / 2 + TCClientConfigs.OVERLAY_X_OFFSET.get() - tooltipTextWidth;
        int posY = height / 2 + TCClientConfigs.OVERLAY_Y_OFFSET.get();

        posX = Math.min(posX, width - tooltipTextWidth - 20);
        posY = Math.min(posY, height - tooltipHeight - 20);

        float fade = Mth.clamp((hoverTicks + partialTicks) / 24f, 0, 1);

        int colorBackground = 0xf0_100010;
        int colorBorderTop = 0x50_5000ff;
        int colorBorderBot = 0x50_28007f;

        if (fade < 1) {
            poseStack.translate(Math.pow(1 - fade, 3) * Math.signum(TCClientConfigs.OVERLAY_X_OFFSET.get() - .5f) * 8, 0, 0);
            colorBackground = scaleAlpha(colorBackground, fade);
            colorBorderTop = scaleAlpha(colorBorderTop, fade);
            colorBorderBot = scaleAlpha(colorBorderBot, fade);
        }
        int itemRenderX = posX + tooltipTextWidth + 15;
        int itemRenderY = posY - 16;

        poseStack.translate(itemRenderX, itemRenderY, 1000);
        poseStack.mulPose(Axis.ZP.rotationDegrees(180));
        poseStack.mulPose(Axis.YP.rotationDegrees(180));
        renderItemIntoGUI(poseStack, item, false);
        poseStack.mulPose(Axis.YN.rotationDegrees(180));
        poseStack.mulPose(Axis.ZN.rotationDegrees(180));
        poseStack.translate(-itemRenderX, -itemRenderY, -1000);

        drawHoveringText(item, graphics, tooltip, posX, posY, width, height, -1, colorBackground,
                colorBorderTop, colorBorderBot, mc.font);


        poseStack.popPose();


    }

    public static int scaleAlpha(int color, float scale) {
        int alpha = (color >>> 24) & 0xFF;
        alpha = Math.round(alpha * scale);

        return (color & 0x00FFFFFF) | (alpha << 24);
    }

    public static void renderItemIntoGUI(PoseStack poseStack, ItemStack stack, boolean useDefaultLighting) {
        ItemRenderer renderer = Minecraft.getInstance().getItemRenderer();
        BakedModel bakedModel = renderer.getModel(stack, null, null, 0);

        renderer.textureManager.getTexture(InventoryMenu.BLOCK_ATLAS).setFilter(false, false);
        RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
        RenderSystem.enableBlend();
        RenderSystem.enableCull();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.pushPose();
        poseStack.translate(0, 0, 500.0F);
        poseStack.translate(8.0F, -8.0F, 0.0F);
        poseStack.scale(16.0F, 16.0F, 16.0F);
        MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
        boolean flatLighting = !bakedModel.usesBlockLight();
        if (useDefaultLighting && flatLighting) {
            Lighting.setupForFlatItems();
        }

        renderer.render(stack, ItemDisplayContext.GUI, false, poseStack, buffer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, bakedModel);
        RenderSystem.disableDepthTest();
        buffer.endBatch();

        RenderSystem.enableDepthTest();
        if (useDefaultLighting && flatLighting) {
            Lighting.setupFor3DItems();
        }

        poseStack.popPose();
    }

    public static void drawHoveringText(@Nonnull final ItemStack stack, GuiGraphics graphics,
                                        List<? extends FormattedText> textLines, int mouseX, int mouseY, int screenWidth, int screenHeight,
                                        int maxTextWidth, int backgroundColor, int borderColorStart, int borderColorEnd, Font font) {
        if (textLines.isEmpty())
            return;

        @SuppressWarnings("UnstableApiUsage")
        List<ClientTooltipComponent> list = ForgeHooksClient.gatherTooltipComponents(stack, textLines,
                stack.getTooltipImage(), mouseX, screenWidth, screenHeight, font);
        @SuppressWarnings({"UnstableApiUsage", "DataFlowIssue"})
        RenderTooltipEvent.Pre event =
                new RenderTooltipEvent.Pre(stack, graphics, mouseX, mouseY, screenWidth, screenHeight, font, list, null);
        if (MinecraftForge.EVENT_BUS.post(event))
            return;

        PoseStack pStack = graphics.pose();

        mouseX = event.getX();
        mouseY = event.getY();
        screenWidth = event.getScreenWidth();
        screenHeight = event.getScreenHeight();
        font = event.getFont();

        // RenderSystem.disableRescaleNormal();
        RenderSystem.disableDepthTest();
        int tooltipTextWidth = 0;

        for (FormattedText textLine : textLines) {
            int textLineWidth = font.width(textLine);
            if (textLineWidth > tooltipTextWidth)
                tooltipTextWidth = textLineWidth;
        }

        tooltipTextWidth += 15;

        boolean needsWrap = false;

        int titleLinesCount = 1;
        int tooltipX = mouseX + 12;
        if (tooltipX + tooltipTextWidth + 4 > screenWidth) {
            tooltipX = mouseX - 16 - tooltipTextWidth;
            if (tooltipX < 4) // if the tooltip doesn't fit on the screen
            {
                if (mouseX > screenWidth / 2)
                    tooltipTextWidth = mouseX - 12 - 8;
                else
                    tooltipTextWidth = screenWidth - 16 - mouseX;
                needsWrap = true;
            }
        }

        if (maxTextWidth > 0 && tooltipTextWidth > maxTextWidth) {
            tooltipTextWidth = maxTextWidth;
            needsWrap = true;
        }

        if (needsWrap) {
            int wrappedTooltipWidth = 0;
            List<FormattedText> wrappedTextLines = new ArrayList<>();
            for (int i = 0; i < textLines.size(); i++) {
                FormattedText textLine = textLines.get(i);
                List<FormattedText> wrappedLine = font.getSplitter()
                        .splitLines(textLine, tooltipTextWidth, Style.EMPTY);
                if (i == 0)
                    titleLinesCount = wrappedLine.size();

                for (FormattedText line : wrappedLine) {
                    int lineWidth = font.width(line);
                    if (lineWidth > wrappedTooltipWidth)
                        wrappedTooltipWidth = lineWidth;
                    wrappedTextLines.add(line);
                }
            }
            tooltipTextWidth = wrappedTooltipWidth;
            textLines = wrappedTextLines;

            if (mouseX > screenWidth / 2)
                tooltipX = mouseX - 16 - tooltipTextWidth;
            else
                tooltipX = mouseX + 12;
        }

        int tooltipY = mouseY - 12;
        int tooltipHeight = 8;

        if (textLines.size() > 1) {
            tooltipHeight += (textLines.size() - 1) * 10;
            if (textLines.size() > titleLinesCount)
                tooltipHeight += 2; // gap between title lines and next lines
        }

        if (tooltipY < 4)
            tooltipY = 4;
        else if (tooltipY + tooltipHeight + 4 > screenHeight)
            tooltipY = screenHeight - tooltipHeight - 4;

        final int zLevel = 400;
        @SuppressWarnings("UnstableApiUsage")
        RenderTooltipEvent.Color colorEvent = new RenderTooltipEvent.Color(stack, graphics, tooltipX, tooltipY,
                font, backgroundColor, borderColorStart, borderColorEnd, list);
        MinecraftForge.EVENT_BUS.post(colorEvent);
        backgroundColor = colorEvent.getBackgroundStart();
        borderColorStart = colorEvent.getBorderStart();
        borderColorEnd = colorEvent.getBorderEnd();

        pStack.pushPose();
        Matrix4f mat = pStack.last()
                .pose();
        graphics.fillGradient(tooltipX - 3, tooltipY - 4, tooltipX + tooltipTextWidth + 3,
                tooltipY - 3, zLevel, backgroundColor, backgroundColor);
        graphics.fillGradient(tooltipX - 3, tooltipY + tooltipHeight + 3,
                tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 4, zLevel, backgroundColor, backgroundColor);
        graphics.fillGradient(tooltipX - 3, tooltipY - 3, tooltipX + tooltipTextWidth + 3,
                tooltipY + tooltipHeight + 3, zLevel, backgroundColor, backgroundColor);
        graphics.fillGradient(tooltipX - 4, tooltipY - 3, tooltipX - 3, tooltipY + tooltipHeight + 3,
                zLevel, backgroundColor, backgroundColor);
        graphics.fillGradient(tooltipX + tooltipTextWidth + 3, tooltipY - 3,
                tooltipX + tooltipTextWidth + 4, tooltipY + tooltipHeight + 3, zLevel, backgroundColor, backgroundColor);
        graphics.fillGradient(tooltipX - 3, tooltipY - 3 + 1, tooltipX - 3 + 1,
                tooltipY + tooltipHeight + 3 - 1, zLevel, borderColorStart, borderColorEnd);
        graphics.fillGradient(tooltipX + tooltipTextWidth + 2, tooltipY - 3 + 1,
                tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 3 - 1, zLevel, borderColorStart, borderColorEnd);
        graphics.fillGradient(tooltipX - 3, tooltipY - 3, tooltipX + tooltipTextWidth + 3,
                tooltipY - 3 + 1, zLevel, borderColorStart, borderColorStart);
        graphics.fillGradient(tooltipX - 3, tooltipY + tooltipHeight + 2,
                tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 3, zLevel, borderColorEnd, borderColorEnd);

        MultiBufferSource.BufferSource renderType = MultiBufferSource.immediate(Tesselator.getInstance()
                .getBuilder());
        pStack.translate(0.0D, 0.0D, zLevel);

        for (int lineNumber = 0; lineNumber < list.size(); ++lineNumber) {
            ClientTooltipComponent line = list.get(lineNumber);

            if (line != null)
                line.renderText(font, tooltipX, tooltipY, mat, renderType);

            if (lineNumber + 1 == titleLinesCount)
                tooltipY += 2;

            tooltipY += line == null ? 10 : line.getHeight();
        }

        renderType.endBatch();
        pStack.popPose();

        RenderSystem.enableDepthTest();
    }


}
