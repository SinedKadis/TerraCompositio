package net.sinedkadis.terracompositio.fluid;

import com.mojang.datafixers.util.Pair;
import lombok.Getter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.wrapper.EmptyHandler;
import net.sinedkadis.terracompositio.block.entity.ModBlockEntity;
import net.sinedkadis.terracompositio.util.LerpedFloat;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Function;

public class ModFluidTank {
    public ModBlockEntity blockEntity;
    protected int syncCooldown;
    protected boolean queuedSync;
    protected Runnable fluidUpdateCallback;
    @Getter
    protected LazyOptional<? extends IFluidHandler> capability;
    protected boolean insertionAllowed;
    protected boolean extractionAllowed;
    private static final int SYNC_RATE = 8;
    @Getter
    protected TankSegment[] tanks;
    @Getter
    protected IFluidHandler[] handlers;

    public ModFluidTank(ModBlockEntity blockEntity,int tanks,int capacity,boolean enforceVariety) {
        this.blockEntity = blockEntity;

        insertionAllowed = true;
        extractionAllowed = true;
        this.tanks = new TankSegment[tanks];
        handlers = new IFluidHandler[tanks];
        for (int i = 0; i < tanks; i++) {
            TankSegment tankSegment = new TankSegment(capacity);
            this.tanks[i] = tankSegment;
            handlers[i] = tankSegment.tank;
        }
        capability = LazyOptional.of(() -> new InternalFluidHandler(handlers, enforceVariety));
        fluidUpdateCallback = () -> {
        };
    }
    public ModFluidTank whenFluidUpdates(Runnable fluidUpdateCallback) {
        this.fluidUpdateCallback = fluidUpdateCallback;
        return this;
    }
    public ModFluidTank allowInsertion() {
        insertionAllowed = true;
        return this;
    }

    public ModFluidTank allowExtraction() {
        extractionAllowed = true;
        return this;
    }
    public ModFluidTank forbidInsertion() {
        insertionAllowed = false;
        return this;
    }

    public ModFluidTank forbidExtraction() {
        extractionAllowed = false;
        return this;
    }

    public void forEach(Consumer<TankSegment> action) {
        for (TankSegment tankSegment : tanks)
            action.accept(tankSegment);
    }
    public FluidTank getPrimaryHandler() {
        return getPrimaryTank().tank;
    }

    public TankSegment getPrimaryTank() {
        return tanks[0];
    }

    public boolean isEmpty() {
        for (TankSegment tankSegment : tanks)
            if (!tankSegment.tank.isEmpty())
                return false;
        return true;
    }

    public void write(CompoundTag nbt, boolean clientPacket) {
        ListTag tanksNBT = new ListTag();
        forEach(ts -> tanksNBT.add(ts.writeNBT()));
        nbt.put("Tanks", tanksNBT);
    }


    public void read(CompoundTag nbt, boolean clientPacket) {
        MutableInt index = new MutableInt(0);
        iterateCompoundList(nbt.getList("Tanks", Tag.TAG_COMPOUND), c -> {
            if (index.intValue() >= tanks.length)
                return;
            tanks[index.intValue()].readNBT(c, clientPacket);
            index.increment();
        });
    }

    public class InternalFluidHandler extends CombinedTankWrapper {

        public InternalFluidHandler(IFluidHandler[] handlers, boolean enforceVariety) {
            super(handlers);
            if (enforceVariety)
                enforceVariety();
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (!insertionAllowed)
                return 0;
            return super.fill(resource, action);
        }

        public int forceFill(FluidStack resource, FluidAction action) {
            return super.fill(resource, action);
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            if (!extractionAllowed)
                return FluidStack.EMPTY;
            return super.drain(resource, action);
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            if (!extractionAllowed)
                return FluidStack.EMPTY;
            return super.drain(maxDrain, action);
        }

    }
    public static <T> ListTag writeCompoundList(Iterable<T> list, Function<T, CompoundTag> serializer) {
        ListTag listNBT = new ListTag();
        list.forEach(t -> {
            CompoundTag apply = serializer.apply(t);
            if (apply == null)
                return;
            listNBT.add(apply);
        });
        return listNBT;
    }
    public static <T> void iterateCompoundList(ListTag listNBT, Consumer<CompoundTag> consumer) {
        listNBT.forEach(inbt -> consumer.accept((CompoundTag) inbt));
    }

    @Getter
    public class TankSegment {

        protected SmartFluidTank tank;
        protected LerpedFloat fluidLevel;
        protected FluidStack renderedFluid;

        public TankSegment(int capacity) {
            tank = new SmartFluidTank(capacity, this::onFluidStackChanged);
            fluidLevel = LerpedFloat.linear()
                    .startWithValue(0)
                    .chase(0, .25, LerpedFloat.Chaser.EXP);
            renderedFluid = FluidStack.EMPTY;
        }

        public void onFluidStackChanged(FluidStack stack) {
            if (!blockEntity.hasLevel())
                return;
            fluidLevel.chase(tank.getFluidAmount() / (float) tank.getCapacity(), .25, LerpedFloat.Chaser.EXP);
            if (!blockEntity.getLevel().isClientSide)
                updateFluids();
            if (blockEntity.isVirtual() && !tank.getFluid().isEmpty())
                renderedFluid = tank.getFluid();
            else
                renderedFluid = FluidStack.EMPTY;
        }
        protected void updateFluids() {
            fluidUpdateCallback.run();
        }

        public float getTotalUnits(float partialTicks) {
            return fluidLevel.getValue(partialTicks) * tank.getCapacity();
        }

        public CompoundTag writeNBT() {
            CompoundTag compound = new CompoundTag();
            compound.put("TankContent", tank.writeToNBT(new CompoundTag()));
            compound.put("Level", fluidLevel.writeNBT());
            return compound;
        }

        public void readNBT(CompoundTag compound, boolean clientPacket) {
            tank.readFromNBT(compound.getCompound("TankContent"));
            fluidLevel.readNBT(compound.getCompound("Level"), clientPacket);
            if (!tank.getFluid()
                    .isEmpty())
                renderedFluid = tank.getFluid();
        }

        public boolean isEmpty(float partialTicks) {
            FluidStack renderedFluid = getRenderedFluid();
            if (renderedFluid.isEmpty())
                return true;
            float units = getTotalUnits(partialTicks);
            return units < 1;
        }

    }
    public class SmartFluidTank extends FluidTank {

        private final Consumer<FluidStack> updateCallback;

        public SmartFluidTank(int capacity, Consumer<FluidStack> updateCallback) {
            super(capacity);
            this.updateCallback = updateCallback;
        }

        @Override
        protected void onContentsChanged() {
            super.onContentsChanged();
            updateCallback.accept(getFluid());
        }

        @Override
        public void setFluid(FluidStack stack) {
            super.setFluid(stack);
            updateCallback.accept(stack);
        }

    }

}
