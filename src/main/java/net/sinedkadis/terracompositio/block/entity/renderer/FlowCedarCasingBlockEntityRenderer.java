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
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.sinedkadis.terracompositio.api.helpers.WorldHelper;
import net.sinedkadis.terracompositio.api.registries.TCCapabilities;
import net.sinedkadis.terracompositio.block.custom.FlowCedarCasingBlock;
import net.sinedkadis.terracompositio.block.entity.FlowCedarCasingBlockEntity;
import net.sinedkadis.terracompositio.registries.TCItems;
import org.jetbrains.annotations.NotNull;

public class FlowCedarCasingBlockEntityRenderer implements BlockEntityRenderer<FlowCedarCasingBlockEntity> {
    public FlowCedarCasingBlockEntityRenderer(BlockEntityRendererProvider.Context ignoredContext) {

    }


    @Override
    public void render(FlowCedarCasingBlockEntity pBlockEntity,
                       float pPartialTick,
                       @NotNull PoseStack pPoseStack,
                       @NotNull MultiBufferSource pBuffer,
                       int pPackedLight,
                       int pPackedOverlay) {
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        ItemStack renderStack = pBlockEntity.getRenderStack();
        BlockState blockState = pBlockEntity.getBlockState();

        Level level = pBlockEntity.getLevel();

        @SuppressWarnings("DataFlowIssue")
        IItemHandler iItemHandler = pBlockEntity.getCapability(TCCapabilities.ITEM_STATE_HOLDER).orElse(null);
        if (!(iItemHandler instanceof ItemStackHandler itemStackHandler)) return;
        if (level == null) return;
        renderItemInPort(pBlockEntity, pPoseStack, pBuffer, level, blockState, itemRenderer, renderStack);
        boolean renderedInputBus = renderInputBus(pBlockEntity, pPoseStack, pBuffer, level, blockState, itemRenderer, itemStackHandler);
        boolean renderedOutputBus = renderOutputBus(pBlockEntity, pPoseStack, pBuffer, level, blockState, itemRenderer, itemStackHandler);
        if (renderedInputBus) {
            renderInputConnectionBus(pBlockEntity, pPoseStack, pBuffer, level, blockState, itemRenderer, itemStackHandler);
        }
        if (renderedOutputBus) {
            renderOutputConnectionBus(pBlockEntity, pPoseStack, pBuffer, level, blockState, itemRenderer, itemStackHandler);
        }
    }

    private void renderOutputConnectionBus(FlowCedarCasingBlockEntity pBlockEntity,
                                           @NotNull PoseStack pPoseStack,
                                           @NotNull MultiBufferSource pBuffer,
                                           Level level,
                                           BlockState ignoredBlockState,
                                           ItemRenderer itemRenderer,
                                           ItemStackHandler itemStackHandler) {
        Direction facing = pBlockEntity.attachedDir();
        if (facing.getAxis().isHorizontal()) {
            ItemStack stackInSlot = itemStackHandler.getStackInSlot(FlowCedarCasingBlockEntity.DOWN_CONNECTION_SLOT);
            if (stackInSlot.isEmpty()) return;

            {
                pPoseStack.pushPose();

                //Bottom center
                pPoseStack.translate(0.5f, 0.0f, 0.5f);
                Vec3i normal = facing.getNormal();
                //Move to edges
                float k = -0.1f;
                pPoseStack.translate(normal.getX() * k, 0, normal.getZ() * k);
                Vec3 vec3 = Vec3.atLowerCornerOf(normal);
                Vec3 rotatedVec3 = vec3.yRot((float) ((Math.PI) / 2f));
                //Move lil right
                pPoseStack.translate(rotatedVec3.x() * 0.1f, 0, rotatedVec3.z() * 0.1f);

                pPoseStack.mulPose(Axis.ZP.rotationDegrees(180));
                pPoseStack.mulPose(Axis.YP.rotationDegrees(facing.toYRot() + 90));
                pPoseStack.mulPose(Axis.YP.rotationDegrees(-45));
                pPoseStack.mulPose(Axis.XP.rotationDegrees(90));

                itemRenderer.renderStatic(TCItems.HALF_ROD.get().getDefaultInstance(), ItemDisplayContext.FIXED, WorldHelper.getLightLevel(level, pBlockEntity.getBlockPos(), Direction.DOWN),
                        OverlayTexture.NO_OVERLAY, pPoseStack, pBuffer, level, 1);

                pPoseStack.popPose();
            }

            {
                pPoseStack.pushPose();

                //To center
                pPoseStack.translate(0.5f, 0.55f, 0.5f);
                Vec3i normal = facing.getNormal();
                Vec3 vec3 = Vec3.atLowerCornerOf(normal);
                //To MI cords
                pPoseStack.translate(normal.getX(), -0.02f, normal.getZ());
                // To face
                pPoseStack.translate(vec3.scale(-1).x() * 0.5f, 0, vec3.scale(-1).z() * 0.5f);

                Vec3 rotatedVec3 = vec3.yRot((float) ((Math.PI) / 2f));
                // To right
                pPoseStack.translate(rotatedVec3.x() * 0.1f, 0, rotatedVec3.z() * 0.1f);

                pPoseStack.mulPose(Axis.YN.rotationDegrees(facing.toYRot()));
                pPoseStack.mulPose(Axis.ZP.rotationDegrees(135));

                itemRenderer.renderStatic(TCItems.HALF_ROD.get().getDefaultInstance(), ItemDisplayContext.FIXED, WorldHelper.getLightLevel(level, pBlockEntity.getBlockPos(), facing),
                        OverlayTexture.NO_OVERLAY, pPoseStack, pBuffer, level, 1);

                pPoseStack.popPose();
            }
        }


    }

