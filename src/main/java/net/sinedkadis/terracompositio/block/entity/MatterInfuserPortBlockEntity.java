package net.sinedkadis.terracompositio.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.sinedkadis.terracompositio.block.ModBlockStateProperties.INFUSED;

public class MatterInfuserPortBlockEntity extends ModItemIOCFEBlockEntity {

    public MatterInfuserPortBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.MATTER_INFUSER_PORT_BE.get(),pPos, pBlockState,100,10);
    }

    @Override
    public ItemStack getRenderStack() {
        return this.getInputSlot();
    }


    @Override
    public void tick(Level pLevel, BlockPos pPos, BlockState pState) {
        super.tick(pLevel, pPos, pState);
        if (!pState.canSurvive(pLevel,pPos))
            pLevel.destroyBlock(pPos,true);
    }

    @Override
    protected @Nullable <T> LazyOptional<T> getCap(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == ForgeCapabilities.ITEM_HANDLER && side == Direction.UP){
            return lazyItemHandler.cast();
        }
        return null;
    }
}
