package net.sinedkadis.terracompositio.block.entity;


import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
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
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetworkMemberBE;
import net.sinedkadis.terracompositio.registries.TCBlockStateProperties;
import net.sinedkadis.terracompositio.registries.TCFluids;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
@Getter
public abstract class AbstractDesorberBlockEntity extends TCCFEBlockEntity implements CFENetworkMemberBE {

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
    public int getLimit() {
        return 5;
    }


    protected int getTankCapacity() {
        return 250;
    }
    protected LazyOptional<IFluidHandler> lazyFluidHandler = LazyOptional.empty();

    public AbstractDesorberBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state,BlockMode.SOURCE);
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
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
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
    protected void saveAdditional(@NotNull CompoundTag pTag) {
        fluidHandler.writeToNBT(pTag);
        super.saveAdditional(pTag);
    }

    @Override
    public void load(@NotNull CompoundTag pTag) {
        super.load(pTag);
        fluidHandler.readFromNBT(pTag);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyFluidHandler.invalidate();
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }
}
