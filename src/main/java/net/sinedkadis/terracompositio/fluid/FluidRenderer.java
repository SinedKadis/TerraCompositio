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
    public static void renderFluidBox(PoseStack poseStack, FluidStack fluid,
                                      float xMin, float yMin, float zMin,
                                      float xMax, float yMax, float zMax,
                                      MultiBufferSource buffer, int light, boolean renderBottom) {

        TextureAtlasSprite fluidTexture = getFluidTexture(fluid);
        int color = getFluidColor(fluid);
        float uMin = fluidTexture.getU0();
        float uMax = fluidTexture.getU1();
        float vMin = fluidTexture.getV0();
        float vMax = fluidTexture.getV1();

        VertexConsumer builder = buffer.getBuffer(RenderType.translucent());
        Matrix4f matrix = poseStack.last().pose();

        // Top face
        builder.vertex(matrix, xMin, yMax, zMin).color(color).uv(uMin, vMin).uv2(light).normal(0, 1, 0).endVertex();
        builder.vertex(matrix, xMin, yMax, zMax).color(color).uv(uMin, vMax).uv2(light).normal(0, 1, 0).endVertex();
        builder.vertex(matrix, xMax, yMax, zMax).color(color).uv(uMax, vMax).uv2(light).normal(0, 1, 0).endVertex();
        builder.vertex(matrix, xMax, yMax, zMin).color(color).uv(uMax, vMin).uv2(light).normal(0, 1, 0).endVertex();

        // Bottom face
        if (renderBottom) {
            builder.vertex(matrix, xMax, yMin, zMin).color(color).uv(uMax, vMin).uv2(light).normal(0, -1, 0).endVertex();
            builder.vertex(matrix, xMax, yMin, zMax).color(color).uv(uMax, vMax).uv2(light).normal(0, -1, 0).endVertex();
            builder.vertex(matrix, xMin, yMin, zMax).color(color).uv(uMin, vMax).uv2(light).normal(0, -1, 0).endVertex();
            builder.vertex(matrix, xMin, yMin, zMin).color(color).uv(uMin, vMin).uv2(light).normal(0, -1, 0).endVertex();
        }

        // North face
        builder.vertex(matrix, xMax, yMin, zMin).color(color).uv(uMax, vMax).uv2(light).normal(0, 0, -1).endVertex();
        builder.vertex(matrix, xMin, yMin, zMin).color(color).uv(uMin, vMax).uv2(light).normal(0, 0, -1).endVertex();
        builder.vertex(matrix, xMin, yMax, zMin).color(color).uv(uMin, vMin).uv2(light).normal(0, 0, -1).endVertex();
        builder.vertex(matrix, xMax, yMax, zMin).color(color).uv(uMax, vMin).uv2(light).normal(0, 0, -1).endVertex();

        // South face
        builder.vertex(matrix, xMin, yMin, zMax).color(color).uv(uMin, vMax).uv2(light).normal(0, 0, 1).endVertex();
        builder.vertex(matrix, xMax, yMin, zMax).color(color).uv(uMax, vMax).uv2(light).normal(0, 0, 1).endVertex();
        builder.vertex(matrix, xMax, yMax, zMax).color(color).uv(uMax, vMin).uv2(light).normal(0, 0, 1).endVertex();
        builder.vertex(matrix, xMin, yMax, zMax).color(color).uv(uMin, vMin).uv2(light).normal(0, 0, 1).endVertex();

        // West face
        builder.vertex(matrix, xMin, yMin, zMin).color(color).uv(uMax, vMax).uv2(light).normal(-1, 0, 0).endVertex();
        builder.vertex(matrix, xMin, yMin, zMax).color(color).uv(uMin, vMax).uv2(light).normal(-1, 0, 0).endVertex();
        builder.vertex(matrix, xMin, yMax, zMax).color(color).uv(uMin, vMin).uv2(light).normal(-1, 0, 0).endVertex();
        builder.vertex(matrix, xMin, yMax, zMin).color(color).uv(uMax, vMin).uv2(light).normal(-1, 0, 0).endVertex();

        // East face
        builder.vertex(matrix, xMax, yMin, zMax).color(color).uv(uMax, vMax).uv2(light).normal(1, 0, 0).endVertex();
        builder.vertex(matrix, xMax, yMin, zMin).color(color).uv(uMin, vMax).uv2(light).normal(1, 0, 0).endVertex();
        builder.vertex(matrix, xMax, yMax, zMin).color(color).uv(uMin, vMin).uv2(light).normal(1, 0, 0).endVertex();
        builder.vertex(matrix, xMax, yMax, zMax).color(color).uv(uMax, vMin).uv2(light).normal(1, 0, 0).endVertex();
    }

    private static TextureAtlasSprite getFluidTexture(FluidStack fluid) {
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
