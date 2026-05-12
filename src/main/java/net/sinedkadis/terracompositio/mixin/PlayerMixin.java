package net.sinedkadis.terracompositio.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.sinedkadis.terracompositio.api.TCCapabilities;
import net.sinedkadis.terracompositio.api.dummies.DummyCFEHandler;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetworkMemberEntity;
import net.sinedkadis.terracompositio.api.networks.cfe.ICFEHandler;
import net.sinedkadis.terracompositio.block.custom.TechnetiumBoardBlock;
import net.sinedkadis.terracompositio.config.TCInnerConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(Player.class)
public class PlayerMixin implements CFENetworkMemberEntity {
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
}
