package net.sinedkadis.terracompositio.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.sinedkadis.terracompositio.registries.ModBlockEntities;
import org.jetbrains.annotations.NotNull;

public class ModHangingSignBlockEntity extends SignBlockEntity {
    public ModHangingSignBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.MOD_HANGING_SIGN.get(), pPos, pBlockState);
    }

    @Override
    public @NotNull BlockEntityType<?> getType() {
        return ModBlockEntities.MOD_HANGING_SIGN.get();
    }
}