package net.sinedkadis.terracompositio.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.sinedkadis.terracompositio.cfe.CFEContainer;
import net.sinedkadis.terracompositio.fluid.ModFluids;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FlowExtractorBlockEntity extends BlockEntity implements CFEContainer {
    private final FluidTank fluidTank = new FluidTank(5000) {
        @Override
        protected void onContentsChanged() {
            super.onContentsChanged();
            FlowExtractorBlockEntity.this.sendUpdate();
        }
    };

    private final LazyOptional<FluidTank> fluidOptional = LazyOptional.of(() -> this.fluidTank);
    private int CFE;


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
    }

    @Override
    public void load(@NotNull CompoundTag pTag) {
        super.load(pTag);
        this.fluidTank.readFromNBT(pTag.getCompound("FluidTank"));
        this.CFE = pTag.getInt("CFE");
    }

    public void tick(Level pLevel, BlockPos pPos, BlockState pState) {
        if(this.fluidTank.getFluidAmount() >= this.fluidTank.getCapacity())
            return;
        if (this.fluidTank.getFluid().getFluid() == ModFluids.FLOW_FLUID.source.get().getSource()){
            
            this.fluidTank.drain(1, IFluidHandler.FluidAction.EXECUTE);
            this.CFE++;
        }
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

    public LazyOptional<FluidTank> getFluidOptional() {
        return this.fluidOptional;
    }

    public FluidTank getFluidTank() {
        return this.fluidTank;
    }
}
