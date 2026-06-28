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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.fluids.FluidStack;
import net.sinedkadis.terracompositio.api.IHaveKnowledge;
import net.sinedkadis.terracompositio.api.components.EmptyComponent;
import net.sinedkadis.terracompositio.api.components.FluidComponent;
import net.sinedkadis.terracompositio.api.components.HeaderComponent;
import net.sinedkadis.terracompositio.api.components.ItemComponent;
import net.sinedkadis.terracompositio.api.helpers.PlayerHelper;
import net.sinedkadis.terracompositio.config.TCClientConfigs;
import net.sinedkadis.terracompositio.network.TCPackets;
import net.sinedkadis.terracompositio.network.packets.C2SRequestBlockKnowledgePacket;
import net.sinedkadis.terracompositio.network.packets.C2SRequestEntityKnowledgePacket;
import net.sinedkadis.terracompositio.network.packets.S2CKnowledgeDataPacket;
import net.sinedkadis.terracompositio.registries.TCItems;
import net.sinedkadis.terracompositio.util.accessors.PlayerKnowledgeAccessor;

import java.util.ArrayList;
import java.util.List;

public class KnowledgeOverlay {

    private static int hoverTicks = 0;
    // Как часто (в тиках) повторно запрашивать данные у сервера
    // пока смотрим на блок. 20 = раз в секунду.
    private static final int REQUEST_INTERVAL = 20;

    // ─────────────────────────────────────────────────────────────
    //  Ширина и высота строки
    // ─────────────────────────────────────────────────────────────

    private static int lineWidth(Font font, FormattedText line) {
        if (line instanceof ItemComponent ic) {
            return 16 + 4 + 45 + font.width(ic.itemStack().getHoverName());
        } else if (line instanceof FluidComponent fc) {
            return 16 + 4 + 25 + font.width(fc.fluidStack().getFluid().getBucket().getDefaultInstance().getHoverName());
        } else if (line instanceof EmptyComponent) {
            return 0;
        }
        return font.width(line) + 15;
    }

    private static int lineHeight(FormattedText line) {
        if (line instanceof EmptyComponent) {
            return 0;
        }
        return (line instanceof MutableComponent) ? 9 : 16;
    }

    // ─────────────────────────────────────────────────────────────
    //  Вход из EventBus
    // ─────────────────────────────────────────────────────────────

