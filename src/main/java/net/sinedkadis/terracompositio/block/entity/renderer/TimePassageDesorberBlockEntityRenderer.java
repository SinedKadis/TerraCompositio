package net.sinedkadis.terracompositio.block.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.sinedkadis.terracompositio.block.entity.TimePassageDesorberBlockEntity;
import net.sinedkadis.terracompositio.fluid.FluidRenderer;
import org.jetbrains.annotations.NotNull;

public class TimePassageDesorberBlockEntityRenderer implements BlockEntityRenderer<TimePassageDesorberBlockEntity> {
    public TimePassageDesorberBlockEntityRenderer(BlockEntityRendererProvider.Context context) {

    }

    private static final float TANK_HEIGHT = 4 / 16f;
    private static final float TANK_BOTTOM = 2 / 16f;
    private static final float TANK_WIDTH = 12 / 16f;
    private static final float TANK_DEPTH = 12 / 16f;
    private static final float TANK_OFFSET = 2 / 16f;

    @Override
    public void render(TimePassageDesorberBlockEntity pBlockEntity, float pPartialTick, @NotNull PoseStack pPoseStack, @NotNull MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
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
    }
}
