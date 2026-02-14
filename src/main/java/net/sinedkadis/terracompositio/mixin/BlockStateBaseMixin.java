package net.sinedkadis.terracompositio.mixin;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockStateBaseMixin {

    @Inject(
            method = "is(Lnet/minecraft/world/level/block/Block;)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private void injectCustomWire(Block block, CallbackInfoReturnable<Boolean> cir) {

        @SuppressWarnings("DataFlowIssue")
        BlockState state = (BlockState)(Object)this;

        if (block instanceof RedStoneWireBlock &&
                state.getBlock() instanceof RedStoneWireBlock) {

            cir.setReturnValue(true);
        }
    }
}

