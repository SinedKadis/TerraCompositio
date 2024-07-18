package net.sinedkadis.terracompositio.block.entity;

import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.SoundAction;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.cfe.CFEContainer;
import net.sinedkadis.terracompositio.fluid.ModFluids;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.sinedkadis.terracompositio.TerraCompositio.GLOGGER;

public class FlowExtractorBlockEntity extends BlockEntity implements CFEContainer {
    @Getter
    private final FluidTank fluidTank = new FluidTank(1000) {
        @Override
        protected void onContentsChanged() {
            super.onContentsChanged();
            FlowExtractorBlockEntity.this.sendUpdate();
        }
    };
    @Getter
    private final LazyOptional<FluidTank> fluidOptional = LazyOptional.of(() -> this.fluidTank);
    private int CFE;
    public float prevScale;



    public FlowExtractorBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.FLOW_EXTRACTOR_BE.get(),pPos, pBlockState);
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
        pTag.put("FluidTank", this.fluidTank.writeToNBT(new CompoundTag()));
        pTag.putInt("CFE",this.CFE);
        pTag.putFloat("prevScale",this.prevScale);
    }

    @Override
    public void load(@NotNull CompoundTag pTag) {
        super.load(pTag);
        this.fluidTank.readFromNBT(pTag.getCompound("FluidTank"));
        this.CFE = pTag.getInt("CFE");
        this.prevScale = pTag.getInt("prevScale");
    }

    public void tick(Level pLevel, BlockPos pPos, BlockState pState) {
        if(this.fluidTank.getFluidAmount() >= this.fluidTank.getCapacity())
            return;
        if (this.fluidTank.getFluid().getFluid() == ModFluids.FLOW_FLUID.source.get().getSource()){
            this.fluidTank.drain(1, IFluidHandler.FluidAction.EXECUTE);
            this.CFE++;
        }
        float scale = fluidTank.getCapacity() == 0 ? 0 : (float) fluidTank.getFluidAmount() /fluidTank.getCapacity();
        //GLOGGER.debug(scale);
        if (scale != prevScale) {
            prevScale = scale;
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

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
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

    private void sendUpdate() {
        setChanged();

        if (this.level != null)
            this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }

    public FluidStack getFluidStack() {
        return new FluidStack(
                this.fluidTank.getFluid().getFluid(),
                this.fluidTank.getFluidAmount()
        );
    }
}
