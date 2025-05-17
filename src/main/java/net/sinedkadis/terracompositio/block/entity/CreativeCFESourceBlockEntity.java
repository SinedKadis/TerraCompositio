package net.sinedkadis.terracompositio.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.sinedkadis.terracompositio.api.TerraCompositioAPI;
import net.sinedkadis.terracompositio.api.networks.NetworkAction;
import net.sinedkadis.terracompositio.registries.ModBlockEntities;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetworkMemberBE;

public class CreativeCFESourceBlockEntity extends ModBlockEntity implements CFENetworkMemberBE {
    public CreativeCFESourceBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.CREATIVE_CFE_SOURCE_BE.get(),pPos, pBlockState);
    }

    public void tick(Level pLevel, BlockPos pPos, BlockState pState) {
        if (!pLevel.isClientSide) {
            boolean inNetwork = TerraCompositioAPI.instance().getCFENetworkInstance().isIn(pLevel, this);
            if (!inNetwork && !this.isRemoved()) {
                TerraCompositioAPI.INSTANCE.getCFENetworkInstance().fireCFENetworkEvent(this, NetworkAction.ADD);
            }
        }
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
