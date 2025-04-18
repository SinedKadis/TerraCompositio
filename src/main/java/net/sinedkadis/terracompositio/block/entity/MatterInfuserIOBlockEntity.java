package net.sinedkadis.terracompositio.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.sinedkadis.terracompositio.recipe.MatterInfusionRecipe;
import net.sinedkadis.terracompositio.registries.ModBlockEntities;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

import static net.sinedkadis.terracompositio.registries.ModBlockStateProperties.INFUSED;

public class MatterInfuserIOBlockEntity extends MatterInfuserBaseBlockEntity{

    private int progress;
    private int maxProgress;
    protected float tickCFECost;
    protected float catalystDecayRate;

    public MatterInfuserIOBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MATTER_INFUSER_IO_BE.get(), pos, state, 100, 10);
    }

    public void tick(Level pLevel, BlockPos pPos, BlockState pState) {
        super.tick(pLevel, pPos, pState);
        if(hasRecipe() && enoughCFE()){
            increaseCraftingProgress();
            consumeCFE();
            setChanged(pLevel, pPos, pState);
//            if (!pLevel.isClientSide){
//                ((ServerLevel) pLevel).sendParticles(ModParticles.FLOW_STILL_PARTICLE.get(),pPos.getX()+0.5D,pPos.getY()+0.5D,pPos.getZ()+0.5D,3,0,-0.1D,0,0.1D);
//            }
            if(hasProgressFinished()){
                craftItem();
                resetProgress();
            }
        }else if(!hasRecipe()) {
            resetProgress();
        }
    }

    private void consumeCFE() {
        CFE = (int) (CFE-tickCFECost);
    }

    private void resetProgress() {
        progress = 0;
    }

    private void craftItem() {
        Optional<MatterInfusionRecipe> recipe = getCurrentRecipe();
        MatterInfuserPortBlockEntity portBE = this.getPortBE();
        if (recipe.isPresent() && this.level != null && portBE != null) {
            ItemStack result = recipe.get().getResultItem(null);
            int takeCount = recipe.get().getIngredients().get(1).getItems()[0].getCount();
            this.extractItemStack(0, takeCount);
            this.forceInsertItemStack(1, new ItemStack(result.getItem(),
                    this.getItemInSlot(1).getCount() + result.getCount()));
            if (this.level.getRandom().nextInt(100) < catalystDecayRate){
                portBE.extractItemStack(0,1);
            }

//            if (this.level instanceof ServerLevel level1) {
//                level1.sendParticles(ModParticles.FLOW_STILL_PARTICLE.get(),
//                        this.getBlockPos().getX(),
//                        this.getBlockPos().getY(),
//                        this.getBlockPos().getZ(),
//                        10,
//                        this.level.getRandom().nextFloat(),
//                        this.level.getRandom().nextFloat(),
//                        this.level.getRandom().nextFloat(),
//                        0.5D);
//            }
        }
    }

    private boolean hasProgressFinished() {
        return progress>=maxProgress;
    }

    public int ticksLeft(){
        return maxProgress-progress;
    }

    private void increaseCraftingProgress() {
        progress++;
    }

    protected boolean enoughCFE() {
        return CFE >= tickCFECost;
    }

    private boolean hasRecipe() {
        Optional<MatterInfusionRecipe> recipe = getCurrentRecipe();
        if (recipe.isEmpty()){
            return false;
        }
        MatterInfusionRecipe matterInfusionRecipe = recipe.get();
        ItemStack result = matterInfusionRecipe.getResultItem(null);
        boolean outputTest = enoughSpaceInOutput(result.getCount()) && sameItemInOutput(result.getItem());
        FlowCedarCasingBlockEntity casingBE = this.getCasingBE();
        boolean infusedTest = false;
        if (casingBE != null) {
            infusedTest = casingBE.getBlockState().getValue(INFUSED);
        }
        if (outputTest && infusedTest) {
            maxProgress = matterInfusionRecipe.getTicks();
            tickCFECost = matterInfusionRecipe.getCFETick();
            catalystDecayRate = matterInfusionRecipe.getCatalystDecayRate();
        }
        return outputTest && infusedTest;
    }

    public Optional<MatterInfusionRecipe> getCurrentRecipe() {
        ItemStack catalyst = this.getCatalyst();
        ItemStack inputSlot = this.getInputSlot();
        if (catalyst.isEmpty() || inputSlot.isEmpty())
            return Optional.empty();
        SimpleContainer inventory;inventory = new SimpleContainer(catalyst, inputSlot);

        if (this.level != null) {
            return this.level.getRecipeManager().getRecipeFor(MatterInfusionRecipe.Type.INSTANCE, inventory, level);
        }
        return Optional.empty();
    }

    public ItemStack getCatalyst() {
        MatterInfuserPortBlockEntity port = this.getPortBE();
        return port != null ? port.getInputSlot() : ItemStack.EMPTY;
    }

    private @Nullable MatterInfuserPortBlockEntity getPortBE() {
        for (int i = 1; i <= 8; i++){
            if (level != null
                    && level.getBlockEntity(worldPosition.relative(
                            this.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING).getClockWise(), i))
                    instanceof MatterInfuserPortBlockEntity blockEntity) {
                if (blockEntity.getInputSlot().isEmpty())
                    continue;
                return blockEntity;
            }
        }
        return null;
    }

    private boolean sameItemInOutput(Item item) {
        ItemStack outputSlot = this.getOutputSlot();
        return outputSlot.isEmpty() || outputSlot.is(item);
    }

    private boolean enoughSpaceInOutput(int count) {
        ItemStack outputSlot = this.getOutputSlot();
        return outputSlot.getCount() + count <= outputSlot.getMaxStackSize();
    }
}
