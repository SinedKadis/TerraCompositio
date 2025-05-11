package net.sinedkadis.terracompositio.block.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.sinedkadis.terracompositio.block.entity.CultivationDesorberBlockEntity;
import net.sinedkadis.terracompositio.fluid.FluidRenderer;
import org.jetbrains.annotations.NotNull;

public class CultivationDesorberBlockEntityRenderer implements BlockEntityRenderer<CultivationDesorberBlockEntity> {
    public CultivationDesorberBlockEntityRenderer(BlockEntityRendererProvider.Context context) {

    }

    private static final float TANK_HEIGHT = 2 / 16f;
    private static final float TANK_BOTTOM = 2 / 16f;
    private static final float TANK_WIDTH = 12 / 16f;
    private static final float TANK_DEPTH = 12 / 16f;
    private static final float TANK_OFFSET = 2 / 16f;
    private static final float ROTATION_SPEED = 0.5f;

    @Override
    public void render(CultivationDesorberBlockEntity pBlockEntity, float pPartialTick, @NotNull PoseStack pPoseStack, @NotNull MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        if (!pBlockEntity.hasLevel()) return;
        pBlockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER, Direction.DOWN).ifPresent(handler -> {
            FluidTank tank = (FluidTank) handler;
            if (tank.isEmpty()) return;

            FluidStack fluidStack = tank.getFluid();
            float fillRatio = (float) tank.getFluidAmount() / tank.getCapacity();
            float renderHeight = TANK_BOTTOM + (TANK_HEIGHT * fillRatio);

            FluidRenderer.renderFluidBox(pPoseStack, fluidStack,
                    TANK_OFFSET, TANK_BOTTOM, TANK_OFFSET,
                    TANK_OFFSET + TANK_WIDTH, renderHeight, TANK_OFFSET + TANK_DEPTH,
                    pBuffer, pPackedLight, true);
        });
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        ItemStack itemStack = pBlockEntity.getRenderStack();

        pPoseStack.pushPose();
        pPoseStack.translate(0.5f, 0.8f, 0.5f);
        pPoseStack.scale(0.6f, 0.6f, 0.6f);

        Level level = pBlockEntity.getLevel();
        if (level != null) {
            float rotation = (level.getGameTime() + pPartialTick) * ROTATION_SPEED;
            pPoseStack.mulPose(Axis.YP.rotationDegrees(rotation));
            pPoseStack.mulPose(Axis.XP.rotationDegrees(rotation * 0.5f));
            itemRenderer.renderStatic(itemStack, ItemDisplayContext.FIXED, getLightLevel(level, pBlockEntity.getBlockPos()),
                    OverlayTexture.NO_OVERLAY, pPoseStack, pBuffer, level, 1);
        }
        pPoseStack.popPose();

    }

    private int getLightLevel(Level level, BlockPos pos) {
        int bLight = level.getBrightness(LightLayer.BLOCK, pos);
        int sLight = level.getBrightness(LightLayer.SKY, pos);
        return LightTexture.pack(bLight, sLight);
    }
}
