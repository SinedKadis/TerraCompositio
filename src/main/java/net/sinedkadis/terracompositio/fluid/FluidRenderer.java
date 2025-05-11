package net.sinedkadis.terracompositio.fluid;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;
import org.joml.Matrix4f;

public class FluidRenderer {
    private static final float PIXEL = 1f / 16f;

    public static void renderFluidBox(PoseStack poseStack, FluidStack fluid,
                                      float xMin, float yMin, float zMin,
                                      float xMax, float yMax, float zMax,
                                      MultiBufferSource buffer, int light, boolean renderBottom) {

        TextureAtlasSprite fluidTexture = getFluidTexture(fluid);
        int color = getFluidColor(fluid);

        VertexConsumer builder = buffer.getBuffer(RenderType.translucent());
        Matrix4f matrix = poseStack.last().pose();

        float width = xMax - xMin;
        float height = yMax - yMin;
        float depth = zMax - zMin;

        // Основные UV-координаты текстуры
        float uMin = fluidTexture.getU0();
        float uMax = fluidTexture.getU1();
        float vMin = fluidTexture.getV0();
        float vMax = fluidTexture.getV1();

        // UV для верхней грани
        float v = (uMax - uMin) * (width / PIXEL) / 16f;
        float topUMax = uMin + v;
        float topVMax = vMin + (vMax - vMin) * (depth / PIXEL) / 16f;

        // Top face (верхняя грань)
        builder.vertex(matrix, xMin, yMax, zMin).color(color).uv(uMin, vMin).uv2(light).normal(0, 1, 0).endVertex();
        builder.vertex(matrix, xMin, yMax, zMax).color(color).uv(uMin, topVMax).uv2(light).normal(0, 1, 0).endVertex();
        builder.vertex(matrix, xMax, yMax, zMax).color(color).uv(topUMax, topVMax).uv2(light).normal(0, 1, 0).endVertex();
        builder.vertex(matrix, xMax, yMax, zMin).color(color).uv(topUMax, vMin).uv2(light).normal(0, 1, 0).endVertex();

        // Bottom face (нижняя грань)
        if (renderBottom) {
            builder.vertex(matrix, xMax, yMin, zMin).color(color).uv(topUMax, vMin).uv2(light).normal(0, -1, 0).endVertex();
            builder.vertex(matrix, xMax, yMin, zMax).color(color).uv(topUMax, topVMax).uv2(light).normal(0, -1, 0).endVertex();
            builder.vertex(matrix, xMin, yMin, zMax).color(color).uv(uMin, topVMax).uv2(light).normal(0, -1, 0).endVertex();
            builder.vertex(matrix, xMin, yMin, zMin).color(color).uv(uMin, vMin).uv2(light).normal(0, -1, 0).endVertex();
        }

        // UV для боковых граней
        float sideUMax = uMin + v;
        float sideVMax = vMin + (vMax - vMin) * (height / PIXEL) / 16f;

        // North face (Z-) - отзеркалено, но с нормалью наружу
        builder.vertex(matrix, xMax, yMin, zMin).color(color).uv(uMin, sideVMax).uv2(light).normal(0, 0, -1).endVertex();
        builder.vertex(matrix, xMin, yMin, zMin).color(color).uv(sideUMax, sideVMax).uv2(light).normal(0, 0, -1).endVertex();
        builder.vertex(matrix, xMin, yMax, zMin).color(color).uv(sideUMax, vMin).uv2(light).normal(0, 0, -1).endVertex();
        builder.vertex(matrix, xMax, yMax, zMin).color(color).uv(uMin, vMin).uv2(light).normal(0, 0, -1).endVertex();

        // South face (Z+)
        builder.vertex(matrix, xMin, yMin, zMax).color(color).uv(uMin, sideVMax).uv2(light).normal(0, 0, 1).endVertex();
        builder.vertex(matrix, xMax, yMin, zMax).color(color).uv(sideUMax, sideVMax).uv2(light).normal(0, 0, 1).endVertex();
        builder.vertex(matrix, xMax, yMax, zMax).color(color).uv(sideUMax, vMin).uv2(light).normal(0, 0, 1).endVertex();
        builder.vertex(matrix, xMin, yMax, zMax).color(color).uv(uMin, vMin).uv2(light).normal(0, 0, 1).endVertex();

        // West face (X-)
        builder.vertex(matrix, xMin, yMin, zMin).color(color).uv(sideUMax, sideVMax).uv2(light).normal(-1, 0, 0).endVertex();
        builder.vertex(matrix, xMin, yMin, zMax).color(color).uv(uMin, sideVMax).uv2(light).normal(-1, 0, 0).endVertex();
        builder.vertex(matrix, xMin, yMax, zMax).color(color).uv(uMin, vMin).uv2(light).normal(-1, 0, 0).endVertex();
        builder.vertex(matrix, xMin, yMax, zMin).color(color).uv(sideUMax, vMin).uv2(light).normal(-1, 0, 0).endVertex();

        // East face (X+)
        builder.vertex(matrix, xMax, yMin, zMax).color(color).uv(sideUMax, sideVMax).uv2(light).normal(1, 0, 0).endVertex();
        builder.vertex(matrix, xMax, yMin, zMin).color(color).uv(uMin, sideVMax).uv2(light).normal(1, 0, 0).endVertex();
        builder.vertex(matrix, xMax, yMax, zMin).color(color).uv(uMin, vMin).uv2(light).normal(1, 0, 0).endVertex();
        builder.vertex(matrix, xMax, yMax, zMax).color(color).uv(sideUMax, vMin).uv2(light).normal(1, 0, 0).endVertex();
    }

    public static TextureAtlasSprite getFluidTexture(FluidStack fluid) {
        Fluid fluidType = fluid.getFluid();
        IClientFluidTypeExtensions clientFluid = IClientFluidTypeExtensions.of(fluidType);
        ResourceLocation stillTexture = clientFluid.getStillTexture();
        return Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(stillTexture);
    }

    private static int getFluidColor(FluidStack fluid) {
        Fluid fluidType = fluid.getFluid();
        IClientFluidTypeExtensions clientFluid = IClientFluidTypeExtensions.of(fluidType);
        return clientFluid.getTintColor(fluid);
    }
}