package net.sinedkadis.terracompositio.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.sinedkadis.terracompositio.registries.TCBlockStateProperties;
import net.sinedkadis.terracompositio.registries.TCFluids;


public interface IFluidApplicable {

    default FluidApplyResult tryApply(Level level, BlockPos blockPos, ItemStack itemStack, IFluidHandlerItem handlerItem, Player player) {
        FluidStack resource = new FluidStack(TCFluids.FLOW_FLUID.source.get(), defaultConsumeAmount());
        FluidStack result = handlerItem.drain(resource, IFluidHandler.FluidAction.SIMULATE);
        BlockState blockState = level.getBlockState(blockPos);
        if (result.getAmount() >= defaultConsumeAmount()
                && blockState.hasProperty(TCBlockStateProperties.INFUSED)
                && !blockState.getValue(TCBlockStateProperties.INFUSED)) {
            level.setBlockAndUpdate(blockPos, blockState.setValue(TCBlockStateProperties.INFUSED, true));
            if (!player.isCreative())
                handlerItem.drain(defaultConsumeAmount(), IFluidHandler.FluidAction.EXECUTE);
            return FluidApplyResult.SUCCESS;
        }
        return FluidApplyResult.SKIP;
    }

    default int defaultConsumeAmount() {
        return 1000;
    }

    enum FluidApplyResult {
        SUCCESS,SKIP,CANCEL;

//        public boolean skip() {
//            return this.equals(SKIP);
//        }
        public boolean success() {
            return this.equals(SUCCESS);
        }
        public boolean cancel() {
            return this.equals(CANCEL);
        }
    }
}
