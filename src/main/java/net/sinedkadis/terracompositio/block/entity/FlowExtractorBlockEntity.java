package net.sinedkadis.terracompositio.block.entity;

import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.sinedkadis.terracompositio.cfe.CFEContainer;
import net.sinedkadis.terracompositio.fluid.CombinedTankWrapper;
import net.sinedkadis.terracompositio.fluid.ModFluidTank;
import net.sinedkadis.terracompositio.fluid.ModFluids;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static net.sinedkadis.terracompositio.TerraCompositio.GLOGGER;
import static net.sinedkadis.terracompositio.fluid.ModFluidTank.iterateCompoundList;
import static net.sinedkadis.terracompositio.fluid.ModFluidTank.writeCompoundList;

public class FlowExtractorBlockEntity extends ModBlockEntity implements CFEContainer {
    List<FluidStack> visualizedOutputFluids;

    @Getter
    private final ModFluidTank inputFluidTank = new ModFluidTank(this,1,1000,true)
            .whenFluidUpdates(this::sendUpdate);
    @Getter
    private final ModFluidTank outputFluidTank = new ModFluidTank(this,1,1000,true)
            .whenFluidUpdates(this::sendUpdate);
    @Getter
    private final LazyOptional<IFluidHandler> fluidOptional = LazyOptional.of(() -> {
        LazyOptional<? extends IFluidHandler> inputCap = inputFluidTank.getCapability();
        LazyOptional<? extends IFluidHandler> outputCap = outputFluidTank.getCapability();
        return new CombinedTankWrapper(outputCap.orElse(null), inputCap.orElse(null));}
    );


    private int CFE;



    public FlowExtractorBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.FLOW_EXTRACTOR_BE.get(),pPos, pBlockState);
        visualizedOutputFluids = Collections.synchronizedList(new ArrayList<>());
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == ForgeCapabilities.FLUID_HANDLER)
            return this.fluidOptional.cast();

        return super.getCapability(cap);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        this.fluidOptional.invalidate();
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag pTag) {
        super.saveAdditional(pTag);
        pTag.put("VisualizedFluids", writeCompoundList(visualizedOutputFluids,
                ia -> ia.writeToNBT(new CompoundTag())));
        visualizedOutputFluids.clear();
        pTag.putInt("CFE",this.CFE);
    }

    @Override
    public void load(@NotNull CompoundTag pTag) {
        super.load(pTag);
        iterateCompoundList(pTag.getList("VisualizedFluids", Tag.TAG_COMPOUND),
                c -> visualizedOutputFluids
                        .add(FluidStack.loadFluidStackFromNBT(c)));
        this.CFE = pTag.getInt("CFE");
    }

    public void tick(Level pLevel, BlockPos pPos, BlockState pState) {
        if (this.inputFluidTank.getPrimaryHandler().getFluid().getFluid() == ModFluids.FLOW_FLUID.source.get().getSource()
                && (this.outputFluidTank.getPrimaryHandler().getFluid().getFluid() == Fluids.WATER
                    || this.outputFluidTank.getPrimaryHandler().getFluid().getFluid() == Fluids.EMPTY)){
            this.inputFluidTank.getPrimaryHandler().drain(1, IFluidHandler.FluidAction.EXECUTE);
            this.CFE++;
            this.outputFluidTank.getPrimaryHandler().fill(new FluidStack(Fluids.WATER,1), IFluidHandler.FluidAction.EXECUTE);
        }

    }
    public static float getScale(float prevScale, IFluidTank tank) {
        return getScale(prevScale, tank.getFluidAmount(), tank.getCapacity(), tank.getFluid().isEmpty());
    }
    public static float getScale(float prevScale, int stored, int capacity, boolean empty) {
        return getScale(prevScale, capacity == 0 ? 0 : stored / (float) capacity, empty, stored == capacity);
    }
    public static float getScale(float prevScale, float targetScale, boolean empty, boolean full) {
        float difference = Math.abs(prevScale - targetScale);
        if (difference > 0.01) {
            //GLOGGER.debug("RENDER: "+"difference > 0.01");
            return (9 * prevScale + targetScale) / 10;
        } else if (!empty && full && difference > 0) {
            GLOGGER.debug("RENDER: "+"!empty && full && difference > 0");
            //If we are full but our difference is less than 0.01, but we want to get our scale all the way up to the target
            // instead of leaving it at a value just under. Note: We also check that we are not empty as we technically may
            // be both empty and full if the current capacity is zero
            return targetScale;
        } else if (!empty && prevScale == 0) {
            GLOGGER.debug("RENDER: "+"!empty && prevScale == 0");
            //If we have any contents make sure we end up rendering it
            return targetScale;
        }
        if (empty && prevScale < 0.01) {
            //GLOGGER.debug("RENDER: "+"empty && prevScale < 0.01");
            //If we are empty and have a very small amount just round it down to empty
            return 0;
        }
        return prevScale;
    }



    @Override
    public @NotNull CompoundTag getUpdateTag() {
        CompoundTag nbt = super.getUpdateTag();
        saveAdditional(nbt);
        return nbt;
    }

    @Override
    public int getCFE() {
        return CFE;
    }

    @Override
    public void setCFE(int count) {
        CFE = count;
    }

    public boolean isEmpty() {
        return inputFluidTank.isEmpty() && outputFluidTank.isEmpty();
    }


}
