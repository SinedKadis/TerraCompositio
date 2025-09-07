package net.sinedkadis.terracompositio.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.sinedkadis.terracompositio.api.TerraCompositioAPI;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetworkMemberBE;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetworkMemberEntity;
import net.sinedkadis.terracompositio.api.networks.cfe.ICFEHandler;
import net.sinedkadis.terracompositio.api.networks.cfe.CFECapability;
import net.sinedkadis.terracompositio.registries.TCBlockEntities;
import net.sinedkadis.terracompositio.util.TCUtil;

import java.util.Optional;

public class CFETrashCanBlockEntity extends TCCFEBlockEntity {

    public CFETrashCanBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(TCBlockEntities.CFE_TRASH_CAN_BE.get(),pPos, pBlockState,Integer.MAX_VALUE,10,BlockMode.CONSUMER);
        cfeContainer.setMaxCFE(Integer.MAX_VALUE);
    }

    public void tick(Level pLevel, BlockPos pPos, BlockState pState) {
        super.tick(pLevel,pPos,pState);
    }

    @Override
    public void onCFENetworkMemberUpdate(Level level, BlockPos pos) {
        TerraCompositioAPI.instance().getCFENetworkInstance().getAllCFENetworkMembers(level).stream()
                .filter(cfeNetworkMemberBE -> TCUtil.distSqr(cfeNetworkMemberBE.getBlockPos(),worldPosition) <= 100)
                .filter(cfeNetworkMemberBE -> {
                    if (cfeNetworkMemberBE instanceof CFENetworkMemberBE cfeNetworkMemberBE1)
                        return !(cfeNetworkMemberBE1.getBE() instanceof CreativeCFESourceBlockEntity);
                    return true;
                })
                .filter(cfeNetworkMemberBE -> {
                    if (cfeNetworkMemberBE instanceof CFENetworkMemberBE cfeNetworkMemberBE1)
                        return ((TCCFEBlockEntity) cfeNetworkMemberBE1.getBE()).getCfeContainer().getCFE() > 0;
                    if (cfeNetworkMemberBE instanceof CFENetworkMemberEntity cfeNetworkMemberEntity) {
                        Optional<ICFEHandler> icfeHandler = cfeNetworkMemberEntity.getEntity().getCapability(CFECapability.CFE).resolve();
                        return icfeHandler.isPresent() && icfeHandler.get().getCFE() > 0;
                    }
                    return true;
                })
                .filter(cfeNetworkMemberBE -> cfeNetworkMemberBE != this)
                .forEach(cfeNetworkMemberBE -> TCUtil.tryCFETransfer(this, cfeNetworkMemberBE, Integer.MAX_VALUE));
    }

    @Override
    public int getLimit() {
        return 10;
    }

    @Override
    public int getPriority() {
        return Integer.MAX_VALUE;
    }

}
