package net.sinedkadis.terracompositio.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.sinedkadis.terracompositio.registries.TCBlockEntities;
import org.jetbrains.annotations.NotNull;

public class TCHangingSignBlockEntity extends SignBlockEntity {
    public TCHangingSignBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(TCBlockEntities.TC_HANGING_SIGN.get(), pPos, pBlockState);
    }

    @Override
    public @NotNull BlockEntityType<?> getType() {
        return TCBlockEntities.TC_HANGING_SIGN.get();
    }
}