package net.sinedkadis.terracompositio.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.sinedkadis.terracompositio.item.custom.TechnetiumArmorItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
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

    @Unique
    private static int terraCompositio$jumpTimer = 0;

    public LocalPlayerMixin(ClientLevel pClientLevel, GameProfile pGameProfile) {
        super(pClientLevel, pGameProfile);
    }

    @Unique
    private static boolean terraCompositio$wasDown = false;
    @Shadow
    public Input input;

    @Inject(
            method = "sendPosition()V",
            at = @At("HEAD")
    )
    private void tc$onTick(CallbackInfo ci) {
        double x = getX();
        double z = getZ();
        double y = getY();
        if (Math.floor(xLast) != Math.floor(x) || Math.floor(zLast) != Math.floor(z) || Math.floor(yLast1) != Math.floor(y)) {
            TechnetiumArmorItem.onBlockChanged(this);
        }
        if (terraCompositio$jumpTimer > 0) {
            terraCompositio$jumpTimer--;
        }

        Minecraft mc = Minecraft.getInstance();
        KeyMapping keyJump = mc.options.keyJump;
        boolean down = keyJump.isDown();
        if (down && !terraCompositio$wasDown) {
            if (terraCompositio$jumpTimer > 0) {
                TechnetiumArmorItem.onDoubleJump((LocalPlayer) (Object) this);
                terraCompositio$jumpTimer = 0;
            } else {
                terraCompositio$jumpTimer = 7;
            }
        }
        terraCompositio$wasDown = down;
    }
}