    public static void render(ForgeGui ignoredGui, GuiGraphics graphics,
                              float partialTicks, int width, int height) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || mc.gameMode == null) return;

        Level level = mc.level;
        if (level == null) return;

        LocalPlayer player = mc.player;
        if (player == null) return;

        if (!((PlayerKnowledgeAccessor) player).isCreationAcknowledged()) {
            resetHover();
            return;
        }
        boolean isShifting = player.isShiftKeyDown();

        TCClientConfigs.AppleMode appleMode = TCClientConfigs.APPLE_SHOW_MODE.get();
        if (appleMode.equals(TCClientConfigs.AppleMode.ALWAYS)) isShifting = true;
        if (appleMode.equals(TCClientConfigs.AppleMode.SHIFT) && !isShifting) return;

        List<Component> tooltip = new ArrayList<>();
        HitResult hit = mc.hitResult;

        EntityHitResult entityResult = PlayerHelper.getEntityHitResult(mc, player, level);

        boolean isEntity = false;
        Entity entity = null;
        IHaveKnowledge entityKnowledge = null;
        if (entityResult != null) {
            entity = entityResult.getEntity();
            if ((entity instanceof IHaveKnowledge ihk)) {
                isEntity = true;
                entityKnowledge = ihk;
            }
        }

        if (!isEntity && hit instanceof BlockHitResult result) {
            BlockPos pos = result.getBlockPos();

            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof IHaveKnowledge ihk)) {
                resetHover();
                return;
            }

            hoverTicks++;

            if (hoverTicks == 1 || hoverTicks % REQUEST_INTERVAL == 0) {
                TCPackets.CHANNEL.sendToServer(new C2SRequestBlockKnowledgePacket(pos));
            }


            CompoundTag data = S2CKnowledgeDataPacket.ClientCache.get(pos);
            if (data == null) {
                return;
            }

            ihk.addTooltipLines(data, tooltip, isShifting);
        } else if (isEntity) {
            hoverTicks++;

            if (hoverTicks == 1 || hoverTicks % REQUEST_INTERVAL == 0) {

                TCPackets.CHANNEL.sendToServer(new C2SRequestEntityKnowledgePacket(entity.getUUID()));
            }

            CompoundTag data = S2CKnowledgeDataPacket.ClientCache.get(entity.getUUID());
            if (data == null) {
                return;
            }

            entityKnowledge.addTooltipLines(data, tooltip, isShifting);
        }

        resolveHeaders(tooltip);
        if (tooltip.isEmpty()) return;

        renderOverlay(mc, graphics, partialTicks, width, height, tooltip);

    }

    private static void resolveHeaders(List<Component> tooltip) {
        for (Component component : new ArrayList<>(tooltip)) {
            if (component instanceof HeaderComponent headerComponent) {
                List<Component> headerContents = new ArrayList<>();
                headerComponent.getConsumerList().forEach(listConsumer -> listConsumer.accept(headerContents));
                if (headerContents.isEmpty()) continue;
                resolveHeaders(headerContents);
                tooltip.add(Component.translatable(headerComponent.getHeader().toTranslation()));
                tooltip.addAll(headerContents);
            }
        }
        tooltip.removeIf(HeaderComponent.class::isInstance);
    }

    /**
     * Сбросить состояние при потере фокуса с блока.
     */
    private static void resetHover() {
        hoverTicks = 0;
        // Не чистим кэш здесь — он почистится сам при следующем наведении
        // или при выходе из мира (ClientCache.clear())
    }

    // ─────────────────────────────────────────────────────────────
    //  Рендер оверлея
    // ─────────────────────────────────────────────────────────────

    private static void renderOverlay(Minecraft mc, GuiGraphics graphics, float partialTicks,
                                      int width, int height, List<? extends FormattedText> lines) {
        Font font = mc.font;

        int tooltipW = 0;
        int tooltipH = 0;
        for (int i = 0; i < lines.size(); i++) {
            FormattedText line = lines.get(i);
            tooltipW = Math.max(tooltipW, lineWidth(font, line));
            tooltipH += lineHeight(line);
            if (i < lines.size() - 1) tooltipH += 1;
        }

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

        anchorX = Math.min(anchorX, width - tooltipW - 20);
        anchorY = Math.min(anchorY, height - tooltipH - 20);

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


        {
            ItemStack knowledgeIcon = TCItems.APPLE_OF_KNOWLEDGE.get().getDefaultInstance();
            int iconX = anchorX + tooltipW - 13;
            int iconY = anchorY - 3;
            poseStack.pushPose();
            poseStack.translate(iconX, iconY, 1000);
            poseStack.mulPose(Axis.ZP.rotationDegrees(180));
            poseStack.mulPose(Axis.YP.rotationDegrees(180));
            renderItemIntoGUI(poseStack, knowledgeIcon, false);
            poseStack.popPose();
        }

        drawTooltipBackground(graphics, anchorX, anchorY, tooltipW, tooltipH,
                colorBg, colorBorderT, colorBorderB);

        RenderSystem.disableDepthTest();
        poseStack.pushPose();
        poseStack.translate(0, 0, 401);

        int cursorY = anchorY + 2;
        for (FormattedText line : lines) {
            if (line instanceof ItemComponent ic) {
                drawItemLine(poseStack, graphics, font, ic.itemStack(), anchorX + 2, cursorY);
            } else if (line instanceof FluidComponent fc) {
                drawFluidLine(poseStack, graphics, font, fc.fluidStack(), anchorX + 2, cursorY);
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
    //  Строка с предметом
    // ─────────────────────────────────────────────────────────────

    private static void drawItemLine(PoseStack poseStack, GuiGraphics graphics,
                                     Font font, ItemStack stack, int x, int y) {
        poseStack.pushPose();
        poseStack.translate(x + 10, y, 1000);
        poseStack.mulPose(Axis.ZP.rotationDegrees(180));
        poseStack.mulPose(Axis.YP.rotationDegrees(180));
        renderItemIntoGUI(poseStack, stack, true);
        poseStack.popPose();

        Component label = Component.literal(" -     " + stack.getCount() + "x ")
                .append(stack.getHoverName());
        graphics.drawString(font, label, x, y + 4, 0xFFAAAAAA, false);
    }

    private static void drawFluidLine(PoseStack poseStack, GuiGraphics graphics,
                                      Font font, FluidStack stack, int x, int y) {
        poseStack.pushPose();
        poseStack.translate(x + 10, y, 1000);
        poseStack.mulPose(Axis.ZP.rotationDegrees(180));
        poseStack.mulPose(Axis.YP.rotationDegrees(180));
        renderItemIntoGUI(poseStack, stack.getFluid().getBucket().getDefaultInstance(), true);
        poseStack.popPose();

        Component label = Component.literal(" -     " + stack.getAmount() + "mb ")
                .append(stack.getFluid().getFluidType().getDescription());
        graphics.drawString(font, label, x, y + 4, 0xFFAAAAAA, false);
    }

    // ─────────────────────────────────────────────────────────────
    //  Фон тултипа
    // ─────────────────────────────────────────────────────────────

    private static void drawTooltipBackground(GuiGraphics graphics,
                                              int x, int y, int w, int h,
                                              int bg, int borderTop, int borderBot) {
        final int z = 400;
        int x0 = x - 3, y0 = y - 3, x1 = x + w + 3, y1 = y + h + 3;

        graphics.fillGradient(x0, y0, x1, y1, z, bg, bg);
        graphics.fillGradient(x0, y0 - 1, x1, y0, z, bg, bg);
        graphics.fillGradient(x0, y1, x1, y1 + 1, z, bg, bg);
        graphics.fillGradient(x0 - 1, y0, x0, y1, z, bg, bg);
        graphics.fillGradient(x1, y0, x1 + 1, y1, z, bg, bg);
        graphics.fillGradient(x0, y0 + 1, x0 + 1, y1 - 1, z, borderTop, borderBot);
        graphics.fillGradient(x1 - 1, y0 + 1, x1, y1 - 1, z, borderTop, borderBot);
        graphics.fillGradient(x0, y0, x1, y0 + 1, z, borderTop, borderTop);
        graphics.fillGradient(x0, y1 - 1, x1, y1, z, borderBot, borderBot);
    }

    // ─────────────────────────────────────────────────────────────
    //  Fade анимация
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