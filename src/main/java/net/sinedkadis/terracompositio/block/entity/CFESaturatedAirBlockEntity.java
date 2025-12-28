package net.sinedkadis.terracompositio.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.sinedkadis.terracompositio.api.networks.cfe.ICFEHandler;
import net.sinedkadis.terracompositio.cfe.LimitlessCFEContainer;
import net.sinedkadis.terracompositio.registries.TCBlockEntities;
import net.sinedkadis.terracompositio.registries.TCBlocks;
import net.sinedkadis.terracompositio.util.TCUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class CFESaturatedAirBlockEntity extends TCCFEBlockEntity {

    public final List<Vec3> offsets = new ArrayList<>();

    public CFESaturatedAirBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(TCBlockEntities.CFE_SATURATED_AIR_BE.get(),pPos, pBlockState,BlockMode.SOURCE);
        this.setCfeContainer(new LimitlessCFEContainer(this));
    }

    @Override
    public int getLimit() {
        return 5;
    }

    private int timer = 40;
    @Override
    public void tick(Level pLevel, BlockPos pPos, BlockState pState) {
        super.tick(pLevel, pPos, pState);
        if (pLevel.isClientSide()) return;
        if (this.cfeContainer.getCFE() <= 0 && timer <=20 ){
            pLevel.setBlock(pPos, Blocks.AIR.defaultBlockState(),3);
        }


        if (timer <= 0) {
            List<BlockPos> touchingBlocks = TCUtil.getTouchingBlocks(pPos);

            List<ICFEHandler> handlers = new ArrayList<>(touchingBlocks.stream()
                    .filter(blockPos -> !blockPos.equals(pPos))
                    .peek(blockPos -> {
                        if (this.cfeContainer.getCFE()>50) {
                            BlockState blockState = pLevel.getBlockState(blockPos);
                            if (blockState.isAir() && !blockState.is(TCBlocks.CFE_SATURATED_AIR.get()))
                                pLevel.setBlockAndUpdate(blockPos, TCBlocks.CFE_SATURATED_AIR.get().defaultBlockState());
                        }
                    })
                    .map(pLevel::getBlockEntity)
                    .map(blockEntity -> blockEntity instanceof CFESaturatedAirBlockEntity cfeSaturatedAirBlockEntity
                            ? cfeSaturatedAirBlockEntity : null)
                    .filter(Objects::nonNull)
                    .map(CFESaturatedAirBlockEntity::getCfeContainer).toList());
            Collections.shuffle(handlers);
            handlers.forEach(container -> {
                int diff = Math.abs(this.cfeContainer.getCFE() - container.getCFE());
                if (diff > 20) {
                    TCUtil.tryCFETransfer(container,cfeContainer, Mth.clamp(diff/2,0,container.getFreeSpace()));
                }

            });

            timer = 20;
        }
        timer--;
    }

    @Override
    public void onCFENetworkMemberUpdate() {
    }

}
