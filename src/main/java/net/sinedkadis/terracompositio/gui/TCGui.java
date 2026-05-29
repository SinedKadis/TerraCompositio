package net.sinedkadis.terracompositio.gui;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.api.equipment.goggles.IHaveCustomOverlayIcon;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.api.equipment.goggles.IHaveHoveringInformation;
import com.simibubi.create.api.equipment.goggles.IProxyHoveringInformation;
import com.simibubi.create.compat.Mods;
import com.simibubi.create.content.contraptions.IDisplayAssemblyExceptions;
import com.simibubi.create.content.contraptions.piston.MechanicalPistonBlock;
import com.simibubi.create.content.contraptions.piston.PistonExtensionPoleBlock;
import com.simibubi.create.content.equipment.goggles.GogglesItem;
import com.simibubi.create.content.trains.entity.TrainRelocator;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBox;
import com.simibubi.create.foundation.gui.RemovedGuiUtils;
import com.simibubi.create.foundation.mixin.accessor.MouseHandlerAccessor;
import com.simibubi.create.foundation.utility.CreateLang;
import com.simibubi.create.infrastructure.config.AllConfigs;
import com.simibubi.create.infrastructure.config.CClient;
import net.createmod.catnip.data.Iterate;
import net.createmod.catnip.gui.element.BoxElement;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.createmod.catnip.outliner.Outline;
import net.createmod.catnip.outliner.Outliner;
import net.createmod.catnip.theme.Color;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.api.dummies.DummyCFEHandler;
import net.sinedkadis.terracompositio.api.TCCapabilities;
import net.sinedkadis.terracompositio.api.networks.cfe.ICFEHandler;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TCGui {
    public static final ResourceLocation CFE_HUD = TerraCompositio.modLoc("textures/gui/cfe_hud.png");
    public static final ResourceLocation KNOWLEDGE_HUD = TerraCompositio.modLoc("textures/gui/knowledge_hud.png");

    public static void cfeHud(ForgeGui gui,
                              GuiGraphics guiGraphics,
                              float partialTick,
                              int screenWidth,
                              int screenHeight) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        Inventory inventory = player.getInventory();
        ICFEHandler playerHandler = player.getCapability(TCCapabilities.CFE).orElse(DummyCFEHandler.instance);
        int cfeTotal = playerHandler.getCFE();
        int cfeMaxTotal = playerHandler.getMaxCFE();
        for (ItemStack itemStack : inventory.armor) {
            ICFEHandler handler = itemStack.getCapability(TCCapabilities.CFE).orElse(DummyCFEHandler.instance);
            if (!(handler instanceof DummyCFEHandler)) {
                cfeTotal += handler.getCFE()-1;
                cfeMaxTotal += handler.getMaxCFE()-1;
            }
        }

        int width = 50;
        int x = screenWidth / 2 + 90;
        int y = screenHeight - 15;

        if (cfeMaxTotal == 0) {
            width = 0;
        } else {
            float width1 = width * ( (float) cfeTotal /  cfeMaxTotal);
            width = Math.round(width1);
        }

        if (width == 0) {
            if (cfeTotal > 0) {
                width = 1;
            } else {
                return;
            }
        }

        int color = Mth.hsvToRgb(0.55F, (float) Math.min(1F, Math.sin(Util.getMillis() / 200D) * 0.5 + 1F), 1F);
        int r = (color >> 16 & 0xFF);
        int g = (color >> 8 & 0xFF);
        int b = color & 0xFF;
        RenderSystem.setShaderColor(r / 255F, g / 255F, b / 255F, 1 - (r / 255F));

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        guiGraphics.blit(CFE_HUD, x+10, y, 0, 0, 0, width,40, 256, 256);
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1, 1, 1, 1);
    }

    private static final Map<Object, Outliner.OutlineEntry> outlines = Outliner.getInstance().getOutlines();

    public static int hoverTicks = 0;

    public static BlockPos lastHovered = null;

    public static void knowledgeOverlay(ForgeGui gui, GuiGraphics graphics, float partialTicks, int width,
                                        int height) {

        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || mc.gameMode.getPlayerMode() == GameType.SPECTATOR)
            return;
        HitResult objectMouseOver = mc.hitResult;
        if (!(objectMouseOver instanceof BlockHitResult result)) {
            lastHovered = null;
            hoverTicks = 0;
            return;
        }
        for (Outliner.OutlineEntry entry : outlines.values()) {
            if (!entry.isAlive())
                continue;
            Outline outline = entry.getOutline();
            if (outline instanceof ValueBox && !((ValueBox) outline).isPassive)
                return;
        }
        ClientLevel world = mc.level;
        BlockPos pos = result.getBlockPos();
        int prevHoverTicks = hoverTicks;
        hoverTicks++;
        lastHovered = pos;
        pos = proxiedOverlayPosition(world, pos);
        BlockEntity be = world.getBlockEntity(pos);
        boolean wearingGoggles = GogglesItem.isWearingGoggles(mc.player);
        boolean isShifting = mc.player.isShiftKeyDown();
        boolean hasGoggleInformation = be instanceof IHaveGoggleInformation;
        boolean hasHoveringInformation = be instanceof IHaveHoveringInformation;
        boolean goggleAddedInformation = false;
        boolean hoverAddedInformation = false;
        ItemStack item = AllItems.GOGGLES.asStack();
        List<Component> tooltip = new ArrayList<>();
        if (be instanceof IHaveCustomOverlayIcon customOverlayIcon)
            item = customOverlayIcon.getIcon(isShifting);
        if (hasGoggleInformation && wearingGoggles) {
            IHaveGoggleInformation gte = (IHaveGoggleInformation) be;
            goggleAddedInformation = gte.addToGoggleTooltip(tooltip, isShifting);
        }
        if (hasHoveringInformation) {
            if (!tooltip.isEmpty())
                tooltip.add(CommonComponents.EMPTY);
            IHaveHoveringInformation hte = (IHaveHoveringInformation) be;
            hoverAddedInformation = hte.addToTooltip(tooltip, isShifting);
            if (goggleAddedInformation && !hoverAddedInformation)
                tooltip.remove(tooltip.size() - 1);
        }
        if (be instanceof IDisplayAssemblyExceptions) {
            boolean exceptionAdded = ((IDisplayAssemblyExceptions) be).addExceptionToTooltip(tooltip);
            if (exceptionAdded) {
                hasHoveringInformation = true;
                hoverAddedInformation = true;
            }
        }
        if (!hasHoveringInformation)
            if (hasHoveringInformation =
                    hoverAddedInformation = TrainRelocator.addToTooltip(tooltip, isShifting))
                hoverTicks = prevHoverTicks + 1;
        // break early if goggle or hover returned false when present
        if ((hasGoggleInformation && !goggleAddedInformation) && (hasHoveringInformation && !hoverAddedInformation)) {
            hoverTicks = 0;
            return;
        }
        // check for piston poles if goggles are worn
        BlockState state = world.getBlockState(pos);
        if (wearingGoggles && AllBlocks.PISTON_EXTENSION_POLE.has(state)) {
            Direction[] directions = Iterate.directionsInAxis(state.getValue(PistonExtensionPoleBlock.FACING)
                    .getAxis());
            int poles = 1;
            boolean pistonFound = false;
            for (Direction dir : directions) {
                int attachedPoles = PistonExtensionPoleBlock.PlacementHelper.get()
                        .attachedPoles(world, pos, dir);
                poles += attachedPoles;
                pistonFound |= world.getBlockState(pos.relative(dir, attachedPoles + 1))
                        .getBlock() instanceof MechanicalPistonBlock;
            }
            if (!pistonFound) {
                hoverTicks = 0;
                return;
            }
            if (!tooltip.isEmpty())
                tooltip.add(CommonComponents.EMPTY);
            CreateLang.translate("gui.goggles.pole_length")
                    .text(" " + poles)
                    .forGoggles(tooltip);
        }
        if (tooltip.isEmpty()) {
            hoverTicks = 0;
            return;
        }
        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        int tooltipTextWidth = 0;
        for (FormattedText textLine : tooltip) {
            int textLineWidth = mc.font.width(textLine);
            if (textLineWidth > tooltipTextWidth)
                tooltipTextWidth = textLineWidth;
        }
        int tooltipHeight = 8;
        if (tooltip.size() > 1) {
            tooltipHeight += 2; // gap between title lines and next lines
            tooltipHeight += (tooltip.size() - 1) * 10;
        }
        CClient cfg = AllConfigs.client();
        int posX = width / 2 + cfg.overlayOffsetX.get();
        int posY = height / 2 + cfg.overlayOffsetY.get();
        posX = Math.min(posX, width - tooltipTextWidth - 20);
        posY = Math.min(posY, height - tooltipHeight - 20);
        float fade = Mth.clamp((hoverTicks + partialTicks) / 24f, 0, 1);
        Boolean useCustom = cfg.overlayCustomColor.get();
        Color colorBackground = useCustom ? new Color(cfg.overlayBackgroundColor.get())
                : BoxElement.COLOR_VANILLA_BACKGROUND.scaleAlpha(.75f);
        Color colorBorderTop = useCustom ? new Color(cfg.overlayBorderColorTop.get())
                : BoxElement.COLOR_VANILLA_BORDER.getFirst().copy();
        Color colorBorderBot = useCustom ? new Color(cfg.overlayBorderColorBot.get())
                : BoxElement.COLOR_VANILLA_BORDER.getSecond().copy();
        if (fade < 1) {
            poseStack.translate(Math.pow(1 - fade, 3) * Math.signum(cfg.overlayOffsetX.get() + .5f) * 8, 0, 0);
            colorBackground.scaleAlpha(fade);
            colorBorderTop.scaleAlpha(fade);
            colorBorderBot.scaleAlpha(fade);
        }
        GuiGameElement.of(item)
                .at(posX + 10, posY - 16, 450)
                .render(graphics);
        if (!Mods.MODERNUI.isLoaded()) {
            // default tooltip rendering when modernUI is not loaded
            RemovedGuiUtils.drawHoveringText(graphics, tooltip, posX, posY, width, height, -1, colorBackground.getRGB(),
                    colorBorderTop.getRGB(), colorBorderBot.getRGB(), mc.font);
            poseStack.popPose();
            return;
        }
        /*
            * special handling for modernUI
            *
            * their tooltip handler causes the overlay to jiggle each frame,
            * if the mouse is moving, guiScale is anything but 1 and exactPositioning is enabled
            *
            * this is a workaround to fix this behavior
            */
        MouseHandler mouseHandler = Minecraft.getInstance().mouseHandler;
        Window window = Minecraft.getInstance().getWindow();
        double guiScale = window.getGuiScale();
        double cursorX = mouseHandler.xpos();
        double cursorY = mouseHandler.ypos();
        ((MouseHandlerAccessor) mouseHandler).create$setXPos(Math.round(cursorX / guiScale) * guiScale);
        ((MouseHandlerAccessor) mouseHandler).create$setYPos(Math.round(cursorY / guiScale) * guiScale);
        RemovedGuiUtils.drawHoveringText(graphics, tooltip, posX, posY, width, height, -1, colorBackground.getRGB(),
                colorBorderTop.getRGB(), colorBorderBot.getRGB(), mc.font);
        ((MouseHandlerAccessor) mouseHandler).create$setXPos(cursorX);
        ((MouseHandlerAccessor) mouseHandler).create$setYPos(cursorY);
        poseStack.popPose();
    }
    public static BlockPos proxiedOverlayPosition(Level level, BlockPos pos) {
        BlockState targetedState = level.getBlockState(pos);
        if (targetedState.getBlock() instanceof IProxyHoveringInformation proxy)
            return proxy.getInformationSource(level, pos, targetedState);
        return pos;
    }


}
