package net.sinedkadis.terracompositio.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.sinedkadis.terracompositio.particle.CFEParticleData;
import net.sinedkadis.terracompositio.registries.TCBlockEntities;
import net.sinedkadis.terracompositio.recipe.FlowInfusionRecipe;

import java.util.Optional;

public class FlowInfuserBlockEntity extends TCItemIOCFEBlockEntity {

    public FlowInfuserBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(TCBlockEntities.FLOW_INFUSER_BE.get(),pPos, pBlockState,100,5,BlockMode.CONSUMER);
    }

    @Override
    public int getSlotLimit(int slot) {
        return 1;
    }


    public void tick(Level pLevel, BlockPos pPos, BlockState pState) {
        super.tick(pLevel, pPos, pState);
        if(hasRecipe() && enoughCFE()){
            increaseCraftingProgress();
            consumeCFE();
            setChanged(pLevel, pPos, pState);
            spawnParticles(pLevel, pPos);
            if(hasProgressFinished()){
                craftItem();
                resetProgress();
            }
        }else if(!hasRecipe()) {
            resetProgress();
        }
    }

    protected boolean hasRecipe() {
        Optional<FlowInfusionRecipe> recipe = getCurrentRecipe();
        if (recipe.isEmpty()){
            return false;
        }
        ItemStack result = recipe.get().getResultItem(null);
        boolean outputTest = enoughSpaceInOutput(result.getCount()) && sameItemInOutput(result.getItem());
        if (outputTest){
            maxProgress = recipe.get().getTicks();
            tickCFECost = recipe.get().getCFETick();
        }
        return outputTest;
    }

    protected Optional<FlowInfusionRecipe> getCurrentRecipe() {
        SimpleContainer inventory = new SimpleContainer(this.itemHandler.getSlots());
        for(int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, this.itemHandler.getStackInSlot(i));
        }

        assert this.level != null;
        return this.level.getRecipeManager().getRecipeFor(FlowInfusionRecipe.Type.INSTANCE, inventory, level);
    }

    private static void spawnParticles(Level pLevel, BlockPos targetPos) {
        if (!pLevel.isClientSide){
            TCCFEBlockEntity be = (TCCFEBlockEntity) pLevel.getBlockEntity(targetPos);
            float speed = 1/20f;
            if (be != null) {
                speed = be.getCfeContainer().getCfeTravelSpeed();
            }
            ((ServerLevel) pLevel).sendParticles(new CFEParticleData(speed),
                    targetPos.getX()+0.5D,
                    targetPos.getY()+0.5D,
                    targetPos.getZ()+0.5D,3,0,-0.1D,0,0.1D);
        }
    }


    protected void craftItem() {
        Optional<FlowInfusionRecipe> recipe = getCurrentRecipe();
        if (recipe.isPresent()) {
            ItemStack result = recipe.get().getResultItem(null);
            this.itemHandler.forceExtractItem(SLOT_INPUT, 1, false);
            this.itemHandler.forceInsertItem(SLOT_OUTPUT, new ItemStack(result.getItem(),
                    this.itemHandler.getStackInSlot(SLOT_OUTPUT).getCount() + result.getCount()),false);
        }
    }

}
