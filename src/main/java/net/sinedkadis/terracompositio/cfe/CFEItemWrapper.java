package net.sinedkadis.terracompositio.cfe;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.sinedkadis.terracompositio.api.TCCapabilities;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetworkMember;
import net.sinedkadis.terracompositio.api.networks.cfe.ICFEHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

@SuppressWarnings("DataFlowIssue")
@MethodsReturnNonnullByDefault
public class CFEItemWrapper implements ICFEHandler, ICapabilityProvider {
    @Getter
    @Setter
    protected int queued = 0;
    private final LazyOptional<ICFEHandler> holder = LazyOptional.of(() -> this);
    @NotNull
    protected ItemStack container;

    public CFEItemWrapper(@NotNull ItemStack container) {
        this.container = container;
    }

    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return TCCapabilities.CFE.orEmpty(cap, holder);
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
        int cfe1 = this.getCFE();
        int toTake = Math.min(cfe, cfe1);
        if (!simulate) {
            this.setCFE(cfe1 -toTake);
        }
        return toTake;
    }

    @Override
    public int addCFE(int cfe, boolean simulate) {
        int toAdd = Math.min(this.getFreeSpace(),cfe);
        if (!simulate) {
            int cfe1 = this.getCFE();
            this.setCFE(cfe1 +toAdd);
        }
        return toAdd;
    }


    @Override
    public int sendCFE(int cfe, ICFEHandler target, boolean simulate) {
        return 0;
    }

    @Override
    public int sendCFE(int cfe, BlockPos target, boolean simulate) {
        return 0;
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
    public void writeToNBT(CompoundTag pTag) {

    }

    @Override
    public void readFromNBT(CompoundTag pTag) {

    }

    @Override
    public int getCFEWithQueue() {
        return getCFE()+getQueued();
    }

    @Override
    public boolean isEmpty() {
        return getCFEWithQueue() <= 0;
    }

    @Override
    public int getFreeSpace() {
        return this.getMaxCFE()-this.getCFEWithQueue();
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
    public <T extends BlockEntity> T getEntity() {
        return null;
    }

    @Override
    public ServerLevel getLevel() {
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
