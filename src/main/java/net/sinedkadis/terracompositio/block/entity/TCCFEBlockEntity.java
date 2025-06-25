package net.sinedkadis.terracompositio.block.entity;

import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.sinedkadis.terracompositio.api.TerraCompositioAPI;
import net.sinedkadis.terracompositio.api.networks.NetworkAction;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetworkMember;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetworkMemberBE;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetwork;
import net.sinedkadis.terracompositio.api.networks.cfe.ICFEHandler;
import net.sinedkadis.terracompositio.cfe.CFECapability;
import net.sinedkadis.terracompositio.cfe.CFEContainer;
import net.sinedkadis.terracompositio.util.TCUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
public abstract class TCCFEBlockEntity extends TCBlockEntity implements CFENetworkMemberBE{
    protected final int connectRange;
    protected final CFEContainer cfeContainer = new CFEContainer(this);
    protected LazyOptional<ICFEHandler> lazyCFEOptional = LazyOptional.empty();
    protected BlockMode blockMode;

    public TCCFEBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int maxCFE, int connectRange, BlockMode blockMode) {
        super(type, pos, state);
        this.blockMode = blockMode;
        this.connectRange = connectRange;
        cfeContainer.setMaxCFE(maxCFE);

    }
    public TCCFEBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, BlockMode blockMode) {
        super(type, pos, state);
        if (blockMode.source()) {
            this.blockMode = blockMode;
            this.connectRange = 10;
            cfeContainer.setMaxCFE(100);
        } else {
            this.blockMode = blockMode;
            this.connectRange = 0;
            cfeContainer.setMaxCFE(0);
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyCFEOptional = LazyOptional.of(() -> cfeContainer);
        this.onCFENetworkMemberUpdate(level,worldPosition);
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
        cfeContainer.containerTick();
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

    @Override
    public void onCFENetworkMemberUpdate(Level level, BlockPos pos) {
        if (level != null && !level.isClientSide){
            if (blockMode.consumer() || blockMode.container()){
                CFENetwork cfeNetwork = TerraCompositioAPI.instance().getCFENetworkInstance();
                CFENetworkMember source = cfeNetwork.getClosestSourceWithCFE(pos, level, connectRange * 2, getPriority());
                if (source != null) {
                    int transferred = TCUtil.tryCFETransfer(this, source, Integer.MAX_VALUE);
                    if (transferred > 0)
                        TCUtil.sendCFEParticles((ServerLevel) level,
                                Vec3.atLowerCornerWithOffset(pos,
                                        this.particleTargetOffset().x,
                                        this.particleTargetOffset().y,
                                        this.particleTargetOffset().z),
                                source.getBlockPos(),
                                transferred);
                }
            }
        }
    }

    @Override
    public int getPriority() {
        return switch (blockMode){
            case SOURCE -> Integer.MIN_VALUE;
            case CONSUMER -> Integer.MAX_VALUE;
            case CONTAINER -> 0;
        };
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

    public enum BlockMode {
        SOURCE,CONSUMER,CONTAINER;
        public boolean source(){
            return this == SOURCE;
        }
        public boolean consumer(){
            return this == CONSUMER;
        }
        public boolean container(){
            return this == CONTAINER;
        }


    }

}
