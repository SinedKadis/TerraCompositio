package net.sinedkadis.terracompositio.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.sinedkadis.terracompositio.api.TerraCompositioAPI;
import net.sinedkadis.terracompositio.api.networks.NetworkAction;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetwork;
import net.sinedkadis.terracompositio.cfe.CFECapability;
import net.sinedkadis.terracompositio.cfe.CFEContainer;
import net.sinedkadis.terracompositio.cfe.LimitlessCFEContainer;
import net.sinedkadis.terracompositio.registries.TCBlockEntities;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetworkMemberBE;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CreativeCFESourceBlockEntity extends TCBlockEntity implements CFENetworkMemberBE{

    private LazyOptional<CFEContainer> lazyOptional;

    public CreativeCFESourceBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(TCBlockEntities.CREATIVE_CFE_SOURCE_BE.get(),pPos, pBlockState);
    }

    public void tick(Level pLevel, BlockPos pPos, BlockState pState) {
        CFENetwork cfeNetworkInstance = TerraCompositioAPI.INSTANCE.getCFENetworkInstance();
        if (!pLevel.isClientSide) {
            boolean inNetwork = cfeNetworkInstance.isIn(pLevel, this);
            if (!inNetwork && !this.isRemoved()) {
                cfeNetworkInstance.fireCFENetworkEvent(this, NetworkAction.ADD);
            }
        }
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == CFECapability.CFE) {
            return lazyOptional.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyOptional = LazyOptional.of(() -> {
            CFEContainer cfeContainer = new LimitlessCFEContainer(this);
            cfeContainer.setCFE(Integer.MAX_VALUE);
            return cfeContainer;
        });
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        TerraCompositioAPI.INSTANCE.getCFENetworkInstance().fireCFENetworkEvent(this, NetworkAction.REMOVE);
    }

    @Override
    public int getLimit() {
        return 10;
    }

    @Override
    public int getPriority() {
        return Integer.MIN_VALUE;
    }

}
