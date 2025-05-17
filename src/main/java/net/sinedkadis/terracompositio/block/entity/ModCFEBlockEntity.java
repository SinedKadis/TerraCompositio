package net.sinedkadis.terracompositio.block.entity;

import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.sinedkadis.terracompositio.api.TerraCompositioAPI;
import net.sinedkadis.terracompositio.api.networks.NetworkAction;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetworkMemberBE;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetwork;
import net.sinedkadis.terracompositio.api.networks.cfe.ICFEHandler;
import net.sinedkadis.terracompositio.cfe.CFECapability;
import net.sinedkadis.terracompositio.cfe.CFEContainer;
import net.sinedkadis.terracompositio.util.TCUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
public abstract class ModCFEBlockEntity extends ModBlockEntity implements CFENetworkMemberBE{
    protected final int connectRange;
    protected final CFEContainer cfeContainer = new CFEContainer(this);
    protected LazyOptional<ICFEHandler> lazyCFEOptional = LazyOptional.empty();

    public ModCFEBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state,int minCFE,int maxCFE,int connectRange) {
        super(type, pos, state);
        this.connectRange = connectRange;
        cfeContainer.setMinCFE(minCFE);
        cfeContainer.setMaxCFE(maxCFE);
    }
    public ModCFEBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state,int maxCFE,int connectRange) {
        super(type, pos, state);
        this.connectRange = connectRange;
        cfeContainer.setMinCFE(0);
        cfeContainer.setMaxCFE(maxCFE);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyCFEOptional = LazyOptional.of(() -> cfeContainer);
        this.onCFENetworkMemberUpdate();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == CFECapability.CFE){
            return lazyCFEOptional.cast();
        }
        return super.getCapability(cap, side);
    }

    public void tick(Level pLevel, BlockPos pPos, BlockState pState) {
        CFENetwork cfeNetworkInstance = TerraCompositioAPI.INSTANCE.getCFENetworkInstance();
        if (!pLevel.isClientSide && connectRange != 0) {
            boolean inNetwork = cfeNetworkInstance.isIn(pLevel, this);
            if (!inNetwork && !this.isRemoved()) {
                cfeNetworkInstance.fireCFENetworkEvent(this, NetworkAction.ADD);
            }
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        TerraCompositioAPI.INSTANCE.getCFENetworkInstance().fireCFENetworkEvent(this, NetworkAction.REMOVE);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyCFEOptional.invalidate();
    }

    public ModCFEBlockEntity(BlockEntityType<?> type, BlockPos pPos, BlockState pBlockState) {
        this(type,pPos,pBlockState,0,0,0);
    }

    @Override
    public void onCFENetworkMemberUpdate() {
        if (level != null && !level.isClientSide){
            CFENetwork cfeNetwork = TerraCompositioAPI.instance().getCFENetworkInstance();
            CFENetworkMemberBE source = cfeNetwork.getClosestSourceWithCFE(getBlockPos(), getLevel(), connectRange * 2,getPriority());
            if (source != null){
                int toTransfer = (int) Mth.clamp(
                        Math.round(
                                50 - Mth.square(
                                        Math.abs(
                                                Math.sqrt(
                                                    TCUtil.distSqr(this.getBlockPos(), source.getBlockPos())
                                                ) - connectRange
                                        )
                                )
                        ),0,50);
                int transferred = TCUtil.tryCFETransfer(this,source,toTransfer);
                if (transferred > 0)
                    TCUtil.sendCFEParticles((ServerLevel) level,worldPosition,source.getBlockPos(),transferred);
            }
        }
    }

    @Override
    public int getPriority() {
        return Integer.MAX_VALUE;
    }

    @Override
    public int getLimit() {
        return connectRange*2;
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag pTag) {
        cfeContainer.writeToNBT(pTag);
        super.saveAdditional(pTag);
    }

    @Override
    public void load(@NotNull CompoundTag pTag) {
        super.load(pTag);
        cfeContainer.readFromNBT(pTag);
    }

}
