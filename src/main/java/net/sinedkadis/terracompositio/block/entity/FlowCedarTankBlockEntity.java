package net.sinedkadis.terracompositio.block.entity;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.sinedkadis.terracompositio.api.TerraCompositioAPI;
import net.sinedkadis.terracompositio.api.components.FluidComponent;
import net.sinedkadis.terracompositio.api.helpers.ECFHelper;
import net.sinedkadis.terracompositio.api.helpers.TooltipHelper;
import net.sinedkadis.terracompositio.api.networks.NetworkAction;
import net.sinedkadis.terracompositio.api.networks.fluid.FluidNetwork;
import net.sinedkadis.terracompositio.api.networks.fluid.FluidNetworkMemberBE;
import net.sinedkadis.terracompositio.config.TCCommonConfigs;
import net.sinedkadis.terracompositio.fluid.TCFluidTank;
import net.sinedkadis.terracompositio.registries.TCBlockEntities;
import net.sinedkadis.terracompositio.registries.TCBlocks;
import net.sinedkadis.terracompositio.registries.TCFluids;
import net.sinedkadis.terracompositio.registries.TCTags;
import net.sinedkadis.terracompositio.util.behaviors.blockentity.IBEBehaviour;
import net.sinedkadis.terracompositio.util.helpers.ParticleHelperInternal;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static net.sinedkadis.terracompositio.api.registries.TCBlockStateProperties.INFUSED;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FlowCedarTankBlockEntity extends TCBlockEntity implements FluidNetworkMemberBE {
    protected final TCFluidTank fluidHandler = new TCFluidTank(8000, this);
    protected LazyOptional<IFluidHandler> lazyFluidHandler = LazyOptional.empty();
    private int tickCounter = 20;
    protected boolean scheduledUpdate = false;
    protected int scheduledMembersUpdate = -1;
    protected Set<FluidNetworkMemberBE> scheduledMembers = new HashSet<>();
    private int previousPriority = 0;

    public FlowCedarTankBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.FLOW_CEDAR_TANK_BE.get(), pos, state);
    }

    public boolean onPedestal(Level level, BlockPos pos) {
        return level.getBlockState(pos.below()).is(TCBlocks.FLOW_CEDAR_PEDESTAL.get());
    }

    @Override
    public void addBEBehaviours(List<IBEBehaviour> list) {

    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
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
    protected void saveAdditional(CompoundTag pTag) {
        fluidHandler.writeToNBT(pTag);
        super.saveAdditional(pTag);
    }

    @Override
    public void load(CompoundTag pTag) {
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
        int priority = getPriority();
        if (priority > 0 && previousPriority != priority) {
            TerraCompositioAPI.instance().getFluidNetworkInstance().fireFluidNetworkEvent(this,NetworkAction.UPDATE);
        }
        previousPriority = priority;

        tickCounter--;
        if (tickCounter <= 0 && onPedestal(level, pos) && priority > 0) {
            tickCounter = 20;
            FluidStack fluidStack = new FluidStack(TCFluids.FLOW_FLUID.source.get(), 1000);
            if (fluidHandler.fill(fluidStack, IFluidHandler.FluidAction.SIMULATE)>=1000){
                List<BlockPos> list = new ArrayList<>(
                        BlockPos.betweenClosedStream(pos.offset(-getRange(), -getRange(), -getRange()), pos.offset(getRange(), getRange(), getRange()))
                        .filter(pos1 -> level.getBlockState(pos1).is(TCTags.Blocks.FLOW_CEDAR_LOGS))
                        .filter(pos2 -> level.getBlockState(pos2).getValue(INFUSED))
                        .toList());
                if (!list.isEmpty()) {
                    int index = level.getRandom().nextInt(list.size());
                    BlockPos blockPos = list.get(index);
                    level.setBlockAndUpdate(blockPos, level.getBlockState(blockPos).setValue(INFUSED, false));
                    fluidHandler.fill(fluidStack, IFluidHandler.FluidAction.EXECUTE);
                    ParticleHelperInternal.sendFluidParticles((ServerLevel) level, pos, blockPos, 100, fluidStack);
                }
            }
        }
        updateIfScheduled();
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        TerraCompositioAPI.INSTANCE.getFluidNetworkInstance().fireFluidNetworkEvent(this, NetworkAction.REMOVE);
    }


    @Override
    public IFluidHandler getMainHandler() {
        return fluidHandler;
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
    public int getRange() {
        return 10;
    }

    @Override
    public void updateIfScheduled() {
        if (scheduledUpdate) {
            this.scheduledUpdate = false;
            this.onFluidNetworkMemberUpdate();
        }
        if (scheduledMembersUpdate == 0) {
            scheduledMembersUpdate = -1;
            Set<FluidNetworkMemberBE> scheduledMembers1 = Set.copyOf(this.scheduledMembers);
            this.scheduledMembers.clear();
            scheduledMembers1.forEach(this::onFluidNetworkMemberUpdate);
        } else if (scheduledMembersUpdate > 0)
            scheduledMembersUpdate--;

    }

    @Override
    public void scheduleMemberUpdate() {
        this.scheduledUpdate = true;
    }

    @Override
    public void scheduleMemberUpdate(FluidNetworkMemberBE updated) {
        this.scheduledMembers.add(updated);
        if (scheduledMembersUpdate < 0) scheduledMembersUpdate = TCCommonConfigs.TICKS_BETWEEN_BURSTS.get();
    }

    @Override
    public void onFluidNetworkMemberUpdate() {
        if (getMainHandler().getFluidInTank(0).getAmount() > 0) {
            FluidNetwork fluidNetwork = TerraCompositioAPI.instance().getFluidNetworkInstance();
            Set<FluidNetworkMemberBE> targets = fluidNetwork.getAvailableNetworkTargets(this);
            targets.forEach(target -> {
                if (target.getPriority() <= 0) return;
                IFluidHandler mainHandler = target.getMainHandler();
                FluidStack fluidInTank = mainHandler.getFluidInTank(0);
                if (mainHandler.getTankCapacity(0) - fluidInTank.getAmount() > 0)
                    scheduleMemberUpdate(target);

                FluidStack transferred = FluidUtil.tryFluidTransfer(mainHandler, fluidHandler, 1000, true);
                int amount = transferred.getAmount();
                if (amount > 0) {
                    ParticleHelperInternal
                            .sendFluidParticles((ServerLevel) level, target.getPos(), this.getBlockPos(), amount / 10, transferred);
                }
            });
        }
    }

    @Override
    public void onFluidNetworkMemberUpdate(FluidNetworkMemberBE updated) {
        if (updated.getPriority() > this.getPriority() && getMainHandler().getFluidInTank(0).getAmount() > 0 && ECFHelper.validMember(updated)) {
            IFluidHandler mainHandler = updated.getMainHandler();
            if (mainHandler.getTankCapacity(0) - mainHandler.getFluidInTank(0).getAmount() > 0) {
                scheduleMemberUpdate(updated);
            }
            FluidStack transferred = FluidUtil.tryFluidTransfer(mainHandler, fluidHandler, 1000, true);
            int amount = transferred.getAmount();
            if (amount > 0) {
                ParticleHelperInternal
                        .sendFluidParticles((ServerLevel) level, updated.getPos(), this.getBlockPos(), amount / 10, transferred);
            }
        } else onFluidNetworkMemberUpdate();
    }

    @Override
    public void collectKnowledgeData(CompoundTag data) {
        FluidStack fluidInTank = fluidHandler.getFluidInTank(0);
        CompoundTag compoundTag = new CompoundTag();
        fluidInTank.writeToNBT(compoundTag);
        data.put(TooltipHelper.Keys.FLUID.toData(), compoundTag);
        data.putInt(TooltipHelper.Keys.RANGE.toData(), getRange());

        if (TCCommonConfigs.DEBUG.get()) {
            data.putInt(TooltipHelper.Keys.PRIORITY.toData(), getPriority());
        }

        super.collectKnowledgeData(data);
    }

    @Override
    public void addTooltipLines(CompoundTag data, List<Component> tooltip, boolean isShifting) {
        TooltipHelper.addWithHeader(TooltipHelper.Headers.BLOCK, tooltip, t -> {
            if (isShifting)
                TooltipHelper.addIfExist(TooltipHelper.Keys.RANGE, t, data);

            TooltipHelper.addIfExist(TooltipHelper.Keys.PRIORITY, t, data);
        });

        FluidStack fluidStack = FluidStack.loadFluidStackFromNBT(data.getCompound(TooltipHelper.Keys.FLUID.toData()));
        if (!fluidStack.isEmpty()) {
            TooltipHelper.addWithHeader(TooltipHelper.Headers.FLUIDS, tooltip,
                    t -> t.add(FluidComponent.of(fluidStack)));
        }
        super.addTooltipLines(data, tooltip, isShifting);
    }
}