    private void renderInputConnectionBus(FlowCedarCasingBlockEntity pBlockEntity,
                                          @NotNull PoseStack pPoseStack,
                                          @NotNull MultiBufferSource pBuffer,
                                          Level level,
                                          BlockState ignoredBlockState,
                                          ItemRenderer itemRenderer,
                                          ItemStackHandler itemStackHandler) {
        Direction facing = pBlockEntity.attachedDir();
        if (facing.getAxis().isHorizontal()) {
            ItemStack stackInSlot = itemStackHandler.getStackInSlot(FlowCedarCasingBlockEntity.UP_CONNECTION_SLOT);
            if (stackInSlot.isEmpty()) return;
            {
                pPoseStack.pushPose();

                //Top center
                pPoseStack.translate(0.5f, 1.0f, 0.5f);
                Vec3i normal = facing.getNormal();
                //Move to edges
                float move1 = -0.1f;
                pPoseStack.translate(normal.getX() * move1, 0, normal.getZ() * move1);
                Vec3 vec3 = Vec3.atLowerCornerOf(normal);
                Vec3 rotatedVec3 = vec3.yRot((float) ((3 * Math.PI) / 2f)).normalize();
                //Move lil left
                pPoseStack.translate(rotatedVec3.x() * 0.1f, 0, rotatedVec3.z() * 0.1f);

                pPoseStack.mulPose(Axis.ZP.rotationDegrees(180));
                pPoseStack.mulPose(Axis.YP.rotationDegrees(facing.toYRot() + 90));
                pPoseStack.mulPose(Axis.YP.rotationDegrees(-45));
                pPoseStack.mulPose(Axis.XP.rotationDegrees(90));


                itemRenderer.renderStatic(TCItems.HALF_ROD.get().getDefaultInstance(), ItemDisplayContext.FIXED, WorldHelper.getLightLevel(level, pBlockEntity.getBlockPos(), Direction.UP),
                        OverlayTexture.NO_OVERLAY, pPoseStack, pBuffer, level, 1);

                pPoseStack.popPose();
            }
            {
                pPoseStack.pushPose();

                //To center
                pPoseStack.translate(0.5f, 0.45f, 0.5f);
                Vec3i normal = facing.getNormal();
                Vec3 vec3 = Vec3.atLowerCornerOf(normal);
                //To MI cords
                pPoseStack.translate(normal.getX(), -0.02f, normal.getZ());
                // To face
                pPoseStack.translate(vec3.scale(-1).x() * 0.5f, 0, vec3.scale(-1).z() * 0.5f);

                Vec3 rotatedVec3 = vec3.yRot((float) ((3 * Math.PI) / 2f)).normalize();
                // To left
                pPoseStack.translate(rotatedVec3.x() * 0.1f, 0, rotatedVec3.z() * 0.1f);

                pPoseStack.mulPose(Axis.YN.rotationDegrees(facing.toYRot() + 180));
                pPoseStack.mulPose(Axis.ZP.rotationDegrees(-45));

                itemRenderer.renderStatic(TCItems.HALF_ROD.get().getDefaultInstance(), ItemDisplayContext.FIXED, WorldHelper.getLightLevel(level, pBlockEntity.getBlockPos(), facing),
                        OverlayTexture.NO_OVERLAY, pPoseStack, pBuffer, level, 1);

                pPoseStack.popPose();
            }
        }

    }

