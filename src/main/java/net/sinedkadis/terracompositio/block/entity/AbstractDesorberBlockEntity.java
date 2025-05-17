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
import net.sinedkadis.terracompositio.api.networks.cfe.ICFEHandler;
import net.sinedkadis.terracompositio.api.TerraCompositioAPI;
import net.sinedkadis.terracompositio.api.networks.NetworkAction;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetworkMemberBE;
import net.sinedkadis.terracompositio.cfe.CFECapability;
import net.sinedkadis.terracompositio.cfe.CFEContainer;
import net.sinedkadis.terracompositio.registries.ModBlockStateProperties;
import net.sinedkadis.terracompositio.registries.ModFluids;
import net.sinedkadis.terracompositio.cfe.CFENetworkHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
@Getter
public abstract class AbstractDesorberBlockEntity extends ModBlockEntity implements CFENetworkMemberBE {

    protected final FluidTank fluidHandler = new FluidTank(getTankCapacity()){
        private final FluidStack flow = new FluidStack(ModFluids.FLOW_FLUID.source.get(), 1000);
        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (resource.isFluidEqual(flow))
                return super.fill(resource, action);
            return 0;
        }
    };
    protected final CFEContainer cfeContainer = new CFEContainer(this,-100,100);

    @Override
    public int getLimit() {
        return 7;
    }

    @Override
    public int getPriority() {
        return Integer.MIN_VALUE;
    }

    protected int getTankCapacity() {
        return 250;
    }
    protected LazyOptional<IFluidHandler> lazyFluidHandler = LazyOptional.empty();
    protected LazyOptional<ICFEHandler> lazyCFEHandler = LazyOptional.empty();

    public AbstractDesorberBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public void tick(Level pLevel, BlockPos pPos, BlockState pState) {
        if (!pLevel.isClientSide) {
            boolean inNetwork = CFENetworkHandler.instance.isIn(pLevel, this);
            if (!inNetwork && !this.isRemoved() ) {
                TerraCompositioAPI.INSTANCE.getCFENetworkInstance().fireCFENetworkEvent(this, NetworkAction.ADD);
            }
        }
        if (!fluidHandler.isEmpty() && !pState.getValue(ModBlockStateProperties.INFUSED)) {
            pLevel.setBlockAndUpdate(pPos, pState.setValue(ModBlockStateProperties.INFUSED, true));
        } else if (fluidHandler.isEmpty() && pState.getValue(ModBlockStateProperties.INFUSED)) {
            pLevel.setBlockAndUpdate(pPos, pState.setValue(ModBlockStateProperties.INFUSED, false));
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
        if (cap == CFECapability.CFE) {
            return lazyCFEHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyFluidHandler = LazyOptional.of(() -> fluidHandler);
        lazyCFEHandler = LazyOptional.of(() -> cfeContainer);
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag pTag) {
        fluidHandler.writeToNBT(pTag);
        cfeContainer.writeToNBT(pTag);
        super.saveAdditional(pTag);
    }

    @Override
    public void load(@NotNull CompoundTag pTag) {
        super.load(pTag);
        cfeContainer.readFromNBT(pTag);
        fluidHandler.readFromNBT(pTag);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        TerraCompositioAPI.INSTANCE.getCFENetworkInstance().fireCFENetworkEvent(this, NetworkAction.REMOVE);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyCFEHandler.invalidate();
        lazyCFEHandler.invalidate();
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
