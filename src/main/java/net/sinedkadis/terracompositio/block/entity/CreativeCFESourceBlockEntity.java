package net.sinedkadis.terracompositio.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.api.TerraCompositioAPI;
import net.sinedkadis.terracompositio.api.cfe.CFENetworkAction;
import net.sinedkadis.terracompositio.api.cfe.CFENetworkHandler;
import net.sinedkadis.terracompositio.api.cfe.CFESource;

public class CreativeCFESourceBlockEntity extends ModBlockEntity implements CFESource {
    public CreativeCFESourceBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.CREATIVE_CFE_SOURCE_BE.get(),pPos, pBlockState);
    }

    public void tick(Level pLevel, BlockPos pPos, BlockState pState) {
        if (!pLevel.isClientSide) {
            boolean inNetwork = CFENetworkHandler.instance.isIn(pLevel, this);
            if (!inNetwork && !this.isRemoved()) {
                TerraCompositioAPI.INSTANCE.getCFENetworkInstance().fireCFENetworkEvent(this, CFENetworkAction.ADD);
            }
        }
    }

    @Override
    public Level getCFESourceLevel() {
        return getLevel();
    }

    @Override
    public BlockPos getCFESourceBlockPos() {
        return getBlockPos();
    }

    @Override
    public int getCurrentCFE() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void takeCFE(int cfe) {

    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        TerraCompositioAPI.INSTANCE.getCFENetworkInstance().fireCFENetworkEvent(this, CFENetworkAction.REMOVE);
    }
}
