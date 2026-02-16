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
import net.sinedkadis.terracompositio.api.behaviors.blockentity.IBEBehaviour;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetworkMemberBE;
import net.sinedkadis.terracompositio.api.networks.cfe.ICFEHandler;
import net.sinedkadis.terracompositio.block.entity.PathPointerBlockEntity;
import net.sinedkadis.terracompositio.block.entity.TCBlockEntity;
import net.sinedkadis.terracompositio.cfe.RedirectCFEHandler;
import net.sinedkadis.terracompositio.compat.jade.JadeTerraCompositioPlugin;
import net.sinedkadis.terracompositio.util.BehaviourCapabilities;
import net.sinedkadis.terracompositio.util.TCUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class SenderBehaviour extends AbstractPPBehaviour implements CFENetworkMemberBE {

    @Getter
    private RedirectCFEHandler redirectCFEHandler = null;
    private LazyOptional<SenderBehaviour> senderBehaviourLazyOptional = LazyOptional.empty();
    private LazyOptional<ICFEHandler> redirectCFEHandlerLazyOptional = LazyOptional.empty();

    public SenderBehaviour(PathPointerBlockEntity blockEntity) {
        super(blockEntity);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        schedulePPNetworkUpdate();
        verifyHandler();
        sendCFE();
    }

    private void schedulePPNetworkUpdate() {
        BlockPos bindPos = getBindPos();
        if (bindPos == null) return;
        Level level = getBlockEntity().getLevel();
        if (level == null) return;
        BlockEntity target = level.getBlockEntity(getBindPos());
        if (target == null) return;
        if (target instanceof TCBlockEntity tcBlockEntity) {
            tcBlockEntity.getBehaviours().forEach(IBEBehaviour::onUpdate);
        }
    }

    @Override
    public void onChunkLoad() {
        super.onChunkLoad();
        senderBehaviourLazyOptional = LazyOptional.of(() -> this);
        redirectCFEHandlerLazyOptional = LazyOptional.of(() -> redirectCFEHandler);
    }

    @Override
    public void onInvalidateCaps() {
        super.onInvalidateCaps();
        senderBehaviourLazyOptional.invalidate();
        redirectCFEHandlerLazyOptional.invalidate();
    }

    private void verifyHandler() {
        if (redirectCFEHandler != null) return;
        Level level = getBlockEntity().getLevel();
        if (level == null) return;
        BlockPos bindPos = getBindPos();
        if (bindPos == null) return;
        BlockEntity target = level.getBlockEntity(bindPos);
        if (target == null) return;
        LazyOptional<ICFEHandler> resolve = target.getCapability(TCCapabilities.CFE);
        resolve.ifPresent(icfeHandler ->
                redirectCFEHandler = new RedirectCFEHandler(this, icfeHandler){
                    @Override
                    public int addCFE(int cfe, boolean simulate) {
                        int i = super.addCFE(cfe, simulate);
                        if (!simulate) getAttachedMember().scheduleMemberUpdate();
                        return i;
                    }
                });
    }

    @Override
    public void onRemoved() {
        Level level = blockEntity.getLevel();
        BlockPos bindPos = getBindPos();
        if (level != null && bindPos != null) {
            BlockEntity receiverEntity = level.getBlockEntity(bindPos);
            if (receiverEntity instanceof PathPointerBlockEntity pathPointerBlockEntity) {
                pathPointerBlockEntity.getCapability(BehaviourCapabilities.RECEIVER).ifPresent(receiverBehaviour ->
                        receiverBehaviour.getSenderPoses().remove(blockEntity.getBlockPos()));
            }
        }
        super.onRemoved();
    }

    protected void sendCFE() {
        blockEntity.getCapability(TCCapabilities.CFE).ifPresent(icfeHandler -> {
            if (icfeHandler.getCFE() > 0) {
                Level level = blockEntity.getLevel();
                if (level != null) {
                    BlockEntity target = level.getBlockEntity(getBindPos());
                    if (target != null) {
                        target.getCapability(TCCapabilities.CFE).ifPresent(targetHandler ->
                                TCUtil.tryCFETransfer(targetHandler,icfeHandler));
                    }
                }
            }
        });
    }


    @Override
    public @Nullable LazyOptional<?> getCapability(@NotNull Capability<?> cap, @Nullable Direction side) {
        if (cap == BehaviourCapabilities.SENDER) {
            return senderBehaviourLazyOptional.cast();
        }
        if (cap == TCCapabilities.CFE) {
            if (blockEntity.getCapability(BehaviourCapabilities.RECEIVER).isPresent()) {
                return redirectCFEHandlerLazyOptional;
            }
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void onAppendServerData(CompoundTag compoundTag) {
        super.onAppendServerData(compoundTag);
        compoundTag.put("bindPos", TCUtil.saveBlockPos(getBindPos()));
    }

    @Override
    public void onAppendTooltip(ITooltip iTooltip, CompoundTag serverData, IPluginConfig iPluginConfig) {
        super.onAppendTooltip(iTooltip, serverData, iPluginConfig);
        if (serverData.contains("bindPos") && iPluginConfig.get(JadeTerraCompositioPlugin.debugConfigRL())) {
            BlockPos pos = TCUtil.loadBlockPos(serverData.getCompound("bindPos"));
            iTooltip.add(Component.literal("BindPos: " + pos.getX() + " " + pos.getY() + " " + pos.getZ()));
        }
    }

    public BlockPos getBindPos() {
        CompoundTag persistentData = blockEntity.getPersistentData();
        if (!persistentData.contains("bindPos")) return null;
        return TCUtil.loadBlockPos(persistentData.getCompound("bindPos"));
    }

    public void setBindPos(BlockPos bindPos) {
        blockEntity.getPersistentData().put("bindPos",TCUtil.saveBlockPos(bindPos));
    }

    @Override
    public int getLimit() {
        return 5;
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public BlockEntity getEntity() {
        return blockEntity;
    }

    @Override
    public ICFEHandler getMainHandler() {
        return redirectCFEHandler;
    }
}
