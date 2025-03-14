package net.sinedkadis.terracompositio.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.sinedkadis.terracompositio.registries.ModBlockEntities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.sinedkadis.terracompositio.block.custom.FlowCedarCasingBlock.getDirectionByFunctionSide;
import static net.sinedkadis.terracompositio.registries.ModBlockStateProperties.INPUT_BUS;

public class FlowCedarCasingBlockEntity extends ModItemIOCFEBlockEntity{

    public FlowCedarCasingBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FLOW_CEDAR_CASING_BE.get(), pos, state);
    }

    @Override
    protected boolean sendPacketOnContentChange() {
        return true;
    }

    protected <T> @Nullable LazyOptional<T> getCap(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (side != null && cap == ForgeCapabilities.ITEM_HANDLER && side.equals(Direction.UP) && this.getBlockState().getValue(INPUT_BUS)) {
            return lazyItemHandler.cast();
        }
        return null;
    }
}
