package net.sinedkadis.terracompositio.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
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

    @Inject(
            method = "maybeBackOffFromEdge",
            at = @At("HEAD"),
            cancellable = true
    )
    private void technetium$edgeSafe(Vec3 movement, MoverType pMover, CallbackInfoReturnable<Vec3> cir) {
        Player self = (Player) (Object) this;

        if (!self.onGround()) return;

        BlockPos below = BlockPos.containing(
                self.getX(),
                self.getY() - 0.1,
                self.getZ()
        );

        BlockState state = self.level().getBlockState(below);

        if (!(state.getBlock() instanceof TechnetiumBoardBlock)) return;

        Level level = self.level();

        double x = movement.x;
        double z = movement.z;

        double step = 0.05;

        while (x != 0.0 && level.noCollision(self, self.getBoundingBox().move(x, -1.0, 0))) {
            if (Math.abs(x) < step) x = 0.0;
            else x -= Math.signum(x) * step;
        }

        while (z != 0.0 && level.noCollision(self, self.getBoundingBox().move(0, -1.0, z))) {
            if (Math.abs(z) < step) z = 0.0;
            else z -= Math.signum(z) * step;
        }

        while (x != 0.0 && z != 0.0 &&
                level.noCollision(self, self.getBoundingBox().move(x, -1.0, z))) {

            if (Math.abs(x) < step) x = 0.0;
            else x -= Math.signum(x) * step;

            if (Math.abs(z) < step) z = 0.0;
            else z -= Math.signum(z) * step;
        }

        cir.setReturnValue(new Vec3(x, movement.y, z));
    }
}
