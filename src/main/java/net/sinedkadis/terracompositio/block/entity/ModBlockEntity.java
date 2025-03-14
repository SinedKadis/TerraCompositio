package net.sinedkadis.terracompositio.block.entity;

import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class ModBlockEntity extends BlockEntity {
    public ModBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }


    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        LazyOptional<T> lazyItemHandler1 = getCap(cap,side);
        if (lazyItemHandler1 != null) return lazyItemHandler1;
        return super.getCapability(cap,side);
    }

    protected <T> @Nullable LazyOptional<T> getCap(@NotNull Capability<T> cap,@Nullable Direction side){
        return null;
    }
}