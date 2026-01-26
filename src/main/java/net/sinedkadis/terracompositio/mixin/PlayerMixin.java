package net.sinedkadis.terracompositio.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.sinedkadis.terracompositio.api.dummies.DummyCFEHandler;
import net.sinedkadis.terracompositio.api.TCCapabilities;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetworkMemberEntity;
import net.sinedkadis.terracompositio.api.networks.cfe.ICFEHandler;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Player.class)
public class PlayerMixin implements CFENetworkMemberEntity {
    @Override
    public Level getLevel() {
        return ((Player)(Object)this).level();
    }

    @Override
    public BlockPos getPos() {
        return ((Player)(Object)this).blockPosition();
    }

    @Override
    public int getLimit() {
        return 10;
    }

    @Override
    public int getPriority() {
        return 100;
    }

    @Override
    public ICFEHandler getMainHandler() {
        return ((Player)(Object)this).getCapability(TCCapabilities.CFE).orElse(DummyCFEHandler.instance);
    }
}
