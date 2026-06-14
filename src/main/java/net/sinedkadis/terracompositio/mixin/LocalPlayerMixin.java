package net.sinedkadis.terracompositio.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.sinedkadis.terracompositio.item.custom.TechnetiumArmorItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin extends AbstractClientPlayer {
    @Shadow
    private double xLast;
    @Shadow
    private double zLast;
    @Shadow
    private double yLast1;

    public LocalPlayerMixin(ClientLevel pClientLevel, GameProfile pGameProfile) {
        super(pClientLevel, pGameProfile);
    }

    @Inject(
            method = "sendPosition()V",
            at = @At("HEAD")
    )
    private void tc$onTick(CallbackInfo ci) {
        double x = getX();
        double z = getZ();
        if (Math.floor(xLast) != Math.floor(x) || Math.floor(zLast) != Math.floor(z)) {
            TechnetiumArmorItem.onBlockChanged(this, BlockPos.containing(xLast, yLast1, zLast));
        }
    }

}
