package net.sinedkadis.terracompositio.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RedstoneSide;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RedStoneWireBlock.class)
public abstract class RedStoneWireBlockMixin {

    @Shadow
    protected abstract int getWireSignal(BlockState pState);

    @Shadow
    protected abstract boolean canSurviveOn(BlockGetter pLevel, BlockPos pPos, BlockState pState);

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
    private void onGetSignal(CallbackInfoReturnable<Integer> cir) {
        if (!terraCompositio$sharedShouldSignal) {
            cir.setReturnValue(0);
        }
    }
    @Inject(method = "isSignalSource", at = @At("HEAD"), cancellable = true)
    private void onIsSignalSource(CallbackInfoReturnable<Boolean> cir) {
        if (!terraCompositio$sharedShouldSignal) {
            cir.setReturnValue(false);
        }
    }
    @Inject(
            method = "getConnectingSide(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;Z)Lnet/minecraft/world/level/block/state/properties/RedstoneSide;",
            at = @At("HEAD"),
            cancellable = true)
    private void onGetConnectingSide(BlockGetter pLevel,
                                     BlockPos pPos,
                                     Direction pDirection,
                                     boolean pNonNormalCubeAbove,
                                     CallbackInfoReturnable<RedstoneSide> cir) {
        BlockPos blockposRel = pPos.relative(pDirection);
        BlockState blockstateRel = pLevel.getBlockState(blockposRel);

        if (pNonNormalCubeAbove ) {

            if (!(pLevel.getBlockState(pPos.above()).getBlock() instanceof RedStoneWireBlock)) {
                boolean flag = blockstateRel.getBlock() instanceof TrapDoorBlock || this.canSurviveOn(pLevel, blockposRel, blockstateRel);
                if (flag && pLevel.getBlockState(blockposRel.above()).canRedstoneConnectTo(pLevel, blockposRel.above(), null)) {
                    if (blockstateRel.isFaceSturdy(pLevel, blockposRel, pDirection.getOpposite())) {
                        cir.setReturnValue(RedstoneSide.UP);
                        return;
                    }
                    cir.setReturnValue(RedstoneSide.SIDE);
                    return;
                }
            }

        }
        if (blockstateRel.canRedstoneConnectTo(pLevel, blockposRel, pDirection)) {
            cir.setReturnValue(RedstoneSide.SIDE);
            return;
        }
        if (blockstateRel.isRedstoneConductor(pLevel, blockposRel)) {
            cir.setReturnValue(RedstoneSide.NONE);
            return;
        }
        if (!(pLevel.getBlockState(pPos.below()).getBlock() instanceof RedStoneWireBlock)){
            BlockPos blockPosRelBelow = blockposRel.below();
            cir.setReturnValue(pLevel.getBlockState(blockPosRelBelow).canRedstoneConnectTo(pLevel, blockPosRelBelow, null)
                    ? RedstoneSide.SIDE : RedstoneSide.NONE);
        } else {
            cir.setReturnValue(RedstoneSide.NONE);
        }

    }

    @Inject(
            method = "calculateTargetStrength",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onCalculateTargetStrength(Level pLevel, BlockPos pPos, CallbackInfoReturnable<Integer> cir) {
        terraCompositio$sharedShouldSignal = false;
        int i = pLevel.getBestNeighborSignal(pPos);
        terraCompositio$sharedShouldSignal = true;



        int j = 0;
        if (i < 15) {
            for(Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos blockposRel = pPos.relative(direction);
                BlockState blockstateRel = pLevel.getBlockState(blockposRel);
                j = Math.max(j, this.getWireSignal(blockstateRel));

                BlockPos blockposAbove = pPos.above();
                BlockPos blockposBelow = pPos.below();

                BlockState blockStateAbove = pLevel.getBlockState(blockposAbove);
                BlockState blockStateBelow = pLevel.getBlockState(blockposBelow);

                boolean wireAbove = blockStateAbove.getBlock() instanceof RedStoneWireBlock;
                boolean wireBelow = blockStateBelow.getBlock() instanceof RedStoneWireBlock;
                boolean wireRel = blockstateRel.getBlock() instanceof RedStoneWireBlock;

                if (blockstateRel.isRedstoneConductor(pLevel, blockposRel)
                        && !blockStateAbove.isRedstoneConductor(pLevel, blockposAbove)
                        && !wireAbove) {
                    j = Math.max(j, this.getWireSignal(pLevel.getBlockState(blockposRel.above())));
                } else if (!blockstateRel.isRedstoneConductor(pLevel, blockposRel) && !wireBelow && !wireRel) {
                    j = Math.max(j, this.getWireSignal(pLevel.getBlockState(blockposRel.below())));
                }

            }
        }

        cir.setReturnValue(Math.max(i, j - 1));
    }

}

