package net.sinedkadis.terracompositio.mixin;

import mekanism.api.annotations.ParametersAreNotNullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.CapabilityProvider;
import net.minecraftforge.common.extensions.IForgeLevel;
import net.sinedkadis.terracompositio.block.custom.FlowCedarCasingBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

import static net.sinedkadis.terracompositio.block.custom.FlowCedarCasingBlock.PARTS;

@Mixin(Level.class)
public abstract class MixinLevel extends CapabilityProvider<Level> implements LevelAccessor, AutoCloseable, IForgeLevel {

    @MethodsReturnNonnullByDefault
    @ParametersAreNotNullByDefault
    @Shadow public abstract BlockState getBlockState(BlockPos pPos);

    @ParametersAreNotNullByDefault
    @Shadow @Nullable public abstract BlockEntity getBlockEntity(BlockPos pPos);

    protected MixinLevel(Class<Level> baseClass) {
        super(baseClass);
    }

    @Inject(method = "getBlockEntity",at = @At(value = "RETURN",ordinal = 1),cancellable = true)
    protected void onGetBlockEntity(BlockPos pPos, CallbackInfoReturnable<BlockEntity> cir){
        BlockState state = this.getBlockState(pPos);
        if (state.getBlock().getClass() == FlowCedarCasingBlock.class){
            if (FlowCedarCasingBlock.hasInputBus(state) && !state.getValue(PARTS).equals(Direction.UP)) {
                cir.setReturnValue(this.getBlockEntity(pPos.relative(state.getValue(PARTS))));
            }
        }
    }

}
