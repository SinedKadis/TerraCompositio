package net.sinedkadis.terracompositio.gui;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.sinedkadis.terracompositio.api.IHaveKnowledge;
import net.sinedkadis.terracompositio.config.TCClientConfigs;
import net.sinedkadis.terracompositio.registries.TCItems;
import net.sinedkadis.terracompositio.util.ItemComponent;
import net.sinedkadis.terracompositio.util.accessors.PlayerKnowledgeAccessor;

import java.util.ArrayList;
import java.util.List;

public class KnowledgeOverlay {

    private static int hoverTicks = 0;

    // ─────────────────────────────────────────────────────────────
    //  Структура одной строки тултипа: либо текст, либо предмет
    // ─────────────────────────────────────────────────────────────

    /**
     * Считаем ширину строки: предмет занимает иконку + пробел + имя.
     */
    private static int lineWidth(Font font, FormattedText line) {
        if (line instanceof ItemComponent ic) {
            // 16px иконка + 4px отступ + ширина имени
            return 16 + 4 + 34 + font.width(ic.itemStack().getHoverName());
        }
        return font.width(line) + 15;
    }

    /**
     * Высота одной строки: у предмета — 16px (размер иконки), у текста — 9px (стандарт).
     */
    private static int lineHeight(FormattedText line) {
        return (line instanceof ItemComponent) ? 16 : 9;
    }

    // ─────────────────────────────────────────────────────────────
    //  Вход из EventBus
    // ─────────────────────────────────────────────────────────────

