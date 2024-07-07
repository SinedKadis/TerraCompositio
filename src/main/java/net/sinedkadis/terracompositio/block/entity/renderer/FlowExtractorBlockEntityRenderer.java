package net.sinedkadis.terracompositio.block.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.sinedkadis.terracompositio.block.entity.FlowExtractorBlockEntity;
import net.sinedkadis.terracompositio.mantle.FluidRenderer;
import org.jetbrains.annotations.NotNull;

public class FlowExtractorBlockEntityRenderer implements BlockEntityRenderer<FlowExtractorBlockEntity> {
    public FlowExtractorBlockEntityRenderer(BlockEntityRendererProvider.Context context) {

    }

    @Override
    public void render(@NotNull FlowExtractorBlockEntity flowExtractorBlockEntity, float v, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int i1) {
        BlockState state = flowExtractorBlockEntity.getBlockState();
        FluidRenderer.Baked model = FluidRenderer.getBakedModel(state, FluidRenderer.Baked.class);


        IFluidHandler tank = flowExtractorBlockEntity.getFluidTank();
        if (model == null)
            return;
        FluidRenderer.renderScaledCuboid(poseStack, multiBufferSource, model.getFluid(), tank.getFluidInTank(0), 0, tank.getTankCapacity(0), i, false);
    }
}
