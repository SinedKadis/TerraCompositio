package net.sinedkadis.terracompositio.cfe;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.sinedkadis.terracompositio.api.networks.cfe.CFECapability;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetworkMember;
import net.sinedkadis.terracompositio.api.networks.cfe.ICFEHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class CFEItemWrapper implements ICFEHandler, ICapabilityProvider {
    private final LazyOptional<ICFEHandler> holder = LazyOptional.of(() -> this);
    @NotNull
    protected ItemStack container;

    public CFEItemWrapper(@NotNull ItemStack container) {
        this.container = container;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return CFECapability.CFE.orEmpty(cap, holder);
    }

    @Override
    public int getCFE() {
        CompoundTag tag = container.getOrCreateTag();
        return tag.getInt("CFE");
    }

    @Override
    public ICFEHandler setIndex(int index) {
        CompoundTag tag = container.getOrCreateTag();
        tag.putInt("INDEX",index);
        return this;
    }

    @Override
    public ICFEHandler setOffset(Function<Vec3, Vec3> offset) {
        return this;
    }

    @Override
    public int takeCFE(int cfe, boolean simulate) {
        int toTake = Math.min(cfe,this.getCFE());
        if (!simulate) {
            this.setCFE(this.getCFE()-toTake);
        }
        return toTake;
    }

    @Override
    public int addCFE(int cfe, ICFEHandler source, boolean simulate, boolean doRender) {
        int toAdd = Math.min(this.getFreeSpace(),cfe);
        if (!simulate) {
            this.setCFE(this.getCFE()+toAdd);
        }
        return toAdd;
    }

    @Override
    public int addCFE(int cfe, BlockPos sourcePos, boolean simulate) {
        int toAdd = Math.min(this.getFreeSpace(),cfe);
        if (!simulate) {
            this.setCFE(this.getCFE()+toAdd);
        }
        return toAdd;
    }

    @Override
    public void setCFE(int cfe) {
        CompoundTag tag = container.getOrCreateTag();
        tag.putInt("CFE",cfe);
    }

    @Override
    public int getMaxCFE() {
        CompoundTag tag = container.getOrCreateTag();
        return tag.getInt("MAX_CFE");
    }

    @Override
    public ICFEHandler setCfeTravelSpeed(float cfeTravelSpeed) {
        return this;
    }

    @Override
    public ICFEHandler setMaxCFE(int max) {
        CompoundTag tag = container.getOrCreateTag();
        if (!tag.contains("MAX_CFE"))
            tag.putInt("MAX_CFE",max);
        return this;
    }

    @Override
    public void containerTick() {

    }

    @Override
    public void writeToNBT(CompoundTag pTag) {

    }

    @Override
    public void readFromNBT(CompoundTag pTag) {

    }

    @Override
    public int getFreeSpace() {
        return this.getMaxCFE()-this.getCFE();
    }

    @Override
    public int getQueued() {
        return 0;
    }

    @Override
    public BlockPos getBlockPos() {
        return null;
    }

    @Override
    public float getCfeTravelSpeed() {
        return 0;
    }

    @Override
    public CFENetworkMember getAttachedMember() {
        return null;
    }

    @Override
    public Function<Vec3, Vec3> getOffset() {
        return t->t;
    }

    @Override
    public int getIndex() {
        CompoundTag tag = container.getOrCreateTag();
        return tag.getInt("INDEX");
    }
}
