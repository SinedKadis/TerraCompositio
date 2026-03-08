package net.sinedkadis.terracompositio.block.behaviours.pp;

import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.sinedkadis.terracompositio.api.TCCapabilities;
import net.sinedkadis.terracompositio.api.dummies.DummyCFEHandler;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetworkMemberBE;
import net.sinedkadis.terracompositio.api.networks.cfe.ICFEHandler;
import net.sinedkadis.terracompositio.block.entity.PathPointerBlockEntity;
import net.sinedkadis.terracompositio.cfe.RedirectCFEHandler;
import net.sinedkadis.terracompositio.compat.jade.JadeTerraCompositioPlugin;
import net.sinedkadis.terracompositio.util.TCUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import java.util.ArrayList;
import java.util.List;

public class SenderBehaviour extends AbstractPPBehaviour implements CFENetworkMemberBE {

    @Getter
    private final RedirectCFEHandler redirectCFEHandler = new RedirectCFEHandler(this, DummyCFEHandler.instance);
    private LazyOptional<ICFEHandler> redirectCFEHandlerLazyOptional = LazyOptional.empty();

    public SenderBehaviour(PathPointerBlockEntity blockEntity) {
        super(blockEntity);
    }

    @Override
    public void onUpdate() {
        scheduleBindPosPPNetworkUpdate();
        verifyHandler();
    }

    private void scheduleBindPosPPNetworkUpdate() {
        BlockPos bindPos = getBindPos(blockEntity);
        if (bindPos == null) return;
        Level level = getBlockEntity().getLevel();
        if (level == null) return;
        BlockEntity target = level.getBlockEntity(bindPos);
        if (target instanceof PathPointerBlockEntity ppBE)
            ppBE.setPpUpdateScheduled(true);
    }

    @Override
    public void onChunkLoad() {
        super.onChunkLoad();
        redirectCFEHandlerLazyOptional = LazyOptional.of(() -> redirectCFEHandler);
    }

    @Override
    public void onInvalidateCaps() {
        super.onInvalidateCaps();
        redirectCFEHandlerLazyOptional.invalidate();
    }

    private void verifyHandler() {
        if (!(redirectCFEHandler.getRedirectedHandler() instanceof DummyCFEHandler)) return;
        Level level = getBlockEntity().getLevel();
        if (level == null) return;
        BlockPos bindPos = getBindPos(blockEntity);
        if (bindPos == null) return;
        BlockEntity target = level.getBlockEntity(bindPos);
        if (target == null) return;
        LazyOptional<ICFEHandler> resolve = target.getCapability(TCCapabilities.CFE);
        resolve.ifPresent(redirectCFEHandler::setRedirectedHandler);
    }

    @Override
    public void onRemoved() {
        Level level = blockEntity.getLevel();
        BlockPos bindPos = getBindPos(blockEntity);
        if (level != null && bindPos != null) {
            BlockEntity receiverEntity = level.getBlockEntity(bindPos);
            if (receiverEntity instanceof PathPointerBlockEntity pathPointerBlockEntity) {
                List<BlockPos> senderPoses = new ArrayList<>(ReceiverBehaviour.getSenderPoses(pathPointerBlockEntity));
                senderPoses.remove(blockEntity.getBlockPos());
                ReceiverBehaviour.setSenderPoses(pathPointerBlockEntity,
                        senderPoses);
            }
        }
        super.onRemoved();
    }

    @Override
    public boolean isActive() {
        return blockEntity.parts.contains(PathPointerBlockEntity.PPPart.SENDER);
    }


    @Override
    public @Nullable LazyOptional<?> getCapability(@NotNull Capability<?> cap, @Nullable Direction side) {
        if (cap == TCCapabilities.CFE && blockEntity.parts.contains(PathPointerBlockEntity.PPPart.RECEIVER)) {
            return redirectCFEHandlerLazyOptional;
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void onAppendServerData(CompoundTag compoundTag) {
        super.onAppendServerData(compoundTag);
        compoundTag.put("bindPos", TCUtil.saveBlockPos(getBindPos(blockEntity)));
    }

    @Override
    public void onAppendTooltip(ITooltip iTooltip, CompoundTag serverData, IPluginConfig iPluginConfig) {
        super.onAppendTooltip(iTooltip, serverData, iPluginConfig);
        if (serverData.contains("bindPos") && iPluginConfig.get(JadeTerraCompositioPlugin.debugConfigRL())) {
            BlockPos pos = TCUtil.loadBlockPos(serverData.getCompound("bindPos"));
            iTooltip.add(Component.literal("BindPos: " + pos.getX() + " " + pos.getY() + " " + pos.getZ()));
        }
    }

    public static BlockPos getBindPos(BlockEntity blockEntity) {
        CompoundTag persistentData = blockEntity.getPersistentData();
        if (!persistentData.contains("bindPos")) return null;
        return TCUtil.loadBlockPos(persistentData.getCompound("bindPos"));
    }

    public static void setBindPos(BlockEntity blockEntity,BlockPos bindPos) {
        blockEntity.getPersistentData().put("bindPos",TCUtil.saveBlockPos(bindPos));
    }

    @Override
    public int getRange() {
        return 5;
    }

    @Override
    public int getPriority() {
        return 200;
    }

    @Override
    public BlockEntity getEntity() {
        return blockEntity;
    }

    @Override
    public ICFEHandler getMainHandler() {
        return redirectCFEHandler;
    }

    @Override
    public void updateIfScheduled() {

    }

    @Override
    public void scheduleMemberUpdate() {
        blockEntity.setPpUpdateScheduled(true);
    }
}
