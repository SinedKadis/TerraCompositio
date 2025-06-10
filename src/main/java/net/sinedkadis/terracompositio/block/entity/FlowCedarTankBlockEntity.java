package net.sinedkadis.terracompositio.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.sinedkadis.terracompositio.api.networks.fluid.FluidNetwork;
import net.sinedkadis.terracompositio.api.networks.fluid.FluidNetworkMemberBE;
import net.sinedkadis.terracompositio.api.TerraCompositioAPI;
import net.sinedkadis.terracompositio.api.networks.NetworkAction;
import net.sinedkadis.terracompositio.fluid.TCFluidTank;
import net.sinedkadis.terracompositio.registries.TCBlockEntities;
import net.sinedkadis.terracompositio.registries.TCBlocks;
import net.sinedkadis.terracompositio.registries.TCFluids;
import net.sinedkadis.terracompositio.registries.TCTags;
import net.sinedkadis.terracompositio.util.TCUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static net.sinedkadis.terracompositio.registries.TCBlockStateProperties.INFUSED;

public class FlowCedarTankBlockEntity extends TCBlockEntity implements FluidNetworkMemberBE {
    protected final TCFluidTank fluidHandler = new TCFluidTank(8000, this);
    protected LazyOptional<IFluidHandler> lazyFluidHandler = LazyOptional.empty();
    private int tickCounter = 20;
    private boolean wasActivated = false;
    public FlowCedarTankBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.FLOW_CEDAR_TANK_BE.get(), pos, state);
    }

    public boolean onPedestal(Level level, BlockPos pos) {
        return level != null && level.getBlockState(pos.below()).is(TCBlocks.FLOW_CEDAR_PEDESTAL.get());
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
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

    public void tick(Level level, BlockPos pos, BlockState state) {
        FluidNetwork fluidNetworkInstance = TerraCompositioAPI.INSTANCE.getFluidNetworkInstance();
        if (!level.isClientSide) {
            boolean inNetwork = fluidNetworkInstance.isIn(level, this.fluidHandler);
            if (!inNetwork && !this.isRemoved()) {
                fluidNetworkInstance.fireFluidNetworkEvent(this, NetworkAction.ADD);
            }
        }
        if (getPriority()>0 && !wasActivated){
            TerraCompositioAPI.instance().getFluidNetworkInstance().fireFluidNetworkEvent(this,NetworkAction.UPDATE);
            wasActivated = true;
        }
        if (getPriority() == 0){
            wasActivated = false;
        }
        tickCounter--;
        if (tickCounter <= 0 && onPedestal(level, pos) && getPriority() > 0){
            tickCounter = 20;
            FluidStack fluidStack = new FluidStack(TCFluids.FLOW_FLUID.source.get(), 1000);
            if (fluidHandler.fill(fluidStack, IFluidHandler.FluidAction.SIMULATE)>=1000){
                List<BlockPos> list = new java.util.ArrayList<>(TCUtil.getNearBlocks(pos, 10).stream()
                        .filter(pos1 -> level.getBlockState(pos1).is(TCTags.Blocks.FLOW_CEDAR_LOGS))
                        .filter(pos2 -> level.getBlockState(pos2).getValue(INFUSED))
                        .toList());
                if (!list.isEmpty()) {
                    Collections.shuffle(list);
                    BlockPos blockPos = list.get(0);
                    level.setBlockAndUpdate(blockPos, level.getBlockState(blockPos).setValue(INFUSED, false));
                    fluidHandler.fill(fluidStack, IFluidHandler.FluidAction.EXECUTE);
                    TCUtil.sendFluidParticles((ServerLevel) level, pos, blockPos, 100, fluidStack);
                }
            }
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        TerraCompositioAPI.INSTANCE.getFluidNetworkInstance().fireFluidNetworkEvent(this, NetworkAction.REMOVE);
    }


    @Override
    public int getPriority() {
        if (level != null && onPedestal(level, worldPosition)) {
            int redStone = 0;
            if (level.hasNeighborSignal(worldPosition)) {
                redStone = level.getBestNeighborSignal(worldPosition);
            }
            if (level.hasNeighborSignal(worldPosition.below())) {
                redStone = Math.max(redStone, level.getBestNeighborSignal(worldPosition.below()));
            }
            FlowCedarTankBlockEntity blockEntity = (FlowCedarTankBlockEntity) level.getBlockEntity(worldPosition);
            if (blockEntity != null) {
                return redStone;
            }
        }
        return 0;
    }

    @Override
    public int getLimit() {
        return 10;
    }

    @Override
    public void onFluidNetworkMemberUpdate() {
        FluidNetwork fluidNetworkInstance = TerraCompositioAPI.INSTANCE.getFluidNetworkInstance();
        BlockPos pos = this.getBlockPos();
        if (getPriority() > 0) {
            FluidNetworkMemberBE source;
            if (fluidHandler.isEmpty()){
                source = fluidNetworkInstance.getRandomFluidHandlerInRange(pos,level,fluidHandler.getFluid().getFluid(),10,getPriority());
            } else {
                source = fluidNetworkInstance
                        .getClosestFluidHandlerWithMatchingContent(pos, level, fluidHandler.getFluid().getFluid(), 10, getPriority());
            }
            if (source != null) {
                Optional<IFluidHandler> fluidHandlerOptional = source.getBE().getCapability(ForgeCapabilities.FLUID_HANDLER).resolve();
                if (fluidHandlerOptional.isPresent() && fluidHandlerOptional.get() instanceof FluidTank sourceTank) {
                    FluidStack transferred = FluidUtil.tryFluidTransfer(fluidHandler, sourceTank, 1000, true);
                    int amount = transferred.getAmount();
                    if (amount > 0){
                        TCUtil.sendFluidParticles((ServerLevel) level,pos,source.getBlockPos(), amount /10,transferred);
                    }
                }
            }
        }
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