    private boolean renderOutputBus(FlowCedarCasingBlockEntity pBlockEntity,
                                    @NotNull PoseStack pPoseStack,
                                    @NotNull MultiBufferSource pBuffer,
                                    Level level,
                                    BlockState ignoredBlockState,
                                    ItemRenderer itemRenderer,
                                    ItemStackHandler itemStackHandler) {
        ItemStack stackInSlot = itemStackHandler.getStackInSlot(FlowCedarCasingBlockEntity.OUTPUT_BUS_SLOT);
        if (!stackInSlot.isEmpty()) {

            pPoseStack.pushPose();

            pPoseStack.translate(0.5f, -0.05f, 0.5f);
            pPoseStack.mulPose(Axis.XP.rotationDegrees(90));

            itemRenderer.renderStatic(stackInSlot, ItemDisplayContext.FIXED, WorldHelper.getLightLevel(level, pBlockEntity.getBlockPos(), Direction.DOWN),
                    OverlayTexture.NO_OVERLAY, pPoseStack, pBuffer, level, 1);

            pPoseStack.popPose();
            return true;
        }
        return false;

    }

    private boolean renderInputBus(FlowCedarCasingBlockEntity pBlockEntity,
                                   @NotNull PoseStack pPoseStack,
                                   @NotNull MultiBufferSource pBuffer,
                                   Level level,
                                   BlockState ignoredBlockState,
                                   ItemRenderer itemRenderer,
                                   ItemStackHandler itemStackHandler) {
        ItemStack stackInSlot = itemStackHandler.getStackInSlot(FlowCedarCasingBlockEntity.INPUT_BUS_SLOT);
        if (!stackInSlot.isEmpty()) {

            pPoseStack.pushPose();

            pPoseStack.translate(0.5f, 1.05f, 0.5f);
            pPoseStack.mulPose(Axis.XP.rotationDegrees(90));

            itemRenderer.renderStatic(stackInSlot, ItemDisplayContext.FIXED, WorldHelper.getLightLevel(level, pBlockEntity.getBlockPos(), Direction.UP),
                    OverlayTexture.NO_OVERLAY, pPoseStack, pBuffer, level, 1);

            pPoseStack.popPose();
            return true;
        }
        return false;
    }

    private void renderItemInPort(FlowCedarCasingBlockEntity pBlockEntity,
                                  @NotNull PoseStack pPoseStack,
                                  @NotNull MultiBufferSource pBuffer,
                                  Level level,
                                  BlockState blockState,
                                  ItemRenderer itemRenderer,
                                  ItemStack itemStack) {
        Boolean portAttached = FlowCedarCasingBlock.isPortAttached(level, blockState, pBlockEntity.getBlockPos());
        if (portAttached != null) {

            Direction facing;
            if (portAttached)
                facing = Direction.get(Direction.AxisDirection.POSITIVE, blockState.getValue(BlockStateProperties.AXIS)).getClockWise();
            else
                facing = Direction.get(Direction.AxisDirection.POSITIVE, blockState.getValue(BlockStateProperties.AXIS)).getCounterClockWise();

            pPoseStack.pushPose();
            switch (facing) {
                case EAST -> pPoseStack.translate(1.05f, 0.5f, 0.5f);
                case NORTH -> pPoseStack.translate(0.5f, 0.5f, -0.05f);
                case WEST -> pPoseStack.translate(-0.05f, 0.5f, 0.5f);
                default -> pPoseStack.translate(0.5f, 0.5f, 1.05f);
            }
            pPoseStack.scale(0.5f, 0.5f, 0.5f);
            pPoseStack.mulPose(Axis.YP.rotationDegrees(switch (facing) {
                case WEST -> 90;
                case SOUTH -> 180;
                case EAST -> 270;
                default -> 0;
            }));
            itemRenderer.renderStatic(itemStack, ItemDisplayContext.FIXED, WorldHelper.getLightLevel(level, pBlockEntity.getBlockPos(), facing),
                    OverlayTexture.NO_OVERLAY, pPoseStack, pBuffer, level, 1);
            pPoseStack.popPose();
        }
    }
}
