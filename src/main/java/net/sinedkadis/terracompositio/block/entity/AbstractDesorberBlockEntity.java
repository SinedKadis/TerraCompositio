package net.sinedkadis.terracompositio.block.entity;


import lombok.Getter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.sinedkadis.terracompositio.api.behaviors.blockentity.IBEBehaviour;
import net.sinedkadis.terracompositio.api.helpers.TooltipHelper;
import net.sinedkadis.terracompositio.api.networks.cfe.IECFHandler;
import net.sinedkadis.terracompositio.block.behaviours.ECFHandlerBehaviour;
import net.sinedkadis.terracompositio.config.TCInnerConfig;
import net.sinedkadis.terracompositio.registries.TCBlockStateProperties;
import net.sinedkadis.terracompositio.registries.TCFluids;
import net.sinedkadis.terracompositio.util.FluidComponent;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Optional;

@Getter
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class AbstractDesorberBlockEntity extends TCBlockEntity {

    protected final FluidTank fluidHandler = new FluidTank(getTankCapacity()){
        private final FluidStack flow = new FluidStack(TCFluids.FLOW_FLUID.source.get(), 1000);
        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (resource.isFluidEqual(flow))
                return super.fill(resource, action);
            return 0;
        }
    };

    @Override
    public void addBEBehaviours(List<IBEBehaviour> list) {
        list.add(new ECFHandlerBehaviour(this)
                .maxCFE(1000)
                .range(5)
                .priority(TCInnerConfig.DEFAULT_SOURCE_PRIORITY));
    }

    //todo comparator compatibility


    protected int getTankCapacity() {
        return 250;
    }
    protected LazyOptional<IFluidHandler> lazyFluidHandler = LazyOptional.empty();

    public AbstractDesorberBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public void tick(Level pLevel, BlockPos pPos, BlockState pState) {
        super.tick(pLevel,pPos,pState);
        if (!fluidHandler.isEmpty() && !pState.getValue(TCBlockStateProperties.INFUSED)) {
            pLevel.setBlockAndUpdate(pPos, pState.setValue(TCBlockStateProperties.INFUSED, true));
        } else if (fluidHandler.isEmpty() && pState.getValue(TCBlockStateProperties.INFUSED)) {
            pLevel.setBlockAndUpdate(pPos, pState.setValue(TCBlockStateProperties.INFUSED, false));
        }
        BlockEntity blockEntity = pLevel.getBlockEntity(pPos.below());
        Optional<IFluidHandler> fluidHandlerOptional = Optional.empty();
        if (blockEntity != null) {
            fluidHandlerOptional = blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER).resolve();
        }
        if (fluidHandlerOptional.isPresent() && fluidHandlerOptional.get() instanceof FluidTank sourceTank){
            FluidUtil.tryFluidTransfer(this.fluidHandler,sourceTank,1000,true);
        }
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER && (side == null || side == Direction.DOWN)) {
            return lazyFluidHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyFluidHandler = LazyOptional.of(() -> fluidHandler);
    }

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        fluidHandler.writeToNBT(pTag);
        super.saveAdditional(pTag);
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        fluidHandler.readFromNBT(pTag);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyFluidHandler.invalidate();
    }

    protected IECFHandler cfeContainer() {
        return ((ECFHandlerBehaviour) behaviours.get(0)).getMainHandler();
    }

    @Override
    public void collectKnowledgeData(CompoundTag data) {
        super.collectKnowledgeData(data);

        FluidStack fluidInTank = fluidHandler.getFluidInTank(0);
        CompoundTag compoundTag = new CompoundTag();
        fluidInTank.writeToNBT(compoundTag);
        data.put(TooltipHelper.Keys.FLUID.toData(), compoundTag);

    }

    @Override
    public void addTooltipLines(CompoundTag data, List<Component> tooltip, boolean isShifting) {
        super.addTooltipLines(data, tooltip, isShifting);

        FluidStack fluidStack = FluidStack.loadFluidStackFromNBT(data.getCompound(TooltipHelper.Keys.FLUID.toData()));
        if (!fluidStack.isEmpty()) {
            TooltipHelper.addHeader(TooltipHelper.Headers.FLUIDS, tooltip);

            tooltip.add(FluidComponent.of(fluidStack));
        }

    }
}
