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
import net.sinedkadis.terracompositio.api.FluidNetwork;
import net.sinedkadis.terracompositio.api.FluidSource;
import net.sinedkadis.terracompositio.api.TerraCompositioAPI;
import net.sinedkadis.terracompositio.api.cfe.NetworkAction;
import net.sinedkadis.terracompositio.fluid.ModFluidTank;
import net.sinedkadis.terracompositio.registries.ModBlockEntities;
import net.sinedkadis.terracompositio.registries.ModBlocks;
import net.sinedkadis.terracompositio.registries.ModFluids;
import net.sinedkadis.terracompositio.registries.ModTags;
import net.sinedkadis.terracompositio.util.TCUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

import static net.sinedkadis.terracompositio.registries.ModBlockStateProperties.INFUSED;

public class FlowCedarTankBlockEntity extends ModBlockEntity implements FluidSource {
    protected final ModFluidTank fluidHandler = new ModFluidTank(8000, this);
    protected LazyOptional<IFluidHandler> lazyFluidHandler = LazyOptional.empty();

    public FlowCedarTankBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FLOW_CEDAR_TANK_BE.get(), pos, state);
    }

    public boolean onPedestal(Level level, BlockPos pos) {
        return level != null && level.getBlockState(pos.below()).is(ModBlocks.FLOW_CEDAR_PEDESTAL.get());
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
        if (getPriority() > 0) {
            FluidSource source;
            if (fluidHandler.isEmpty()){
                source = fluidNetworkInstance.getRandomFluidHandlerInRange(pos,level,fluidHandler.getFluid().getFluid(),10,getPriority());
            } else {
                source = fluidNetworkInstance
                        .getClosestFluidHandlerWithMatchingContent(pos, level, fluidHandler.getFluid().getFluid(), 10, getPriority());
            }
            if (source != null) {
                if (source.getFluidHandler() instanceof FluidTank sourceTank) {
                    FluidStack transferred = FluidUtil.tryFluidTransfer(fluidHandler, sourceTank, 1000, true);
                    int amount = transferred.getAmount();
                    if (amount > 0){
                        TCUtil.sendFluidParticles((ServerLevel) level,pos,source.getBlockPos(), amount /10,transferred);
                    }
                }
            } else {
                FluidStack fluidStack = new FluidStack(ModFluids.FLOW_FLUID.source.get(), 1000);
                if (fluidHandler.fill(fluidStack, IFluidHandler.FluidAction.SIMULATE)>=1000){
                    List<BlockPos> list = new java.util.ArrayList<>(TCUtil.getNearBlocks(pos, 10).stream()
                            .filter(pos1 -> level.getBlockState(pos1).is(ModTags.Blocks.FLOW_CEDAR_LOGS))
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
    public IFluidHandler getFluidHandler() {
        return this.fluidHandler;
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
