package net.sinedkadis.terracompositio.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RedStoneWireBlock.class)
public abstract class RedStoneWireBlockMixin {

    @Unique
    private static boolean terraCompositio$sharedShouldSignal = true;

    @Inject(method = "updatePowerStrength", at = @At("HEAD"))
    private void before(Level pLevel, BlockPos pPos, BlockState pState, CallbackInfo ci){
        terraCompositio$sharedShouldSignal = false;
    }

    @Inject(method = "updatePowerStrength", at = @At("RETURN"))
    private void after(Level pLevel, BlockPos pPos, BlockState pState, CallbackInfo ci){
        terraCompositio$sharedShouldSignal = true;
    }

    @Inject(method = "getSignal", at = @At("HEAD"), cancellable = true)
    private void useShared(CallbackInfoReturnable<Integer> cir) {
        if (!terraCompositio$sharedShouldSignal) {
            cir.setReturnValue(0);
        }
    }
    @Inject(method = "isSignalSource", at = @At("HEAD"), cancellable = true)
    private void useShared2(CallbackInfoReturnable<Boolean> cir) {
        if (!terraCompositio$sharedShouldSignal) {
            cir.setReturnValue(false);
        }
    }
}

