package net.sinedkadis.terracompositio.item.custom;

import net.minecraft.core.Direction;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class ModBucketItem extends BucketItem implements IFluidHandlerItem, ICapabilityProvider {
    Supplier<? extends Fluid> supplier;
    private final LazyOptional<IFluidHandlerItem> holder = LazyOptional.of(() -> this);

    public ModBucketItem(Supplier<? extends Fluid> supplier, Properties builder) {
        super(supplier, builder);
        this.supplier = supplier;
    }

    @Override
    public @NotNull ItemStack getContainer() {
        return new ItemStack(this);
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int i) {
        return new FluidStack(supplier.get(),1000);
    }

    @Override
    public int getTankCapacity(int i) {
        return 1000;
    }

    @Override
    public boolean isFluidValid(int i, @NotNull FluidStack fluidStack) {
        return true;
    }

    @Override
    public int fill(FluidStack fluidStack, FluidAction fluidAction) {
        return 0;
    }

    @Override
    public @NotNull FluidStack drain(FluidStack fluidStack, FluidAction fluidAction) {
        if (fluidStack.getFluid().isSame(supplier.get())){
            return drain(1000,fluidAction);
        }
        return FluidStack.EMPTY;
    }

    @Override
    public @NotNull FluidStack drain(int i, FluidAction fluidAction) {
        if (i >=1000){
            return new FluidStack(supplier.get(),1000);
        }
        return FluidStack.EMPTY;
    }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction facing) {
        if(capability == ForgeCapabilities.FLUID_HANDLER)
            return this.holder.cast();

        return ICapabilityProvider.super.getCapability(capability);
    }
}
