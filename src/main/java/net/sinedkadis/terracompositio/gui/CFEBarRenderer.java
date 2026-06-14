package net.sinedkadis.terracompositio.gui;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.IItemDecorator;
import net.sinedkadis.terracompositio.api.TCCapabilities;
import net.sinedkadis.terracompositio.api.networks.cfe.ICFEHandler;
import net.sinedkadis.terracompositio.mixin.accessors.GuiGraphicsAccessor;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import java.util.Optional;

import static net.minecraft.util.FastColor.ARGB32.*;

public final class CFEBarRenderer implements IItemDecorator {

    public static final CFEBarRenderer INSTANCE = new CFEBarRenderer();
    private static final int BAR_W = 12;
    private static final int colorShadow = FastColor.ARGB32.color(255, 0, 0, 0);
    private static final int colorBarLeftEnergy = 0xFF037efc;
    private static final int colorBarRightEnergy = 0xFF025BB8;
    private static final int colorBarLeftDurability = 0xFF00FF00;
    private static final int colorBarRightDurability = 0xFF00c900;
    private static final int colorBarLeftDepleted = FastColor.ARGB32.color(255, 122, 0, 0);
    private static final int colorBarRightDepleted = FastColor.ARGB32.color(255, 255, 27, 27);

    private CFEBarRenderer() {
    }

    public static void render(GuiGraphics graphics, int level, int xPosition, int yPosition, int offset,
                              int left, int right, boolean doDepletedColor) {
        if (doDepletedColor && level <= BAR_W / 4) {
            left = colorBarLeftDepleted;
            right = colorBarRightDepleted;
        }

        int x = xPosition + 2;
        int y = yPosition + 13 - offset;

        fillHorizontalGradient(graphics, RenderType.gui(), x, y, x + level, y + 1, left, right, 190);
    }

    public static void renderBarsTool(GuiGraphics graphics, ICFEHandler handler, ItemStack stack, int xPosition,
                                      int yPosition) {

        boolean renderedDurability = false;

        boolean damaged = stack.isDamaged();

        renderShadow(graphics, xPosition, yPosition, damaged);

        if (damaged) {
            renderedDurability = renderDurabilityBar(graphics, stack.getBarWidth(), xPosition, yPosition);
        }

        renderElectricBar(graphics, handler.getCFE(), handler.getMaxCFE(), xPosition, yPosition,
                renderedDurability);

    }

    private static void renderShadow(GuiGraphics graphics, int xPosition, int yPosition, boolean damaged) {
        int x = xPosition + 2;
        int y = yPosition + 13 - 2;
        if (damaged) {
            y++;
        }
        graphics.fill(RenderType.gui(), x, y, x + 13, y + (2), 190, colorShadow);
    }

    public static void renderElectricBar(GuiGraphics graphics, long charge, long maxCharge, int xPosition,
                                         int yPosition, boolean renderedDurability) {
        if (charge > 0 && maxCharge > 0) {
            int level = Math.round(charge * 13.0F / maxCharge);
            render(graphics, level, xPosition, yPosition, renderedDurability ? 1 : 0, colorBarLeftEnergy,
                    colorBarRightEnergy, true);
        }
    }

    private static boolean renderDurabilityBar(GuiGraphics graphics, int level, int xPosition, int yPosition) {

        render(graphics, level, xPosition, yPosition, 0, colorBarLeftDurability, colorBarRightDurability, true);
        return true;
    }

    public static void fillHorizontalGradient(GuiGraphics graphics, RenderType renderType, int x1, int y1, int x2,
                                              int y2, int colorFrom, int colorTo, int z) {
        VertexConsumer vertexconsumer = graphics.bufferSource().getBuffer(renderType);
        fillHorizontalGradient(graphics, vertexconsumer, x1, y1, x2, y2, z, colorFrom, colorTo);
        ((GuiGraphicsAccessor) graphics).callFlushIfUnmanaged();
    }

    private static void fillHorizontalGradient(GuiGraphics graphics, VertexConsumer consumer,
                                               float x1, float y1, float x2, float y2, float z,
                                               int colorFrom, int colorTo) {
        int a1 = alpha(colorFrom), r1 = red(colorFrom), g1 = green(colorFrom), b1 = blue(colorFrom);
        int a2 = alpha(colorTo), r2 = red(colorTo), g2 = green(colorTo), b2 = blue(colorTo);

        Matrix4f pose = graphics.pose().last().pose();
        consumer.vertex(pose, x1, y1, z).color(r1, g1, b1, a1).endVertex();
        consumer.vertex(pose, x1, y2, z).color(r1, g1, b1, a1).endVertex();
        consumer.vertex(pose, x2, y2, z).color(r2, g2, b2, a2).endVertex();
        consumer.vertex(pose, x2, y1, z).color(r2, g2, b2, a2).endVertex();
    }

    @Override
    public boolean render(@NotNull GuiGraphics guiGraphics, @NotNull Font font, ItemStack stack, int x, int y) {
        Optional<ICFEHandler> handler = stack.getCapability(TCCapabilities.CFE).resolve();
        if (handler.isPresent()) {
            CFEBarRenderer.renderBarsTool(guiGraphics, handler.get(), stack, x, y);
            return true;
        }
        return true;
    }
}
