package net.sinedkadis.terracompositio.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import net.sinedkadis.terracompositio.api.TCCapabilities;
import net.sinedkadis.terracompositio.api.dummies.DummyCFEHandler;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetworkMemberEntity;
import net.sinedkadis.terracompositio.api.networks.cfe.ICFEHandler;
import net.sinedkadis.terracompositio.block.custom.TechnetiumBoardBlock;
import net.sinedkadis.terracompositio.config.TCInnerConfig;
import net.sinedkadis.terracompositio.network.TCPackets;
import net.sinedkadis.terracompositio.network.packets.S2CAddPlayerKnowledge;
import net.sinedkadis.terracompositio.network.packets.S2CPlayerCfeContainerSync;
import net.sinedkadis.terracompositio.util.accessors.PlayerKnowledgeAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(Player.class)
public class PlayerMixin implements CFENetworkMemberEntity, PlayerKnowledgeAccessor {
    @Unique
    boolean creationKnowledge = false;

    @Unique
    boolean scheduleUpdate = false;

    @Override
    public Level getLevel() {
        return ((Player)(Object)this).level();
    }

    @Override
    public BlockPos getPos() {
        return ((Player)(Object)this).blockPosition();
    }

    @Override
    public void updateIfScheduled() {
        if (scheduleUpdate){
            scheduleUpdate = false;
            onCFENetworkMemberUpdate();
        }
    }

    @Override
    public void scheduleMemberUpdate() {
        scheduleUpdate = true;
    }

    @Override
    public int getRange() {
        return 10;
    }

    @Override
    public int getPriority() {
        return TCInnerConfig.DEFAULT_CONSUMER_PRIORITY;
    }

    @Override
    public ICFEHandler getMainHandler() {
        return ((Player)(Object)this).getCapability(TCCapabilities.CFE).orElse(DummyCFEHandler.instance);
    }

    @Unique
    private boolean technetium$fakeSneak = false;


    @Inject(
            method = "isStayingOnGroundSurface()Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private void technetium$isOnGround(CallbackInfoReturnable<Boolean> cir) {
        Player self = (Player) (Object) this;
        boolean found = false;
        for (BlockPos pos : BlockPos.betweenClosed(self.blockPosition().offset(-1, -1, -1), self.blockPosition().offset(1, -1, 1))) {
            if ((self.level().getBlockState(pos).getBlock() instanceof TechnetiumBoardBlock)) {
                found = true;
                break;
            }
        }
        if (!found) return;

        technetium$fakeSneak = true;
        cir.setReturnValue(true);
    }

    @Inject(
            method = "maybeBackOffFromEdge",
            at = @At("RETURN")
    )
    private void technetium$edgeSafeTail(Vec3 movement, MoverType mover, CallbackInfoReturnable<Vec3> cir) {
        if (technetium$fakeSneak) {
            ((Player) (Object) this).setShiftKeyDown(false);
            technetium$fakeSneak = false;
        }
    }


    @Override
    public boolean isCreationAcknowledged() {
        return creationKnowledge;
    }

    @Override
    public void setCreationKnowledge(boolean knowledge) {
        creationKnowledge = knowledge;
    }

    @Unique
    boolean wasSent = false;

    @Inject(
            method = "tick()V",
            at = @At("RETURN")
    )
    private void tc$onTick(CallbackInfo ci) {
        if (!wasSent)
            if (((Player) (Object) this) instanceof ServerPlayer serverPlayer) {
                wasSent = true;
                if (((PlayerKnowledgeAccessor) serverPlayer).isCreationAcknowledged())
                    TCPackets.CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer),
                        new S2CAddPlayerKnowledge());
                TCPackets.CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer),
                        new S2CPlayerCfeContainerSync(((CFENetworkMemberEntity) serverPlayer).getMainHandler().getCFE()));
            }
    }

    @Inject(
            method = "readAdditionalSaveData",
            at = @At("HEAD")
    )
    private void tc$onLoad(CompoundTag pCompound, CallbackInfo ci) {
        setCreationKnowledge(pCompound.getBoolean("tc_creation_knowledge"));
    }

    @Inject(
            method = "addAdditionalSaveData",
            at = @At("RETURN")
    )
    private void tc$onSave(CompoundTag pCompound, CallbackInfo ci) {
        pCompound.putBoolean("tc_creation_knowledge", isCreationAcknowledged());
    }
}
