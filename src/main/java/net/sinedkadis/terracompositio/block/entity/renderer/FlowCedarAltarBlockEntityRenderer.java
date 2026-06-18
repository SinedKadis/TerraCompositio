package net.sinedkadis.terracompositio.block.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.EmptyHandler;
import net.sinedkadis.terracompositio.block.entity.FlowCedarAltarBlockEntity;
import org.jetbrains.annotations.NotNull;

import static net.sinedkadis.terracompositio.util.helpers.WorldHelper.getLightLevel;

public class FlowCedarAltarBlockEntityRenderer implements BlockEntityRenderer<FlowCedarAltarBlockEntity> {
    public FlowCedarAltarBlockEntityRenderer(BlockEntityRendererProvider.Context context) {

    }


    @Override
    public void render(FlowCedarAltarBlockEntity pBlockEntity,
                       float pPartialTick,
                       @NotNull PoseStack pPoseStack,
                       @NotNull MultiBufferSource pBuffer,
                       int pPackedLight,
                       int pPackedOverlay) {
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        IItemHandler iItemHandler = pBlockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).orElse(EmptyHandler.INSTANCE);
        ItemStack stack1 = iItemHandler.getStackInSlot(0);
        ItemStack stack2 = iItemHandler.getStackInSlot(1);

        Level level = pBlockEntity.getLevel();
        if (level == null) return;

        if (!stack1.isEmpty()) {
            pPoseStack.pushPose();
            pPoseStack.translate(0.5f, 0.5f, 0.5f);
            if (!stack2.isEmpty()) {
                pPoseStack.translate(0.2f, 0, 0.2f);
            }

            pPoseStack.scale(0.5f, 0.5f, 0.5f);

            pPoseStack.mulPose(Axis.YP.rotation((level.getGameTime() + pPartialTick) / 100));
            itemRenderer.renderStatic(stack1, ItemDisplayContext.FIXED, getLightLevel(level, pBlockEntity.getBlockPos(), null),
                    OverlayTexture.NO_OVERLAY, pPoseStack, pBuffer, level, 1);
            pPoseStack.popPose();
        }

        if (!stack2.isEmpty()) {
            pPoseStack.pushPose();
            pPoseStack.translate(0.5f, 0.5f, 0.5f);
            if (!stack1.isEmpty()) {
                pPoseStack.translate(-0.2f, 0, -0.2f);
            }

            pPoseStack.scale(0.5f, 0.5f, 0.5f);

            pPoseStack.mulPose(Axis.YP.rotation((level.getGameTime() + pPartialTick) / 100));
            itemRenderer.renderStatic(stack2, ItemDisplayContext.FIXED, getLightLevel(pBlockEntity.getLevel(), pBlockEntity.getBlockPos(), null),
                    OverlayTexture.NO_OVERLAY, pPoseStack, pBuffer, pBlockEntity.getLevel(), 1);
            pPoseStack.popPose();
        }
    }
}
