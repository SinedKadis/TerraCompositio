package net.sinedkadis.terracompositio.block.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.Tags;
import net.minecraftforge.fluids.FluidStack;
import net.sinedkadis.terracompositio.block.entity.FlowExtractorBlockEntity;
import net.sinedkadis.terracompositio.block.entity.FlowPortBlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import static net.sinedkadis.terracompositio.TerraCompositio.GLOGGER;

public class FlowExtractorBlockEntityRenderer implements BlockEntityRenderer<FlowExtractorBlockEntity> {

    protected final BlockEntityRendererProvider.Context context;

    public FlowExtractorBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.context = context;
    }

    @Override
    public void render(FlowExtractorBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {

        FluidStack fluid = pBlockEntity.getFluidStack();
        float fluidScale = fluid.isEmpty() ? 0 : pBlockEntity.prevScale;
        VertexConsumer buffer;

        if (fluidScale > 0) {
            //GLOGGER.debug("RENDER: "+"Fluid scale > 0 : {}",fluidScale);
            buffer = pBuffer.getBuffer(Sheets.translucentCullBlockSheet());
            renderObject(getFluidModel(fluid, fluidScale), pPoseStack, buffer, getColorARGB(fluid, fluidScale),
                    calculateGlowLight(pPackedLight, fluid), pPackedOverlay, RenderResizableCuboid.FaceDisplay.FRONT, getCamera(), pBlockEntity.getBlockPos());
        }
    }

    protected Camera getCamera() {
        return context.getBlockEntityRenderDispatcher().camera;
    }
    public static int calculateGlowLight(int combinedLight, @NotNull FluidStack fluid) {
        return fluid.isEmpty() ? combinedLight : calculateGlowLight(combinedLight, fluid.getFluid().getFluidType().getLightLevel(fluid));
    }
    public static int calculateGlowLight(int combinedLight, int glow) {
        //Only factor the glow into the block light portion
        return (combinedLight & 0xFFFF0000) | Math.max(Math.min(glow, 15) << 4, combinedLight & 0xFFFF);
    }
    public static int getColorARGB(@NotNull FluidStack fluidStack, float fluidScale) {
        if (fluidStack.isEmpty()) {
            return -1;
        }
        int color = getColorARGB(fluidStack);
        if (lighterThanAirGas(fluidStack)) {
            return getColorARGB(FastColor.ARGB32.red(color), FastColor.ARGB32.green(color), FastColor.ARGB32.blue(color), Math.min(1, fluidScale + 0.2F));
        }
        return color;
    }
    public static int getColorARGB(int red, int green, int blue, float alpha) {
        if (alpha < 0) {
            alpha = 0;
        } else if (alpha > 1) {
            alpha = 1;
        }
        return FastColor.ARGB32.color((int) (255 * alpha), red, green, blue);
    }
    public static int getColorARGB(@NotNull FluidStack fluidStack) {
        return IClientFluidTypeExtensions.of(fluidStack.getFluid()).getTintColor(fluidStack);
    }
    public static void renderObject(@Nullable Model3D object, @NotNull PoseStack matrix, VertexConsumer buffer, int argb, int light, int overlay,
                                    RenderResizableCuboid.FaceDisplay faceDisplay, Camera camera, BlockPos renderPos) {
        if (object != null) {
            GLOGGER.debug("RENDER: "+"Object is not null");
            renderObject(object, matrix, buffer, argb, light, overlay, faceDisplay, camera, Vec3.atLowerCornerOf(renderPos));
        }
    }
    public static void renderObject(@Nullable Model3D object, @NotNull PoseStack matrix, VertexConsumer buffer, int argb, int light, int overlay,
                                    RenderResizableCuboid.FaceDisplay faceDisplay, Camera camera, @Nullable Vec3 renderPos) {
        if (object != null) {
            RenderResizableCuboid.renderCube(object, matrix, buffer, argb, light, overlay, faceDisplay, camera, renderPos);
        }
    }

    public static class Model3D {

        public float minX, minY, minZ;
        public float maxX, maxY, maxZ;

        private final TextureAtlasSprite[] textures = new TextureAtlasSprite[6];
        private final boolean[] renderSides = {true, true, true, true, true, true};

        public static final Direction[] DIRECTIONS = Direction.values();

        public Model3D setSideRender(Predicate<Direction> shouldRender) {
            for (Direction direction : DIRECTIONS) {
                setSideRender(direction, shouldRender.test(direction));
            }
            return this;
        }

        public Model3D setSideRender(Direction side, boolean value) {
            renderSides[side.ordinal()] = value;
            return this;
        }

        public Model3D copy() {
            Model3D copy = new Model3D();
            System.arraycopy(textures, 0, copy.textures, 0, textures.length);
            System.arraycopy(renderSides, 0, copy.renderSides, 0, renderSides.length);
            return copy.bounds(minX, minY, minZ, maxX, maxY, maxZ);
        }

        @Nullable
        public TextureAtlasSprite getSpriteToRender(Direction side) {
            int ordinal = side.ordinal();
            return renderSides[ordinal] ? textures[ordinal] : null;
        }

        public Model3D shrink(float amount) {
            return grow(-amount);
        }

        public Model3D grow(float amount) {
            return bounds(minX - amount, minY - amount, minZ - amount, maxX + amount, maxY + amount, maxZ + amount);
        }

        public Model3D xBounds(float min, float max) {
            this.minX = min;
            this.maxX = max;
            return this;
        }

        public Model3D yBounds(float min, float max) {
            this.minY = min;
            this.maxY = max;
            return this;
        }

        public Model3D zBounds(float min, float max) {
            this.minZ = min;
            this.maxZ = max;
            return this;
        }

        public Model3D bounds(float min, float max) {
            return bounds(min, min, min, max, max, max);
        }

        public Model3D bounds(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
            return xBounds(minX, maxX)
                    .yBounds(minY, maxY)
                    .zBounds(minZ, maxZ);
        }

        public Model3D prepSingleFaceModelSize(Direction face) {
            bounds(0, 1);
            return switch (face) {
                case DOWN -> yBounds(-0.01F, -0.001F);
                case UP -> yBounds(1.001F, 1.01F);
                case NORTH -> zBounds(-0.01F, -0.001F);
                case SOUTH -> zBounds(1.001F, 1.01F);
                case WEST -> xBounds(-0.01F, -0.001F);
                case EAST -> xBounds(1.001F, 1.01F);
            };
        }

        public Model3D prepFlowing(@NotNull FluidStack fluid) {
            TextureAtlasSprite still = getFluidTexture(fluid, FluidTextureType.STILL);
            TextureAtlasSprite flowing = getFluidTexture(fluid, FluidTextureType.FLOWING);
            return setTextures(still, still, flowing, flowing, flowing, flowing);
        }

        public Model3D setTexture(Direction side, @Nullable TextureAtlasSprite sprite) {
            textures[side.ordinal()] = sprite;
            return this;
        }

        public Model3D setTexture(TextureAtlasSprite tex) {
            Arrays.fill(textures, tex);
            return this;
        }

        public Model3D setTextures(TextureAtlasSprite down, TextureAtlasSprite up, TextureAtlasSprite north, TextureAtlasSprite south, TextureAtlasSprite west,
                                   TextureAtlasSprite east) {
            textures[0] = down;
            textures[1] = up;
            textures[2] = north;
            textures[3] = south;
            textures[4] = west;
            textures[5] = east;
            return this;
        }

        public interface ModelBoundsSetter {

            Model3D set(float min, float max);
        }
    }
    public static TextureAtlasSprite getFluidTexture(@NotNull FluidStack fluidStack, @NotNull FluidTextureType type) {
        IClientFluidTypeExtensions properties = IClientFluidTypeExtensions.of(fluidStack.getFluid());
        ResourceLocation spriteLocation;
        if (type == FluidTextureType.STILL) {
            spriteLocation = properties.getStillTexture(fluidStack);
        } else {
            spriteLocation = properties.getFlowingTexture(fluidStack);
        }
        return getSprite(spriteLocation);
    }
    public enum FluidTextureType {
        STILL,
        FLOWING
    }
    public static TextureAtlasSprite getSprite(ResourceLocation spriteLocation) {
        return Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(spriteLocation);
    }

    private static final Map<FluidStack, Int2ObjectMap<Model3D>> cachedCenterFluids = new HashMap<>();

    private static final int stages = 1_400;

    public static Model3D getFluidModel(@NotNull FluidStack fluid, float fluidScale) {
        return cachedCenterFluids.computeIfAbsent(fluid, f -> new Int2ObjectOpenHashMap<>())
                .computeIfAbsent(getStage(fluid, stages, fluidScale), stage -> new Model3D()
                        .setTexture(getFluidTexture(fluid, FluidTextureType.STILL))
                        .setSideRender(Direction.DOWN, false)
                        .setSideRender(Direction.UP, stage < stages)
                        .xBounds(0.135F, 0.865F)
                        .yBounds(0.0625F, 0.0625F + 0.875F * (stage / (float) stages))
                        .zBounds(0.135F, 0.865F)
                );
    }
    public static int getStage(FluidStack stack, int stages, double scale) {
        return getStage(lighterThanAirGas(stack), stages, scale);
    }
    public static boolean lighterThanAirGas(FluidStack stack) {
        return stack.getFluid().is(Tags.Fluids.GASEOUS) && stack.getFluid().getFluidType().getDensity(stack) <= 0;
    }
    public static int getStage(boolean gaseous, int stages, double scale) {
        if (gaseous) {
            return stages - 1;
        }
        return Math.min(stages - 1, (int) (scale * (stages - 1)));
    }
}
