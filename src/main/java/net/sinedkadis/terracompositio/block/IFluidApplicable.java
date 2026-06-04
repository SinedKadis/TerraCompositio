package net.sinedkadis.terracompositio.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;


public interface IFluidApplicable {

    //    default FluidApplyResult tryApply(Level level, BlockPos blockPos, ItemStack fluidStack) {
//        Optional<IFluidHandlerItem> capability = fluidStack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).resolve();
//        if (capability.isPresent()) {
//            IFluidHandlerItem iFluidHandlerItem = capability.get();
//            return tryApply(level,blockPos,fluidStack,iFluidHandlerItem);
//        }
//        return FluidApplyResult.SKIP;
//    }
    FluidApplyResult tryApply(Level level, BlockPos blockPos, ItemStack itemStack, IFluidHandlerItem handlerItem);

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
