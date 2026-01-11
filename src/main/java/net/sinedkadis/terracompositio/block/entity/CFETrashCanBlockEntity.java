package net.sinedkadis.terracompositio.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.sinedkadis.terracompositio.api.TerraCompositioAPI;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetworkMemberBE;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetworkMemberEntity;
import net.sinedkadis.terracompositio.api.networks.cfe.ICFEHandler;
import net.sinedkadis.terracompositio.api.networks.cfe.CFECapability;
import net.sinedkadis.terracompositio.block.behaviours.CFEHandlerBehaviour;
import net.sinedkadis.terracompositio.api.behaviors.blockentity.IBEBehaviour;
import net.sinedkadis.terracompositio.registries.TCBlockEntities;
import net.sinedkadis.terracompositio.util.TCUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class CFETrashCanBlockEntity extends TCBlockEntity {

    public CFETrashCanBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(TCBlockEntities.CFE_TRASH_CAN_BE.get(),pPos, pBlockState);
    }

    @Override
    void addBehaviours(@NotNull List<IBEBehaviour> list) {
        list.add(new CFEHandlerBehaviour(this){
            @Override
            public void init() {
                setPriority(Integer.MAX_VALUE);
                setMaxCFE(Integer.MAX_VALUE);
                setLimit(10);
            }

            @Override
            public void onCFENetworkMemberUpdate() {
                TerraCompositioAPI.instance().getCFENetworkInstance().getAllCFENetworkMembers(level).stream()
                        .filter(cfeNetworkMemberBE -> TCUtil.distSqr(cfeNetworkMemberBE.getPos(),worldPosition) <= 100)
                        .filter(cfeNetworkMemberBE -> {
                            if (cfeNetworkMemberBE instanceof CFENetworkMemberBE cfeNetworkMemberBE1)
                                return !(cfeNetworkMemberBE1.getEntity() instanceof CreativeCFESourceBlockEntity);
                            return true;
                        })
                        .filter(cfeNetworkMemberBE -> {
                            if (cfeNetworkMemberBE instanceof CFENetworkMemberBE cfeNetworkMemberBE1)
                                return cfeNetworkMemberBE1.getMainHandler().getCFE() > 0;
                            if (cfeNetworkMemberBE instanceof CFENetworkMemberEntity cfeNetworkMemberEntity) {
                                Optional<ICFEHandler> icfeHandler = cfeNetworkMemberEntity.getEntity().getCapability(CFECapability.CFE).resolve();
                                return icfeHandler.isPresent() && icfeHandler.get().getCFE() > 0;
                            }
                            return true;
                        })
                        .filter(cfeNetworkMemberBE -> cfeNetworkMemberBE != this)
                        .forEach(cfeNetworkMemberBE -> TCUtil.tryCFETransfer(this, cfeNetworkMemberBE, Integer.MAX_VALUE));
            }
        });
    }





}
