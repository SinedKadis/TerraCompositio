package net.sinedkadis.terracompositio.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.sinedkadis.terracompositio.api.TerraCompositioAPI;
import net.sinedkadis.terracompositio.api.cfe.CFENetworkAction;
import net.sinedkadis.terracompositio.api.cfe.CFESource;
import net.sinedkadis.terracompositio.registries.ModFluids;
import net.sinedkadis.terracompositio.util.CFENetworkHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractDesorberBlockEntity extends ModBlockEntity implements CFESource {
    private int cfe = 0;
    public static final int MAX_CFE = 100;
    public static final int MIN_CFE = -100;
    protected final FluidTank fluidHandler = new FluidTank(250){
        private final FluidStack flow = new FluidStack(ModFluids.FLOW_FLUID.source.get(), 1000);
        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (resource.isFluidEqual(flow))
                return super.fill(resource, action);
            return 0;
        }
    };
    protected LazyOptional<IFluidHandler> lazyFluidHandler = LazyOptional.empty();

    public AbstractDesorberBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public void tick(Level pLevel, BlockPos pPos, BlockState pState) {
        if (!pLevel.isClientSide) {
            boolean inNetwork = CFENetworkHandler.instance.isIn(pLevel, this);
            if (!inNetwork && !this.isRemoved()) {
                TerraCompositioAPI.INSTANCE.getCFENetworkInstance().fireCFENetworkEvent(this, CFENetworkAction.ADD);
            }
        }
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER)
            return lazyFluidHandler.cast();
        return super.getCapability(cap, side);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyFluidHandler = LazyOptional.of(()-> fluidHandler);
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag pTag) {
        fluidHandler.writeToNBT(pTag);
        pTag.putInt("cfe",cfe);
        super.saveAdditional(pTag);
    }

    @Override
    public void load(@NotNull CompoundTag pTag) {
        super.load(pTag);
        cfe = pTag.getInt("cfe");
        fluidHandler.readFromNBT(pTag);
    }

    @Override
    public Level getCFESourceLevel() {
        return getLevel();
    }

    @Override
    public BlockPos getCFESourceBlockPos() {
        return getBlockPos();
    }

    @Override
    public int getCurrentCFE() {
        return this.cfe;
    }

    @Override
    public int takeCFE(int cfe) {
        int CFE = getCurrentCFE();
        int deltaCFE = CFE - cfe;
        if (Mth.clamp(deltaCFE,MIN_CFE,MAX_CFE) == deltaCFE){
            this.cfe = deltaCFE;
            return cfe;
        } else {
            if (cfe < 0){
                this.cfe = MAX_CFE;
                return cfe + (deltaCFE - MAX_CFE);
            } else {
                this.cfe = MIN_CFE;
                return cfe + (deltaCFE - MIN_CFE);
            }
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        TerraCompositioAPI.INSTANCE.getCFENetworkInstance().fireCFENetworkEvent(this, CFENetworkAction.REMOVE);
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
