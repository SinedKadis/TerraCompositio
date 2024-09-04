package net.sinedkadis.terracompositio.block.entity.renderer;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.block.entity.FlowExtractorBlockEntity;
import net.sinedkadis.terracompositio.fluid.ModFluidTank;
import org.jetbrains.annotations.NotNull;

public class FlowExtractorBlockEntityRenderer implements BlockEntityRenderer<FlowExtractorBlockEntity> {

    protected final BlockEntityRendererProvider.Context context;

    public FlowExtractorBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.context = context;
    }

    @Override
    public void render(@NotNull FlowExtractorBlockEntity be, float partialTicks, @NotNull PoseStack ms, @NotNull MultiBufferSource buffer, int light,
                       int overlay) {
        if (isInvalid(be))
            return;
        renderFluids(be, partialTicks, ms, buffer, light, overlay);
    }
    public boolean isInvalid(FlowExtractorBlockEntity be) {
        return !be.hasLevel() || be.getBlockState()
                .getBlock() == Blocks.AIR;
    }
    protected float renderFluids(FlowExtractorBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
                                 int light, int overlay) {
        ModFluidTank inputFluids = be.getInputFluidTank();
        ModFluidTank outputFluids = be.getOutputFluidTank();
        ModFluidTank[] tanks = { inputFluids, outputFluids };
        float totalUnits = be.getTotalFluidUnits(partialTicks);
        if (totalUnits < 1)
            return 0;

        float fluidLevel = Mth.clamp(totalUnits / 2000, 0, 1);

        fluidLevel = 1 - ((1 - fluidLevel) * (1 - fluidLevel));

        float xMin = 2 / 16f;
        float xMax = 2 / 16f;
        final float yMin = 2 / 16f;
        final float yMax = yMin + 12 / 16f * fluidLevel;
        final float zMin = 2 / 16f;
        final float zMax = 14 / 16f;

        for (ModFluidTank tank : tanks) {
            if (tank == null)
                continue;
            for (ModFluidTank.TankSegment tankSegment : tank.getTanks()) {
                FluidStack renderedFluid = tankSegment.getRenderedFluid();
                if (renderedFluid.isEmpty())
                    continue;
                float units = tankSegment.getTotalUnits(partialTicks);
                if (units < 1)
                    continue;

                float partial = Mth.clamp(units / totalUnits, 0, 1);
                xMax += partial * 12 / 16f;
                renderFluidBox(renderedFluid, xMin, yMin, zMin, xMax, yMax, zMax, buffer, ms, light,
                        false);

                xMin = xMax;
            }
        }

        return yMax;
    }
    public static void renderFluidBox(FluidStack fluidStack, float xMin, float yMin, float zMin, float xMax, float yMax,
                                      float zMax, MultiBufferSource buffer, PoseStack ms, int light, boolean renderBottom) {
        renderFluidBox(fluidStack, xMin, yMin, zMin, xMax, yMax, zMax, getFluidBuilder(buffer), ms, light,
                renderBottom);
    }
    public static VertexConsumer getFluidBuilder(MultiBufferSource buffer) {
        return buffer.getBuffer(FLUID);
    }
    private static final RenderType FLUID = RenderType.create(createLayerName("fluid"),
            DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, false, true, RenderType.CompositeState.builder()
                    .setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getRendertypeEntityTranslucentCullShader))
                    .setTextureState(new RenderStateShard.TextureStateShard(TextureAtlas.LOCATION_BLOCKS, false, true))
                    .setTransparencyState(new RenderStateShard.TransparencyStateShard("translucent_transparency", () -> {
                        RenderSystem.enableBlend();
                        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                    }, () -> {
                        RenderSystem.disableBlend();
                        RenderSystem.defaultBlendFunc();
                    }))
                    .setLightmapState(new RenderStateShard.LightmapStateShard(true))
                    .setOverlayState(new RenderStateShard.OverlayStateShard(true))
                    .createCompositeState(true));

    private static String createLayerName(String name) {
        return TerraCompositio.MOD_ID + ":" + name;
    }
    public static void renderFluidBox(FluidStack fluidStack, float xMin, float yMin, float zMin, float xMax, float yMax,
                                      float zMax, VertexConsumer builder, PoseStack ms, int light, boolean renderBottom) {
        Fluid fluid = fluidStack.getFluid();
        IClientFluidTypeExtensions clientFluid = IClientFluidTypeExtensions.of(fluid);
        FluidType fluidAttributes = fluid.getFluidType();
        TextureAtlasSprite fluidTexture = Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(clientFluid.getStillTexture(fluidStack));

        int color = clientFluid.getTintColor(fluidStack);
        int blockLightIn = (light >> 4) & 0xF;
        int luminosity = Math.max(blockLightIn, fluidAttributes.getLightLevel(fluidStack));
        light = (light & 0xF00000) | luminosity << 4;

        Vec3 center = new Vec3(xMin + (xMax - xMin) / 2, yMin + (yMax - yMin) / 2, zMin + (zMax - zMin) / 2);
        ms.pushPose();

        for (Direction side : Direction.values()) {
            if (side == Direction.DOWN && !renderBottom)
                continue;

            boolean positive = side.getAxisDirection() == Direction.AxisDirection.POSITIVE;
            if (side.getAxis()
                    .isHorizontal()) {
                if (side.getAxis() == Direction.Axis.X) {
                    renderStillTiledFace(side, zMin, yMin, zMax, yMax, positive ? xMax : xMin, builder, ms, light,
                            color, fluidTexture);
                } else {
                    renderStillTiledFace(side, xMin, yMin, xMax, yMax, positive ? zMax : zMin, builder, ms, light,
                            color, fluidTexture);
                }
            } else {
                renderStillTiledFace(side, xMin, zMin, xMax, zMax, positive ? yMax : yMin, builder, ms, light, color,
                        fluidTexture);
            }
        }

        ms.popPose();
    }
    public static void renderStillTiledFace(Direction dir, float left, float down, float right, float up, float depth,
                                            VertexConsumer builder, PoseStack ms, int light, int color, TextureAtlasSprite texture) {
        renderTiledFace(dir, left, down, right, up, depth, builder, ms, light, color, texture, 1);
    }
    public static void renderTiledFace(Direction dir, float left, float down, float right, float up, float depth,
                                       VertexConsumer builder, PoseStack ms, int light, int color, TextureAtlasSprite texture, float textureScale) {
        boolean positive = dir.getAxisDirection() == Direction.AxisDirection.POSITIVE;
        boolean horizontal = dir.getAxis()
                .isHorizontal();
        boolean x = dir.getAxis() == Direction.Axis.X;

        float shrink = texture.uvShrinkRatio() * 0.25f * textureScale;
        float centerU = texture.getU0() + (texture.getU1() - texture.getU0()) * 0.5f * textureScale;
        float centerV = texture.getV0() + (texture.getV1() - texture.getV0()) * 0.5f * textureScale;

        float f;
        float x2 = 0;
        float y2 = 0;
        float u1, u2;
        float v1, v2;
        for (float x1 = left; x1 < right; x1 = x2) {
            f = Mth.floor(x1);
            x2 = Math.min(f + 1, right);
            if (dir == Direction.NORTH || dir == Direction.EAST) {
                f = Mth.ceil(x2);
                u1 = texture.getU((f - x2) * 16 * textureScale);
                u2 = texture.getU((f - x1) * 16 * textureScale);
            } else {
                u1 = texture.getU((x1 - f) * 16 * textureScale);
                u2 = texture.getU((x2 - f) * 16 * textureScale);
            }
            u1 = Mth.lerp(shrink, u1, centerU);
            u2 = Mth.lerp(shrink, u2, centerU);
            for (float y1 = down; y1 < up; y1 = y2) {
                f = Mth.floor(y1);
                y2 = Math.min(f + 1, up);
                if (dir == Direction.UP) {
                    v1 = texture.getV((y1 - f) * 16 * textureScale);
                    v2 = texture.getV((y2 - f) * 16 * textureScale);
                } else {
                    f = Mth.ceil(y2);
                    v1 = texture.getV((f - y2) * 16 * textureScale);
                    v2 = texture.getV((f - y1) * 16 * textureScale);
                }
                v1 = Mth.lerp(shrink, v1, centerV);
                v2 = Mth.lerp(shrink, v2, centerV);

                if (horizontal) {
                    if (x) {
                        putVertex(builder, ms, depth, y2, positive ? x2 : x1, color, u1, v1, dir, light);
                        putVertex(builder, ms, depth, y1, positive ? x2 : x1, color, u1, v2, dir, light);
                        putVertex(builder, ms, depth, y1, positive ? x1 : x2, color, u2, v2, dir, light);
                        putVertex(builder, ms, depth, y2, positive ? x1 : x2, color, u2, v1, dir, light);
                    } else {
                        putVertex(builder, ms, positive ? x1 : x2, y2, depth, color, u1, v1, dir, light);
                        putVertex(builder, ms, positive ? x1 : x2, y1, depth, color, u1, v2, dir, light);
                        putVertex(builder, ms, positive ? x2 : x1, y1, depth, color, u2, v2, dir, light);
                        putVertex(builder, ms, positive ? x2 : x1, y2, depth, color, u2, v1, dir, light);
                    }
                } else {
                    putVertex(builder, ms, x1, depth, positive ? y1 : y2, color, u1, v1, dir, light);
                    putVertex(builder, ms, x1, depth, positive ? y2 : y1, color, u1, v2, dir, light);
                    putVertex(builder, ms, x2, depth, positive ? y2 : y1, color, u2, v2, dir, light);
                    putVertex(builder, ms, x2, depth, positive ? y1 : y2, color, u2, v1, dir, light);
                }
            }
        }
    }
    private static void putVertex(VertexConsumer builder, PoseStack ms, float x, float y, float z, int color, float u,
                                  float v, Direction face, int light) {

        Vec3i normal = face.getNormal();
        PoseStack.Pose peek = ms.last();
        int a = color >> 24 & 0xff;
        int r = color >> 16 & 0xff;
        int g = color >> 8 & 0xff;
        int b = color & 0xff;

        builder.vertex(peek.pose(), x, y, z)
                .color(r, g, b, a)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(peek.normal(), normal.getX(), normal.getY(), normal.getZ())
                .endVertex();
    }
}
