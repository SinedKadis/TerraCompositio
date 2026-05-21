package net.sinedkadis.terracompositio.block.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.sinedkadis.terracompositio.block.entity.FlowCedarCasingBlockEntity;
import net.sinedkadis.terracompositio.block.entity.MatterInfuserIOBlockEntity;
import net.sinedkadis.terracompositio.util.TCUtil;
import org.jetbrains.annotations.NotNull;

public class MatterInfuserIOBlockEntityRenderer implements BlockEntityRenderer<MatterInfuserIOBlockEntity> {
    public MatterInfuserIOBlockEntityRenderer(BlockEntityRendererProvider.Context ignoredContext) {

    }


    @Override
    public void render(MatterInfuserIOBlockEntity pBlockEntity,
                       float pPartialTick,
                       @NotNull PoseStack pPoseStack,
                       @NotNull MultiBufferSource pBuffer,
                       int pPackedLight,
                       int pPackedOverlay) {
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        BlockState blockState = pBlockEntity.getBlockState();

        Level level = pBlockEntity.getLevel();

        @SuppressWarnings("DataFlowIssue")
        IItemHandler iItemHandler = pBlockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).orElse(null);
        if (!(iItemHandler instanceof ItemStackHandler itemStackHandler)) return;
        if (level == null) return;
        renderLeftConnection(pBlockEntity, pPoseStack, pBuffer, level, blockState, itemRenderer, itemStackHandler);
        renderUpConnection(pBlockEntity, pPoseStack, pBuffer, level, blockState, itemRenderer, itemStackHandler);
        renderDownConnection(pBlockEntity, pPoseStack, pBuffer, level, blockState, itemRenderer, itemStackHandler);

    }

    private void renderDownConnection(MatterInfuserIOBlockEntity pBlockEntity,
                                      @NotNull PoseStack pPoseStack,
                                      @NotNull MultiBufferSource pBuffer,
                                      Level level,
                                      BlockState blockState,
                                      ItemRenderer itemRenderer,
                                      ItemStackHandler ignoredItemStackHandler) {
        FlowCedarCasingBlockEntity casingBE = pBlockEntity.getCasingBE();
        if (casingBE == null) return;
        @SuppressWarnings("DataFlowIssue")
        IItemHandler iitemHandler = casingBE.getCapability(ForgeCapabilities.ITEM_HANDLER).orElse(null);
        if (!(iitemHandler instanceof ItemStackHandler itemHandler)) return;

        ItemStack stackInSlot = itemHandler.getStackInSlot(FlowCedarCasingBlockEntity.DOWN_CONNECTION_SLOT);
        if (!stackInSlot.isEmpty()) {
            Direction facing = blockState.getValue(BlockStateProperties.HORIZONTAL_FACING);

            pPoseStack.pushPose();

            pPoseStack.mulPose(Axis.YP.rotationDegrees(facing.toYRot()));
            pPoseStack.mulPose(Axis.ZP.rotationDegrees(-45));

            //To center
            pPoseStack.translate(0.5f, 0.25f, 0.5f);
            Vec3i normal = facing.getNormal();
            Vec3 vec3 = Vec3.atLowerCornerOf(normal);
            // To face
            pPoseStack.translate(vec3.scale(-1).x() * 0.5f, 0, vec3.scale(-1).z() * 0.5f);

            Vec3 rotatedVec3 = vec3.yRot((float) ((Math.PI) / 2f));
            // To right
            pPoseStack.translate(rotatedVec3.x() * 0.1f, 0, rotatedVec3.z() * 0.1f);

            itemRenderer.renderStatic(stackInSlot, ItemDisplayContext.FIXED, TCUtil.getLightLevel(level, pBlockEntity.getBlockPos(), Direction.UP),
                    OverlayTexture.NO_OVERLAY, pPoseStack, pBuffer, level, 1);

            pPoseStack.popPose();
        }

    }

    private void renderUpConnection(MatterInfuserIOBlockEntity pBlockEntity,
                                    @NotNull PoseStack pPoseStack,
                                    @NotNull MultiBufferSource pBuffer,
                                    Level level,
                                    BlockState blockState,
                                    ItemRenderer itemRenderer,
                                    ItemStackHandler ignoredItemStackHandler) {
        FlowCedarCasingBlockEntity casingBE = pBlockEntity.getCasingBE();
        if (casingBE == null) return;
        @SuppressWarnings("DataFlowIssue")
        IItemHandler iitemHandler = casingBE.getCapability(ForgeCapabilities.ITEM_HANDLER).orElse(null);
        if (!(iitemHandler instanceof ItemStackHandler itemHandler)) return;

        ItemStack stackInSlot = itemHandler.getStackInSlot(FlowCedarCasingBlockEntity.UP_CONNECTION_SLOT);
        if (!stackInSlot.isEmpty()) {
            Direction facing = blockState.getValue(BlockStateProperties.HORIZONTAL_FACING);

            pPoseStack.pushPose();

            pPoseStack.mulPose(Axis.YP.rotationDegrees(facing.toYRot()));
            pPoseStack.mulPose(Axis.ZP.rotationDegrees(-45));

            //To center
            pPoseStack.translate(0.5f, 0.75f, 0.5f);
            Vec3i normal = facing.getNormal();
            Vec3 vec3 = Vec3.atLowerCornerOf(normal);
            // To face
            pPoseStack.translate(vec3.scale(-1).x() * 0.5f, 0, vec3.scale(-1).z() * 0.5f);

            Vec3 rotatedVec3 = vec3.yRot((float) ((3 * Math.PI) / 2f));
            // To left
            pPoseStack.translate(rotatedVec3.x() * 0.1f, 0, rotatedVec3.z() * 0.1f);

            itemRenderer.renderStatic(stackInSlot, ItemDisplayContext.FIXED, TCUtil.getLightLevel(level, pBlockEntity.getBlockPos(), Direction.UP),
                    OverlayTexture.NO_OVERLAY, pPoseStack, pBuffer, level, 1);

            pPoseStack.popPose();
        }

    }

    private void renderLeftConnection(MatterInfuserIOBlockEntity pBlockEntity,
                                      @NotNull PoseStack pPoseStack,
                                      @NotNull MultiBufferSource pBuffer,
                                      Level level,
                                      BlockState blockState,
                                      ItemRenderer itemRenderer,
                                      ItemStackHandler itemStackHandler) {
        ItemStack stackInSlot = itemStackHandler.getStackInSlot(0);
        if (!stackInSlot.isEmpty()) {
            Direction facing = blockState.getValue(BlockStateProperties.HORIZONTAL_FACING);

            pPoseStack.pushPose();

            //To center
            pPoseStack.translate(0.5f, 0.666f, 0.5f);
            Vec3i normal = facing.getNormal();
            Vec3 vec3 = Vec3.atLowerCornerOf(normal);
            // To face
            pPoseStack.translate(vec3.scale(-1).x() * 0.5f, 0, vec3.scale(-1).z() * 0.5f);

            Vec3 rotatedVec3 = vec3.yRot((float) ((3 * Math.PI) / 2f));
            // To left
            pPoseStack.translate(rotatedVec3.x() * 0.5f, 0, rotatedVec3.z() * 0.5f);

            pPoseStack.mulPose(Axis.YP.rotationDegrees(facing.toYRot()));
            pPoseStack.mulPose(Axis.ZP.rotationDegrees(45));

            itemRenderer.renderStatic(stackInSlot, ItemDisplayContext.FIXED, TCUtil.getLightLevel(level, pBlockEntity.getBlockPos(), Direction.UP),
                    OverlayTexture.NO_OVERLAY, pPoseStack, pBuffer, level, 1);

            pPoseStack.mulPose(Axis.ZP.rotationDegrees(-45));
            pPoseStack.translate(0, -0.333f, 0);
            pPoseStack.mulPose(Axis.ZP.rotationDegrees(45));

            itemRenderer.renderStatic(stackInSlot, ItemDisplayContext.FIXED, TCUtil.getLightLevel(level, pBlockEntity.getBlockPos(), Direction.UP),
                    OverlayTexture.NO_OVERLAY, pPoseStack, pBuffer, level, 1);

            pPoseStack.popPose();
        }
    }

}
