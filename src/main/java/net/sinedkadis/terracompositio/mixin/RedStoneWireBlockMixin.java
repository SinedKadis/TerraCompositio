package net.sinedkadis.terracompositio.mixin;

import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.minecraft.world.level.block.RedStoneWireBlock.POWER;

@Mixin(RedStoneWireBlock.class)
public abstract class RedStoneWireBlockMixin {

    @Inject(
            method = "getWireSignal(Lnet/minecraft/world/level/block/state/BlockState;)I",
            at = @At("HEAD"),
            cancellable = true)
    private void onGetWireSignal(BlockState pState, CallbackInfoReturnable<Integer> cir) {
        if (pState.getBlock() instanceof RedStoneWireBlock){
            cir.setReturnValue(pState.getValue(POWER));
            cir.cancel();
        }
    }
}