    public static void render(ForgeGui ignoredGui, GuiGraphics graphics,
                              float partialTicks, int width, int height) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || mc.gameMode == null) return;

        HitResult hit = mc.hitResult;
        if (!(hit instanceof BlockHitResult result)) {
            hoverTicks = 0;
            return;
        }

        Level level = mc.level;
        if (level == null) return;

        BlockPos pos = result.getBlockPos();
        hoverTicks++;

        BlockEntity be = level.getBlockEntity(pos);
        LocalPlayer player = mc.player;
        if (player == null) return;

        boolean hasCreationKnowledge = ((PlayerKnowledgeAccessor) player).isCreationAcknowledged();
        boolean isShifting = player.isShiftKeyDown();

        if (!(be instanceof IHaveKnowledge ihk) || !hasCreationKnowledge) {
            hoverTicks = 0;
            return;
        }

        List<Component> tooltip = new ArrayList<>();
        ihk.addToKnowledgeTooltip(tooltip, isShifting);

        if (tooltip.isEmpty()) {
            hoverTicks = 0;
            return;
        }

        renderOverlay(mc, graphics, partialTicks, width, height, tooltip);
    }

    // ─────────────────────────────────────────────────────────────
    //  Рендер всего оверлея
    // ─────────────────────────────────────────────────────────────

    private static void renderOverlay(Minecraft mc, GuiGraphics graphics, float partialTicks,
                                      int width, int height, List<? extends FormattedText> lines) {
        Font font = mc.font;

        // ── 1. Считаем размеры тултипа (учитываем ВСЕ строки, включая ItemComponent) ──
        int tooltipW = 0;
        int tooltipH = 0;
        for (int i = 0; i < lines.size(); i++) {
            FormattedText line = lines.get(i);
            tooltipW = Math.max(tooltipW, lineWidth(font, line));
            tooltipH += lineHeight(line);
            if (i < lines.size() - 1) {
                // Стандартный gap в один пиксель между строками
                tooltipH += 1;
            }
        }

        // ── 2. Позиция тултипа по конфигу ──
        int anchorX = width / 2 + TCClientConfigs.OVERLAY_X_OFFSET.get() - 35;
        int anchorY = height / 2 + TCClientConfigs.OVERLAY_Y_OFFSET.get() - 12;

        switch (TCClientConfigs.OVERLAY_ANCHOR_CORNER.get()) {
            case RIGHT_UP -> anchorX -= tooltipW;
            case LEFT_DOWN -> anchorY -= tooltipH;
            case RIGHT_DOWN -> {
                anchorX -= tooltipW;
                anchorY -= tooltipH;
            }
        }

        // Не даём выйти за экран
        anchorX = Math.min(anchorX, width - tooltipW - 20);
        anchorY = Math.min(anchorY, height - tooltipH - 20);

        // ── 3. Fade-in анимация ──
        float fade = Mth.clamp((hoverTicks + partialTicks) / 24f, 0f, 1f);

        int colorBg = 0xf0_100010;
        int colorBorderT = 0x50_5000ff;
        int colorBorderB = 0x50_28007f;

        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();

        if (fade < 1f) {
            applyFadeTranslation(poseStack, fade);
            if (!TCClientConfigs.OVERLAY_FADE_DIR.get().equals(Direction.NORTH)) {
                colorBg = scaleAlpha(colorBg, fade);
                colorBorderT = scaleAlpha(colorBorderT, fade);
                colorBorderB = scaleAlpha(colorBorderB, fade);
            }
        }

        // ── 4. Иконка Apple of Knowledge рядом с тултипом ──
        ItemStack knowledgeIcon = TCItems.APPLE_OF_KNOWLEDGE.get().getDefaultInstance();
        {
            int iconX = anchorX + tooltipW - 13;
            int iconY = anchorY - 3;
            poseStack.pushPose();
            poseStack.translate(iconX, iconY, 1000);
            poseStack.mulPose(Axis.ZP.rotationDegrees(180));
            poseStack.mulPose(Axis.YP.rotationDegrees(180));
            renderItemIntoGUI(poseStack, knowledgeIcon, false);
            poseStack.popPose();
        }

        // ── 5. Фон тултипа (рисуем первым, на Z=400) ──
        drawTooltipBackground(graphics, anchorX, anchorY, tooltipW, tooltipH,
                colorBg, colorBorderT, colorBorderB);

        // ── 6. Содержимое строка за строкой ──
        // Поднимаем весь стек на Z=401 чтобы текст и иконки гарантированно
        // были выше фона (fillGradient пишет z=400 в depth buffer).
        RenderSystem.disableDepthTest();
        poseStack.pushPose();
        poseStack.translate(0, 0, 401);

        int cursorY = anchorY + 2;

        for (FormattedText line : lines) {
            if (line instanceof ItemComponent ic) {
                drawItemLine(poseStack, graphics, font, ic.itemStack(), anchorX + 2, cursorY);
            } else {
                FormattedCharSequence seq = line instanceof FormattedCharSequence fcs
                        ? fcs
                        : font.split(line, Integer.MAX_VALUE).stream()
                          .findFirst().orElse(FormattedCharSequence.EMPTY);
                graphics.drawString(font, seq, anchorX + 2, cursorY, 0xFF_FFFFFF, false);
            }
            cursorY += lineHeight(line) + 1;
        }

        poseStack.popPose();
        RenderSystem.enableDepthTest();

        poseStack.popPose();
    }

    // ─────────────────────────────────────────────────────────────
    //  Рисуем одну строку с предметом: иконка 16×16 + "Nx Название"
    // ─────────────────────────────────────────────────────────────

    private static void drawItemLine(PoseStack poseStack, GuiGraphics graphics,
                                     Font font, ItemStack stack, int x, int y) {
        // Иконка 16×16, центрирована по высоте строки (16px)
        poseStack.pushPose();
        poseStack.translate(x + 10, y, 1000); // center of 16×16 cell
        poseStack.mulPose(Axis.ZP.rotationDegrees(180));
        poseStack.mulPose(Axis.YP.rotationDegrees(180));
        renderItemIntoGUI(poseStack, stack, true);
        poseStack.popPose();

        // Текст: "Nx Локализованное имя"
        // Используем getHoverName() — это Component с правильной локализацией
        Component label = Component.literal(" -     " + stack.getCount() + "x ")
                .append(stack.getHoverName());
        // y + 4 — вертикальное выравнивание по центру 16px-строки
        graphics.drawString(font, label, x, y + 4, 0xFFAAAAAA, false);
    }

    // ─────────────────────────────────────────────────────────────
    //  Фон тултипа (Minecraft-стиль, без форжевых ивентов)
    // ─────────────────────────────────────────────────────────────

    private static void drawTooltipBackground(GuiGraphics graphics,
                                              int x, int y, int w, int h,
                                              int bg, int borderTop, int borderBot) {
        final int z = 400;
        int x0 = x - 3, y0 = y - 3, x1 = x + w + 3, y1 = y + h + 3;

        // Тело
        graphics.fillGradient(x0, y0, x1, y1, z, bg, bg);
        // Верхняя и нижняя крышки (для чёткой прямоугольной формы)
        graphics.fillGradient(x0, y0 - 1, x1, y0, z, bg, bg);
        graphics.fillGradient(x0, y1, x1, y1 + 1, z, bg, bg);
        // Боковые крышки
        graphics.fillGradient(x0 - 1, y0, x0, y1, z, bg, bg);
        graphics.fillGradient(x1, y0, x1 + 1, y1, z, bg, bg);
        // Левая граница
        graphics.fillGradient(x0, y0 + 1, x0 + 1, y1 - 1, z, borderTop, borderBot);
        // Правая граница
        graphics.fillGradient(x1 - 1, y0 + 1, x1, y1 - 1, z, borderTop, borderBot);
        // Верхняя граница
        graphics.fillGradient(x0, y0, x1, y0 + 1, z, borderTop, borderTop);
        // Нижняя граница
        graphics.fillGradient(x0, y1 - 1, x1, y1, z, borderBot, borderBot);
    }

    // ─────────────────────────────────────────────────────────────
    //  Анимация появления (из конфигурируемого направления)
    // ─────────────────────────────────────────────────────────────

    private static void applyFadeTranslation(PoseStack poseStack, float fade) {
        double offset = Math.pow(1 - fade, 3) * 8;
        switch (TCClientConfigs.OVERLAY_FADE_DIR.get()) {
            case EAST -> poseStack.translate(offset * Math.signum(TCClientConfigs.OVERLAY_X_OFFSET.get() - .5f), 0, 0);
            case WEST -> poseStack.translate(-offset * Math.signum(TCClientConfigs.OVERLAY_X_OFFSET.get() + .5f), 0, 0);
            case UP -> poseStack.translate(0, -offset * Math.signum(TCClientConfigs.OVERLAY_Y_OFFSET.get() + .5f), 0);
            case DOWN -> poseStack.translate(0, offset * Math.signum(TCClientConfigs.OVERLAY_Y_OFFSET.get() - .5f), 0);
            default -> {
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  Утилиты
    // ─────────────────────────────────────────────────────────────

    public static int scaleAlpha(int color, float scale) {
        int alpha = Math.round(((color >>> 24) & 0xFF) * scale);
        return (color & 0x00FFFFFF) | (alpha << 24);
    }

    /**
     * Рендерит ItemStack в текущую точку PoseStack как 16×16 GUI-иконку.
     * Вызывающий код обязан:
     * - translate к центру нужной ячейки
     * - mulPose ZP 180°, YP 180° (разворот для правильного отображения)
     */
    public static void renderItemIntoGUI(PoseStack poseStack, ItemStack stack, boolean flatLightingFix) {
        Minecraft mc = Minecraft.getInstance();
        ItemRenderer renderer = mc.getItemRenderer();
        BakedModel model = renderer.getModel(stack, null, null, 0);

        renderer.textureManager.getTexture(InventoryMenu.BLOCK_ATLAS).setFilter(false, false);
        RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
        RenderSystem.enableBlend();
        RenderSystem.enableCull();
        RenderSystem.blendFunc(
                com.mojang.blaze3d.platform.GlStateManager.SourceFactor.SRC_ALPHA,
                com.mojang.blaze3d.platform.GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        poseStack.pushPose();
        poseStack.translate(0, 0, 500f);
        poseStack.translate(8f, -8f, 0f);
        poseStack.scale(16f, 16f, 16f);

        MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();
        boolean needFlatLighting = flatLightingFix && !model.usesBlockLight();
        if (needFlatLighting) Lighting.setupForFlatItems();

        renderer.render(stack, ItemDisplayContext.GUI, false, poseStack, buffer,
                LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, model);

        buffer.endBatch();

        if (needFlatLighting) Lighting.setupFor3DItems();
        poseStack.popPose();
    }

}