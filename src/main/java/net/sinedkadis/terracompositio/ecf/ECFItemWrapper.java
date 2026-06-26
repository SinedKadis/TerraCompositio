package net.sinedkadis.terracompositio.ecf;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.sinedkadis.terracompositio.api.networks.ecf.ECFNetworkMember;
import net.sinedkadis.terracompositio.api.networks.ecf.IECFHandler;
import net.sinedkadis.terracompositio.api.registries.TCCapabilities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

@SuppressWarnings("DataFlowIssue")
@MethodsReturnNonnullByDefault
public class ECFItemWrapper implements IECFHandler, ICapabilityProvider {
    @Getter
    @Setter
    protected int queued = 0;
    private final LazyOptional<IECFHandler> holder = LazyOptional.of(() -> this);
    @NotNull
    @Getter
    protected ItemStack container;

    public ECFItemWrapper(@NotNull ItemStack container) {
        this.container = container;
    }

    @Override
    public String toString() {
        return "CFEItemWrapper{" +
                "container=" + container +
                ",\n CFE=" + getECF() +
                ",\n Max CFE=" + getMaxECF() +
                ",\n queued=" + queued +
                '}';
    }

    @Override
    public void clear() {
        setECF(0);
        queued = 0;
    }

    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return TCCapabilities.ECF.orEmpty(cap, holder);
    }

    @Override
    public int getECF() {
        CompoundTag tag = container.getOrCreateTag();
        return tag.getInt("CFE");
    }

    @Override
    public IECFHandler setIndex(int index) {
        CompoundTag tag = container.getOrCreateTag();
        tag.putInt("INDEX",index);
        return this;
    }

    @Override
    public IECFHandler setOffset(Function<Vec3, Vec3> offset) {
        return this;
    }

    @Override
    public void setECF(int ecf) {
        CompoundTag tag = container.getOrCreateTag();
        tag.putInt("CFE", ecf);
    }

    @Override
    public int takeECF(int cfe, boolean simulate) {
        int cfe1 = this.getECF();
        int toTake = Math.min(cfe, cfe1);
        if (!simulate) {
            this.setECF(cfe1 - toTake);
        }
        return toTake;
    }

    @Override
    public int sendECF(ECFNetworkMember target, int cfe, float speed) {
        return 0;
    }

    @Override
    public int addECF(int cfe, boolean simulate) {
        int toAdd = Math.min(this.getFreeSpace(),cfe);
        if (!simulate) {
            int cfe1 = this.getECF();
            this.setECF(cfe1 + toAdd);
        }
        return toAdd;
    }

    @Override
    public int getMaxECF() {
        CompoundTag tag = container.getOrCreateTag();
        int maxCfe = tag.getInt("MAX_CFE");
        return maxCfe == 0 ? 8 : maxCfe;
    }

    @Override
    public IECFHandler setMaxECF(int max) {
        CompoundTag tag = container.getOrCreateTag();
        tag.putInt("MAX_CFE", max);
        tag.putInt("CFE", Mth.clamp(tag.getInt("CFE"), 0, max));
        return this;
    }

    @Override
    public void writeToNBT(CompoundTag pTag) {

    }

    @Override
    public void readFromNBT(CompoundTag pTag) {

    }

    @Override
    public int getECFWithQueue() {
        return getECF() + getQueued();
    }

    @Override
    public boolean isEmpty() {
        return getECFWithQueue() <= 0;
    }

    @Override
    public int getFreeSpace() {
        return this.getMaxECF() - this.getECFWithQueue();
    }

    @Override
    public double x() {
        return 0;
    }

    @Override
    public double y() {
        return 0;
    }

    @Override
    public double z() {
        return 0;
    }

    @Override
    public BlockPos getPos() {
        return null;
    }

    @Override
    public BlockState getBlockState() {
        return null;
    }

    @Override
    public ServerLevel getLevel() {
        return null;
    }

    @Override
    public ECFNetworkMember getAttachedMember() {
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
