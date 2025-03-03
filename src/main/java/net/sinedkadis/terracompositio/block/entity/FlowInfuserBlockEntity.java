package net.sinedkadis.terracompositio.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.sinedkadis.terracompositio.particle.ModParticles;
import net.sinedkadis.terracompositio.recipe.FlowInfusionRecipe;

import java.util.Optional;

import static net.sinedkadis.terracompositio.block.ModBlockStateProperties.INFUSED;

public class FlowInfuserBlockEntity extends ModItemIOCFEBlockEntity {

    public FlowInfuserBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.FLOW_INFUSER_BE.get(),pPos, pBlockState,100,5);
    }

    @Override
    public void tick(Level pLevel, BlockPos pPos, BlockState pState) {
        if (pState.getValue(INFUSED))
            super.tick(pLevel, pPos, pState);
        if(hasRecipe() && enoughCFE()){
            increaseCraftingProgress();
            CFE = (int) (CFE-tickCFECost);
            setChanged(pLevel, pPos, pState);
            if (!pLevel.isClientSide){
                ((ServerLevel) pLevel).sendParticles(ModParticles.FLOW_STILL_PARTICLE.get(),pPos.getX()+0.5D,pPos.getY()+0.5D,pPos.getZ()+0.5D,3,0,-0.1D,0,0.1D);
            }
            if(hasProgressFinished()){
                craftItem();
                resetProgress();
            }
        }else if(!hasRecipe()) {
            resetProgress();
        }
    }

    protected void craftItem() {
        Optional<FlowInfusionRecipe> recipe = getCurrentRecipe();
        ItemStack result = recipe.get().getResultItem(null);
        this.itemHandler.extractItem(SLOT_INPUT,1,false);
        this.itemHandler.setStackInSlot(SLOT_OUTPUT, new ItemStack(result.getItem(),
                this.itemHandler.getStackInSlot(SLOT_OUTPUT).getCount()+result.getCount()));
    }
}
