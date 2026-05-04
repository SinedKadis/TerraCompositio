package net.sinedkadis.terracompositio.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.sinedkadis.terracompositio.api.dummies.DummyCFEHandler;
import net.sinedkadis.terracompositio.api.TCCapabilities;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetworkMemberEntity;
import net.sinedkadis.terracompositio.api.networks.cfe.ICFEHandler;
import net.sinedkadis.terracompositio.config.TCInnerConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

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
}
